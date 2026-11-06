package br.com.fiap.bairroativo.resource;

import br.com.fiap.bairroativo.dto.ContaExemploDTO;
import br.com.fiap.bairroativo.dto.EntrarRequest;
import br.com.fiap.bairroativo.model.Usuario;
import br.com.fiap.bairroativo.service.SessaoService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("sessao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessaoResource {

    private final SessaoService service;

    public SessaoResource(SessaoService service) {
        this.service = service;
    }

    /** Entrar (demonstração): { "perfil": "CIDADAO", "email": "ana.souza@email.com" } */
    @POST
    public Usuario entrar(EntrarRequest req) {
        return service.entrar(req);
    }

    /** Quem está conectado (confere se a sessão guardada no navegador ainda vale). */
    @GET
    public Usuario atual(@HeaderParam(Identidade.PERFIL) String perfil, @HeaderParam(Identidade.USUARIO) String id) {
        return service.exigirUsuario(Identidade.ler(perfil, id));
    }

    @GET
    @Path("exemplos")
    public List<ContaExemploDTO> exemplos() {
        return service.exemplos();
    }
}
