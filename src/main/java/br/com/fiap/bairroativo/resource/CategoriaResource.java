package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.model.Categoria;
import br.com.fiap.bairroativo.service.CategoriaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    private final CategoriaService service;

    public CategoriaResource(CategoriaService service) {
        this.service = service;
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
    public Response criar(Categoria categoria) {
        return Response.status(Response.Status.CREATED).entity(service.criar(categoria)).build();
    }

    @PUT
    @Path("{id}")
    public Categoria atualizar(@PathParam("id") long id, Categoria categoria) {
        return service.atualizar(id, categoria);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
