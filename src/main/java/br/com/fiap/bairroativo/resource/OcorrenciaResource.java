package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.ApoioRequest;
import br.com.fiap.bairroativo.dto.AtualizacaoRequest;
import br.com.fiap.bairroativo.dto.FiltroOcorrencia;
import br.com.fiap.bairroativo.dto.OcorrenciaDetalheDTO;
import br.com.fiap.bairroativo.dto.OcorrenciaRequest;
import br.com.fiap.bairroativo.model.Atualizacao;
import br.com.fiap.bairroativo.model.Ocorrencia;
import br.com.fiap.bairroativo.model.StatusOcorrencia;
import br.com.fiap.bairroativo.service.OcorrenciaService;
import br.com.fiap.bairroativo.service.Validador;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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

@Path("ocorrencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OcorrenciaResource {

    private final OcorrenciaService service;

    public OcorrenciaResource(OcorrenciaService service) {
        this.service = service;
    }

    /** GET /api/ocorrencias?status=ABERTA&bairro=1&categoria=2&q=buraco (todos opcionais) */
    @GET
    public List<Ocorrencia> listar(@QueryParam("status") String status, @QueryParam("bairro") Long bairro,
                                   @QueryParam("categoria") Long categoria, @QueryParam("q") String texto,
                                   @QueryParam("pendentes") boolean pendentes) {
        StatusOcorrencia filtroStatus = status == null || status.isBlank()
                ? null : Validador.opcao(status, StatusOcorrencia.class, "o status");
        return service.listar(new FiltroOcorrencia(filtroStatus, bairro, categoria, texto, pendentes));
    }

    @GET
    @Path("ranking")
    public List<Ocorrencia> ranking(@QueryParam("limite") @DefaultValue("5") int limite) {
        return service.ranking(limite);
    }

    @GET
    @Path("{id}")
    public OcorrenciaDetalheDTO detalhar(@PathParam("id") long id) {
        return service.detalhar(id);
    }

    @POST
    public Response registrar(OcorrenciaRequest req) {
        return Response.status(Response.Status.CREATED).entity(service.registrar(req)).build();
    }

    @PUT
    @Path("{id}")
    public Ocorrencia atualizar(@PathParam("id") long id, OcorrenciaRequest req) {
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@PathParam("id") long id) {
        service.excluir(id);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/apoios")
    public Response apoiar(@PathParam("id") long id, ApoioRequest req) {
        Validador.corpo(req);
        return Response.status(Response.Status.CREATED).entity(service.apoiar(id, req.idCidadao())).build();
    }

    @DELETE
    @Path("{id}/apoios/{idCidadao}")
    public Ocorrencia removerApoio(@PathParam("id") long id, @PathParam("idCidadao") long idCidadao) {
        return service.removerApoio(id, idCidadao);
    }

    @POST
    @Path("{id}/atualizacoes")
    public Response atualizarStatus(@PathParam("id") long id, AtualizacaoRequest req) {
        Atualizacao atualizacao = service.atualizarStatus(id, req);
        return Response.status(Response.Status.CREATED).entity(atualizacao).build();
    }
}
