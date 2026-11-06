package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.CidadaoRequest;
import br.com.fiap.bairroativo.dto.Sessao;
import br.com.fiap.bairroativo.exception.SemPermissaoException;
import br.com.fiap.bairroativo.model.Cidadao;
import br.com.fiap.bairroativo.model.ServidorPublico;
import br.com.fiap.bairroativo.model.Usuario;
import br.com.fiap.bairroativo.service.CidadaoService;
import br.com.fiap.bairroativo.service.SessaoService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Cadastro do cidadão. Qualquer pessoa cria a própria conta; cada cidadão só altera ou exclui a sua.
 * A lista completa (com contatos) é restrita aos servidores públicos.
 */
@Path("cidadaos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CidadaoResource {

    private final CidadaoService service;
    private final SessaoService sessoes;

    public CidadaoResource(CidadaoService service, SessaoService sessoes) {
        this.service = service;
        this.sessoes = sessoes;
    }

    @GET
    public List<Cidadao> listar(@HeaderParam(Identidade.PERFIL) String perfil,
                                @HeaderParam(Identidade.USUARIO) String u) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), "ver a lista de cidadãos");
        return service.listar();
    }

    @GET
    @Path("{id}")
    public Cidadao buscar(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                          @PathParam("id") long id) {
        Usuario usuario = sessoes.exigirUsuario(Identidade.ler(perfil, u));
        if (!(usuario instanceof ServidorPublico) && !usuario.getId().equals(id)) {
            throw new SemPermissaoException("Você só pode ver o seu próprio cadastro.");
        }
        return service.buscar(id);
    }

    /** Criar conta: aberto a qualquer pessoa. */
    @POST
    public Response cadastrar(CidadaoRequest req) {
        return Response.status(Response.Status.CREATED).entity(service.cadastrar(req)).build();
    }

    @PUT
    @Path("{id}")
    public Cidadao atualizar(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                             @PathParam("id") long id, CidadaoRequest req) {
        exigirProprioCadastro(Identidade.ler(perfil, u), id, "alterar");
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                            @PathParam("id") long id) {
        exigirProprioCadastro(Identidade.ler(perfil, u), id, "excluir");
        service.excluir(id);
        return Response.noContent().build();
    }

    private void exigirProprioCadastro(Sessao sessao, long id, String acao) {
        Cidadao cidadao = sessoes.exigirCidadao(sessao, acao + " o cadastro");
        if (!cidadao.getId().equals(id)) {
            throw new SemPermissaoException("Você só pode " + acao + " o seu próprio cadastro.");
        }
    }
}
