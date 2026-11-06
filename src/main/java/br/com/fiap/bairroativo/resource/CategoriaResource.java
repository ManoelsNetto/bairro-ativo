package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.CategoriaRequest;
import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.service.CategoriaService;
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

/** Consulta pública; cadastrar, alterar e excluir é tarefa do servidor público (gestão). */
@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    private static final String GESTAO = "gerenciar categorias";

    private final CategoriaService service;
    private final SessaoService sessoes;

    public CategoriaResource(CategoriaService service, SessaoService sessoes) {
        this.service = service;
        this.sessoes = sessoes;
    }

    @GET
    public List<Categoria> listar() {
        return service.listar();
    }

    @GET
    @Path("{id}")
    public Categoria buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                          CategoriaRequest req) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return Response.status(Response.Status.CREATED).entity(service.criar(req)).build();
    }

    @PUT
    @Path("{id}")
    public Categoria atualizar(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                            @PathParam("id") long id, CategoriaRequest req) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String u,
                            @PathParam("id") long id) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        service.excluir(id);
        return Response.noContent().build();
    }
}
