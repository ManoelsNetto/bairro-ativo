package br.com.fiap.bairroativo.service;

import br.com.fiap.bairroativo.dao.ApoioDAO;
import br.com.fiap.bairroativo.dao.AtualizacaoDAO;
import br.com.fiap.bairroativo.dao.BairroDAO;
import br.com.fiap.bairroativo.dao.CategoriaDAO;
import br.com.fiap.bairroativo.dao.OcorrenciaDAO;
import br.com.fiap.bairroativo.dto.AtualizacaoRequest;
import br.com.fiap.bairroativo.dto.FiltroOcorrencia;
import br.com.fiap.bairroativo.dto.OcorrenciaDetalheDTO;
import br.com.fiap.bairroativo.dto.OcorrenciaRequest;
import br.com.fiap.bairroativo.dto.Sessao;
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.RegraNegocioException;
import br.com.fiap.bairroativo.exception.SemPermissaoException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.Apoio;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.ServidorPublico;
import br.com.fiap.bairroativo.model.StatusOcorrencia;
import br.com.fiap.bairroativo.model.Usuario;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Regras do ciclo de vida de uma ocorrência.
 * O cidadão registra, corrige e apoia; o servidor do órgão responsável atende.
 * Quem faz cada ação é sempre o usuário da sessão, nunca um id escolhido no formulário.
 */
public class OcorrenciaService {

    private static final Comparator<Ocorrencia> POR_PRIORIDADE =
            Comparator.comparingInt(Ocorrencia::getPontuacaoPrioridade).reversed()
                    .thenComparing(Ocorrencia::getDataAbertura);

    private final OcorrenciaDAO dao;
    private final ApoioDAO apoioDAO;
    private final AtualizacaoDAO atualizacaoDAO;
    private final BairroDAO bairroDAO;
    private final CategoriaDAO categoriaDAO;
    private final SessaoService sessoes;

    public OcorrenciaService(OcorrenciaDAO dao, ApoioDAO apoioDAO, AtualizacaoDAO atualizacaoDAO,
                             BairroDAO bairroDAO, CategoriaDAO categoriaDAO, SessaoService sessoes) {
        this.dao = dao;
        this.apoioDAO = apoioDAO;
        this.atualizacaoDAO = atualizacaoDAO;
        this.bairroDAO = bairroDAO;
        this.categoriaDAO = categoriaDAO;
        this.sessoes = sessoes;
    }

    /* ---------- Consultas públicas ---------- */

    public List<Ocorrencia> listar(FiltroOcorrencia filtro) {
        return dao.listar(filtro);
    }

