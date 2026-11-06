package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.EngajamentoDTO;
import br.com.fiap.bairroativo.dto.RelatorioBairroDTO;
import br.com.fiap.bairroativo.dto.RelatorioCategoriaDTO;
import br.com.fiap.bairroativo.dto.ResumoDTO;
import br.com.fiap.bairroativo.service.RelatorioService;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RelatorioResource {

    private final RelatorioService service;

    public RelatorioResource(RelatorioService service) {
        this.service = service;
    }

    @GET
    @Path("relatorios/resumo")
    public ResumoDTO resumo() {
        return service.resumo();
    }

    @GET
    @Path("relatorios/bairros")
    public List<RelatorioBairroDTO> porBairro() {
        return service.porBairro();
    }

    @GET
    @Path("relatorios/categorias")
    public List<RelatorioCategoriaDTO> porCategoria() {
        return service.porCategoria();
    }

    @GET
    @Path("relatorios/engajamento")
    public List<EngajamentoDTO> engajamento(@QueryParam("limite") @DefaultValue("10") int limite) {
        return service.engajamento(limite);
    }

    /** Saúde da aplicação: GET /api/status */
    @GET
    @Path("status")
    public Response status() {
        boolean banco = service.bancoDisponivel();
        return Response.status(banco ? 200 : 503)
                .entity(Map.of("api", "ok", "banco", banco ? "ok" : "indisponível"))
                .build();
    }
}
