package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.ServidorRequest;
import br.com.fiap.bairroativo.model.ServidorPublico;
import br.com.fiap.bairroativo.service.ServidorService;
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

/** Equipe da prefeitura: só servidores públicos consultam e gerenciam. */
@Path("servidores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServidorResource {

    private static final String GESTAO = "gerenciar servidores";

    private final ServidorService service;
    private final SessaoService sessoes;

    public ServidorResource(ServidorService service, SessaoService sessoes) {
        this.service = service;
        this.sessoes = sessoes;
    }

    @GET
    public List<ServidorPublico> listar(@HeaderParam(Identidade.PERFIL) String perfil,
                                        @HeaderParam(Identidade.USUARIO) String u) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return service.listar();
    }

    @GET
    @Path("{id}")
    public ServidorPublico buscar(@HeaderParam(Identidade.PERFIL) String perfil,
                                  @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return service.buscar(id);
    }

    @POST
    public Response cadastrar(@HeaderParam(Identidade.PERFIL) String perfil,
                              @HeaderParam(Identidade.USUARIO) String u, ServidorRequest req) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return Response.status(Response.Status.CREATED).entity(service.cadastrar(req)).build();
    }

    @PUT
    @Path("{id}")
    public ServidorPublico atualizar(@HeaderParam(Identidade.PERFIL) String perfil,
                                     @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id,
                                     ServidorRequest req) {
        sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@HeaderParam(Identidade.PERFIL) String perfil,
                            @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        ServidorPublico quem = sessoes.exigirServidor(Identidade.ler(perfil, u), GESTAO);
        service.excluir(id, quem);
        return Response.noContent().build();
    }
}