    public Ocorrencia buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Ocorrência não encontrada."));
    }

    /** Detalhe com o que o usuário atual pode fazer (visitante só consulta). */
    public OcorrenciaDetalheDTO detalhar(long id, Sessao sessao) {
        Ocorrencia ocorrencia = buscar(id);
        List<Apoio> apoios = apoioDAO.listarPorOcorrencia(id);
        List<Atualizacao> historico = atualizacaoDAO.listarPorOcorrencia(id);
        Usuario usuario = sessoes.usuario(sessao).orElse(null);

        boolean podeEditar = usuario != null && usuario.podeEditar(ocorrencia);
        boolean podeAtender = usuario != null && usuario.podeAtender(ocorrencia);
        boolean jaApoiou = usuario instanceof Cidadao c
                && apoios.stream().anyMatch(a -> a.getCidadao().getId().equals(c.getId()));
        boolean podeApoiar = usuario instanceof Cidadao c && ocorrencia.isPendente() && !jaApoiou
                && !c.getId().equals(ocorrencia.getAutor().getId());
        Set<StatusOcorrencia> proximos = podeAtender ? ocorrencia.getStatus().proximosPermitidos() : Set.of();
        return new OcorrenciaDetalheDTO(ocorrencia, apoios, historico, proximos, podeEditar, podeAtender,
                podeApoiar, jaApoiou);
    }

    public List<Ocorrencia> ranking(int limite) {
        validarLimite(limite);
        return dao.listar(FiltroOcorrencia.pendentes()).stream().sorted(POR_PRIORIDADE).limit(limite).toList();
    }

    /* ---------- Área do cidadão ---------- */

    public List<Ocorrencia> minhas(Sessao sessao) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "ver suas ocorrências");
        return dao.listar(FiltroOcorrencia.doAutor(cidadao.getId()));
    }

    public List<Ocorrencia> apoiadas(Sessao sessao) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "ver as ocorrências que apoia");
        return dao.listar(FiltroOcorrencia.apoiadasPor(cidadao.getId()));
    }

    public Ocorrencia registrar(Sessao sessao, OcorrenciaRequest req) {
        Cidadao autor = sessoes.exigirCidadao(sessao, "registrar uma ocorrência");
        Validador.corpo(req);
        Ocorrencia nova = montar(req);
        nova.setAutor(autor);
        nova.setStatus(StatusOcorrencia.ABERTA);
        return dao.inserir(nova);
    }

    public Ocorrencia atualizar(Sessao sessao, long id, OcorrenciaRequest req) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "editar uma ocorrência");
        Validador.corpo(req);
        Ocorrencia atual = buscar(id);
        exigirAutor(cidadao, atual, "editar");
        if (!cidadao.podeEditar(atual)) {
            throw new RegraNegocioException("Só é possível editar enquanto a ocorrência está Aberta. Ela já está "
                    + atual.getStatus().getDescricao().toLowerCase() + ".");
        }
        Ocorrencia editada = montar(req);
        editada.setId(id);
        dao.atualizar(editada);
        return buscar(id);
    }

    /** Em atendimento ou resolvida, a ocorrência fica no histórico; só abertas ou canceladas podem ser excluídas. */
    public void excluir(Sessao sessao, long id) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "excluir uma ocorrência");
        Ocorrencia atual = buscar(id);
        exigirAutor(cidadao, atual, "excluir");
        StatusOcorrencia status = atual.getStatus();
        if (status != StatusOcorrencia.ABERTA && status != StatusOcorrencia.CANCELADA) {
            throw new RegraNegocioException("Ocorrências em atendimento ou resolvidas não podem ser excluídas.");
        }
        dao.excluir(id);
    }

    public Ocorrencia apoiar(Sessao sessao, long idOcorrencia) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "apoiar uma ocorrência");
        Ocorrencia ocorrencia = buscar(idOcorrencia);
        if (!ocorrencia.isPendente()) {
            throw new RegraNegocioException("Esta ocorrência já foi encerrada e não recebe mais apoios.");
        }
        if (ocorrencia.getAutor().getId().equals(cidadao.getId())) {
            throw new RegraNegocioException("Você registrou esta ocorrência, então seu apoio já está contado.");
        }
        // Apoio repetido é barrado pela PK composta de BA_APOIO ("Você já apoia esta ocorrência.")
        apoioDAO.inserir(cidadao.getId(), idOcorrencia);
        return buscar(idOcorrencia);
    }

    public Ocorrencia removerApoio(Sessao sessao, long idOcorrencia) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, "retirar um apoio");
        buscar(idOcorrencia);
        if (!apoioDAO.excluir(cidadao.getId(), idOcorrencia)) {
            throw new NaoEncontradoException("Você não apoia esta ocorrência.");
        }
        return buscar(idOcorrencia);
    }

    /* ---------- Área do servidor ---------- */

    /** Pendências das categorias do órgão do servidor, da mais urgente para a menos urgente. */
    public List<Ocorrencia> fila(Sessao sessao) {
        ServidorPublico servidor = sessoes.exigirServidor(sessao, "ver a fila de atendimento");
        return dao.listar(FiltroOcorrencia.pendentesDoOrgao(servidor.getOrgao().getId())).stream()
                .sorted(POR_PRIORIDADE).toList();
    }

    public Atualizacao atualizarStatus(Sessao sessao, long idOcorrencia, AtualizacaoRequest req) {
        ServidorPublico servidor = sessoes.exigirServidor(sessao, "atualizar o atendimento");
        Validador.corpo(req);
        Ocorrencia ocorrencia = buscar(idOcorrencia);
        StatusOcorrencia anterior = ocorrencia.getStatus();
        if (anterior.isFinal()) {
            throw new RegraNegocioException("Esta ocorrência já está " + anterior.getDescricao().toLowerCase()
                    + " e não pode mudar de status.");
        }
        if (!servidor.podeAtender(ocorrencia)) {
            throw new SemPermissaoException("Esta ocorrência é atendida por " + ocorrencia.getCategoria().getOrgao()
                    .getNome() + ". Você é de " + servidor.getOrgao().getNome() + ".");
        }
        StatusOcorrencia novo = Validador.opcao(req.status(), StatusOcorrencia.class, "o novo status");
        String comentario = Validador.texto(req.comentario(), "um comentário para o cidadão", 5, 500);
        if (!anterior.podeMudarPara(novo)) {
            throw new RegraNegocioException("Não é possível passar de " + anterior.getDescricao() + " para "
                    + novo.getDescricao() + ".");
        }

        Atualizacao atualizacao = new Atualizacao();
        atualizacao.setIdOcorrencia(idOcorrencia);
        atualizacao.setServidor(servidor);
        atualizacao.setStatusAnterior(anterior);
        atualizacao.setStatusNovo(novo);
        atualizacao.setComentario(comentario);
        LocalDateTime resolucao = novo == StatusOcorrencia.RESOLVIDA ? LocalDateTime.now() : null;
        return atualizacaoDAO.registrar(atualizacao, resolucao);
    }

    /* ---------- Apoio interno ---------- */

    private void exigirAutor(Cidadao cidadao, Ocorrencia ocorrencia, String acao) {
        if (!ocorrencia.getAutor().getId().equals(cidadao.getId())) {
            throw new SemPermissaoException("Só quem registrou a ocorrência pode " + acao + ".");
        }
    }

    private void validarLimite(int limite) {
        if (limite < 1 || limite > 50) {
            throw new ValidacaoException("O limite deve ficar entre 1 e 50.");
        }
    }

    /** Valida os campos comuns de cadastro e edição. */
    private Ocorrencia montar(OcorrenciaRequest req) {
        Ocorrencia o = new Ocorrencia();
        o.setTitulo(Validador.texto(req.titulo(), "o título", 5, 100));
        o.setDescricao(Validador.texto(req.descricao(), "a descrição do problema", 10, 1000));
        o.setEndereco(Validador.texto(req.endereco(), "o endereço", 5, 150));
        o.setReferencia(Validador.textoOpcional(req.referencia(), "o ponto de referência", 150));
        Bairro bairro = bairroDAO.buscarPorId(Validador.id(req.idBairro(), "o bairro"))
                .orElseThrow(() -> new ValidacaoException("O bairro selecionado não existe."));
        Categoria categoria = categoriaDAO.buscarPorId(Validador.id(req.idCategoria(), "a categoria"))
                .orElseThrow(() -> new ValidacaoException("A categoria selecionada não existe."));
        o.setBairro(bairro);
        o.setCategoria(categoria);
        return o;
    }
}
