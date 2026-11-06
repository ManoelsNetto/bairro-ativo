package br.com.fiap.bairroativo.resource;

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
import jakarta.ws.rs.HeaderParam;
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

    /* ---------- Consultas públicas ---------- */

    /** GET /api/ocorrencias?status=ABERTA&bairro=1&categoria=2&q=buraco (todos opcionais) */
    @GET
    public List<Ocorrencia> listar(@QueryParam("status") String status, @QueryParam("bairro") Long bairro,
                                   @QueryParam("categoria") Long categoria, @QueryParam("q") String texto) {
        StatusOcorrencia filtroStatus = status == null || status.isBlank()
                ? null : Validador.opcao(status, StatusOcorrencia.class, "o status");
        return service.listar(FiltroOcorrencia.simples(filtroStatus, bairro, categoria, texto));
    }

    @GET
    @Path("ranking")
    public List<Ocorrencia> ranking(@QueryParam("limite") @DefaultValue("5") int limite) {
        return service.ranking(limite);
    }

    @GET
    @Path("{id}")
    public OcorrenciaDetalheDTO detalhar(@HeaderParam(Identidade.PERFIL) String perfil,
                                         @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        return service.detalhar(id, Identidade.ler(perfil, u));
    }

    /* ---------- Cidadão ---------- */

    @GET
    @Path("minhas")
    public List<Ocorrencia> minhas(@HeaderParam(Identidade.PERFIL) String perfil,
                                   @HeaderParam(Identidade.USUARIO) String u) {
        return service.minhas(Identidade.ler(perfil, u));
    }

    @GET
    @Path("apoiadas")
    public List<Ocorrencia> apoiadas(@HeaderParam(Identidade.PERFIL) String perfil,
                                     @HeaderParam(Identidade.USUARIO) String u) {
        return service.apoiadas(Identidade.ler(perfil, u));
    }

    @POST
    public Response registrar(@HeaderParam(Identidade.PERFIL) String perfil,
                              @HeaderParam(Identidade.USUARIO) String u, OcorrenciaRequest req) {
        return Response.status(Response.Status.CREATED)
                .entity(service.registrar(Identidade.ler(perfil, u), req)).build();
    }

    @PUT
    @Path("{id}")
    public Ocorrencia atualizar(@HeaderParam(Identidade.PERFIL) String perfil,
                                @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id,
                                OcorrenciaRequest req) {
        return service.atualizar(Identidade.ler(perfil, u), id, req);
    }

    @DELETE
    @Path("{id}")
    public Response excluir(@HeaderParam(Identidade.PERFIL) String perfil,
                            @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        service.excluir(Identidade.ler(perfil, u), id);
        return Response.noContent().build();
    }

    /** O cidadão conectado apoia ("isso também me afeta"). */
    @POST
    @Path("{id}/apoio")
    public Response apoiar(@HeaderParam(Identidade.PERFIL) String perfil,
                           @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        return Response.status(Response.Status.CREATED)
                .entity(service.apoiar(Identidade.ler(perfil, u), id)).build();
    }

    @DELETE
    @Path("{id}/apoio")
    public Ocorrencia removerApoio(@HeaderParam(Identidade.PERFIL) String perfil,
                                   @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id) {
        return service.removerApoio(Identidade.ler(perfil, u), id);
    }

    /* ---------- Servidor público ---------- */

    @GET
    @Path("fila")
    public List<Ocorrencia> fila(@HeaderParam(Identidade.PERFIL) String perfil,
                                 @HeaderParam(Identidade.USUARIO) String u) {
        return service.fila(Identidade.ler(perfil, u));
    }

    @POST
    @Path("{id}/atualizacoes")
    public Response atualizarStatus(@HeaderParam(Identidade.PERFIL) String perfil,
                                    @HeaderParam(Identidade.USUARIO) String u, @PathParam("id") long id,
                                    AtualizacaoRequest req) {
        Atualizacao atualizacao = service.atualizarStatus(Identidade.ler(perfil, u), id, req);
        return Response.status(Response.Status.CREATED).entity(atualizacao).build();
    }
}
