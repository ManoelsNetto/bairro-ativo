package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.model.Bairro;
import br.com.fiap.bairroativo.service.BairroService;
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

@Path("bairros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BairroResource {

    private final BairroService service;

    public BairroResource(BairroService service) {
        this.service = service;
    }

    @GET
    public List<Bairro> listar() {
        return service.listar();
    }

    @GET
    @Path("{id}")
    public Bairro buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(Bairro bairro) {
        return Response.status(Response.Status.CREATED).entity(service.criar(bairro)).build();
    }

    @PUT
    @Path("{id}")
    public Bairro atualizar(@PathParam("id") long id, Bairro bairro) {
        return service.atualizar(id, bairro);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
