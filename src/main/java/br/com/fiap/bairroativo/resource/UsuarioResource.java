package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.UsuarioRequest;
import br.com.fiap.bairroativo.model.Usuario;
import br.com.fiap.bairroativo.service.UsuarioService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private final UsuarioService service;

    public UsuarioResource(UsuarioService service) {
        this.service = service;
    }

    /** GET /api/usuarios?tipo=CIDADAO|AGENTE */
    @GET
    public List<Usuario> listar(@QueryParam("tipo") String tipo) {
        return service.listar(tipo);
    }

    @GET
    @Path("{id}")
    public Usuario buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(UsuarioRequest req) {
        return Response.status(Response.Status.CREATED).entity(service.criar(req)).build();
    }

    @PUT
    @Path("{id}")
    public Usuario atualizar(@PathParam("id") long id, UsuarioRequest req) {
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
