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
import br.com.fiap.bairroativo.exception.NaoEncontradoException;
import br.com.fiap.bairroativo.exception.RegraNegocioException;
import br.com.fiap.bairroativo.exception.ValidacaoException;
import br.com.fiap.bairroativo.model.AgentePublico;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.StatusOcorrencia;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Regras do ciclo de vida de uma ocorrência: registro, edição, apoio da vizinhança e atendimento. */
public class OcorrenciaService {

    private final OcorrenciaDAO dao;
    private final ApoioDAO apoioDAO;
    private final AtualizacaoDAO atualizacaoDAO;
    private final BairroDAO bairroDAO;
    private final CategoriaDAO categoriaDAO;
    private final UsuarioService usuarioService;

    public OcorrenciaService(OcorrenciaDAO dao, ApoioDAO apoioDAO, AtualizacaoDAO atualizacaoDAO,
                             BairroDAO bairroDAO, CategoriaDAO categoriaDAO, UsuarioService usuarioService) {
        this.dao = dao;
        this.apoioDAO = apoioDAO;
        this.atualizacaoDAO = atualizacaoDAO;
        this.bairroDAO = bairroDAO;
        this.categoriaDAO = categoriaDAO;
        this.usuarioService = usuarioService;
    }

    public List<Ocorrencia> listar(FiltroOcorrencia filtro) {
        return dao.listar(filtro);
    }

    public Ocorrencia buscar(long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NaoEncontradoException("Ocorrência não encontrada."));
    }

    public OcorrenciaDetalheDTO detalhar(long id) {
        Ocorrencia ocorrencia = buscar(id);
        return new OcorrenciaDetalheDTO(ocorrencia, apoioDAO.listarPorOcorrencia(id),
                atualizacaoDAO.listarPorOcorrencia(id), ocorrencia.getStatus().proximosPermitidos());
    }

    /** Ocorrências pendentes ordenadas pela pontuação de prioridade (maior primeiro). */
    public List<Ocorrencia> ranking(int limite) {
        if (limite < 1 || limite > 50) {
            throw new ValidacaoException("O limite do ranking deve ficar entre 1 e 50.");
        }
        return dao.listar(FiltroOcorrencia.pendentes()).stream()
                .sorted(Comparator.comparingInt(Ocorrencia::getPontuacaoPrioridade).reversed()
                        .thenComparing(Ocorrencia::getDataAbertura))
                .limit(limite)
                .toList();
    }

    public Ocorrencia registrar(OcorrenciaRequest req) {
        Validador.corpo(req);
        Ocorrencia nova = montar(req);
        nova.setAutor(usuarioService.buscarCidadao(Validador.id(req.idAutor(), "o cidadão que está registrando")));
        nova.setStatus(StatusOcorrencia.ABERTA);
        return dao.inserir(nova);
    }

    /** O cidadão só pode corrigir a ocorrência enquanto ela ainda não começou a ser analisada. */
    public Ocorrencia atualizar(long id, OcorrenciaRequest req) {
        Validador.corpo(req);
        Ocorrencia atual = buscar(id);
        if (atual.getStatus() != StatusOcorrencia.ABERTA) {
            throw new RegraNegocioException(
                    "Só é possível editar ocorrências com status Aberta. Esta já está " +
                            atual.getStatus().getDescricao().toLowerCase() + ".");
        }
        Ocorrencia editada = montar(req);
        editada.setId(id);
        dao.atualizar(editada);
        return buscar(id);
    }

    /** Ocorrências em atendimento ou resolvidas ficam no histórico; só abertas ou canceladas podem ser excluídas. */
    public void excluir(long id) {
        Ocorrencia atual = buscar(id);
        StatusOcorrencia status = atual.getStatus();
        if (status != StatusOcorrencia.ABERTA && status != StatusOcorrencia.CANCELADA) {
            throw new RegraNegocioException("Ocorrências em atendimento ou resolvidas não podem ser excluídas.");
        }
        dao.excluir(id);
    }

    public Ocorrencia apoiar(long idOcorrencia, Long idCidadao) {
        Ocorrencia ocorrencia = buscar(idOcorrencia);
        Cidadao cidadao = usuarioService.buscarCidadao(Validador.id(idCidadao, "quem está apoiando"));
        if (!ocorrencia.isPendente()) {
            throw new RegraNegocioException("Esta ocorrência já foi encerrada e não recebe mais apoios.");
        }
        if (ocorrencia.getAutor().getId().equals(cidadao.getId())) {
            throw new RegraNegocioException("Quem registrou a ocorrência não pode apoiá-la.");
        }
        // Apoio repetido é barrado pela PK composta de BA_APOIO (vira "Este cidadão já apoiou esta ocorrência.")
        apoioDAO.inserir(cidadao.getId(), idOcorrencia);
        return buscar(idOcorrencia);
    }

    public Ocorrencia removerApoio(long idOcorrencia, long idCidadao) {
        buscar(idOcorrencia);
        if (!apoioDAO.excluir(idCidadao, idOcorrencia)) {
            throw new NaoEncontradoException("Apoio não encontrado.");
        }
        return buscar(idOcorrencia);
    }

    public Atualizacao atualizarStatus(long idOcorrencia, AtualizacaoRequest req) {
        Validador.corpo(req);
        Ocorrencia ocorrencia = buscar(idOcorrencia);
        AgentePublico agente = usuarioService.buscarAgente(Validador.id(req.idAgente(), "o agente responsável"));
        StatusOcorrencia novo = Validador.opcao(req.status(), StatusOcorrencia.class, "o novo status");
        String comentario = Validador.texto(req.comentario(), "um comentário para o cidadão", 5, 500);

        StatusOcorrencia anterior = ocorrencia.getStatus();
        if (!anterior.podeMudarPara(novo)) {
            throw new RegraNegocioException(anterior.isFinal()
                    ? "Esta ocorrência já está " + anterior.getDescricao().toLowerCase() + " e não pode mudar de status."
                    : "Não é possível passar de " + anterior.getDescricao() + " para " + novo.getDescricao() + ".");
        }

        Atualizacao atualizacao = new Atualizacao();
        atualizacao.setIdOcorrencia(idOcorrencia);
        atualizacao.setAgente(agente);
        atualizacao.setStatusAnterior(anterior);
        atualizacao.setStatusNovo(novo);
        atualizacao.setComentario(comentario);
        LocalDateTime resolucao = novo == StatusOcorrencia.RESOLVIDA ? LocalDateTime.now() : null;
        return atualizacaoDAO.registrar(atualizacao, resolucao);
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
