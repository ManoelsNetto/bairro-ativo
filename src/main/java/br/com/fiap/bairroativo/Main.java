package br.com.fiap.bairroativo;

import br.com.fiap.bairroativo.config.Config;
import br.com.fiap.bairroativo.config.ConnectionFactory;
import br.com.fiap.bairroativo.config.ErroHandler;
import br.com.fiap.bairroativo.config.JacksonConfig;
import br.com.fiap.bairroativo.dao.ApoioDAO;
import br.com.fiap.bairroativo.dao.AtualizacaoDAO;
import br.com.fiap.bairroativo.dao.BairroDAO;
import br.com.fiap.bairroativo.dao.CategoriaDAO;
import br.com.fiap.bairroativo.dao.CidadaoDAO;
import br.com.fiap.bairroativo.dao.OcorrenciaDAO;
import br.com.fiap.bairroativo.dao.OrgaoDAO;
import br.com.fiap.bairroativo.dao.RelatorioDAO;
import br.com.fiap.bairroativo.dao.ServidorDAO;
import br.com.fiap.bairroativo.resource.BairroResource;
import br.com.fiap.bairroativo.resource.CategoriaResource;
import br.com.fiap.bairroativo.resource.CidadaoResource;
import br.com.fiap.bairroativo.resource.OcorrenciaResource;
import br.com.fiap.bairroativo.resource.OrgaoResource;
import br.com.fiap.bairroativo.resource.RelatorioResource;
import br.com.fiap.bairroativo.resource.ServidorResource;
import br.com.fiap.bairroativo.resource.SessaoResource;
import br.com.fiap.bairroativo.service.BairroService;
import br.com.fiap.bairroativo.service.CategoriaService;
import br.com.fiap.bairroativo.service.CidadaoService;
import br.com.fiap.bairroativo.service.OcorrenciaService;
import br.com.fiap.bairroativo.service.OrgaoService;
import br.com.fiap.bairroativo.service.RelatorioService;
import br.com.fiap.bairroativo.service.ServidorService;
import br.com.fiap.bairroativo.service.SessaoService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sobe o servidor Grizzly com:
 *  - a API REST (Jersey) em http://localhost:8080/api
 *  - as páginas web (pasta web/) em http://localhost:8080/
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        // Jersey avisa que os resources "não são providers" por serem registrados como instância; é esperado
        Logger.getLogger("org.glassfish.jersey.internal.inject.Providers").setLevel(Level.SEVERE);

        ConnectionFactory conexoes = new ConnectionFactory();
        String porta = Config.get("PORT", "8080");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://0.0.0.0:" + porta + "/api"), configurar(conexoes), false);

        Path pastaWeb = Path.of(Config.get("WEB_DIR", "web")).toAbsolutePath().normalize();
        if (Files.isDirectory(pastaWeb)) {
            StaticHttpHandler paginas = new StaticHttpHandler(pastaWeb.toString());
            paginas.setFileCacheEnabled(false);
            server.getServerConfiguration().addHttpHandler(paginas, "/");
        } else {
            log.warning("Pasta das páginas web não encontrada em " + pastaWeb + ". Rode a partir da raiz do projeto.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        server.start();
        log.info("Bairro Ativo no ar: http://localhost:" + porta + "/  (API em http://localhost:" + porta + "/api)");
        Thread.currentThread().join();
    }

    /** Monta as camadas: DAO -> Service -> Resource. */
    static ResourceConfig configurar(ConnectionFactory conexoes) {
        BairroDAO bairroDAO = new BairroDAO(conexoes);
        OrgaoDAO orgaoDAO = new OrgaoDAO(conexoes);
        CategoriaDAO categoriaDAO = new CategoriaDAO(conexoes);
        CidadaoDAO cidadaoDAO = new CidadaoDAO(conexoes);
        ServidorDAO servidorDAO = new ServidorDAO(conexoes);

        SessaoService sessoes = new SessaoService(cidadaoDAO, servidorDAO);
        OcorrenciaService ocorrenciaService = new OcorrenciaService(new OcorrenciaDAO(conexoes),
                new ApoioDAO(conexoes), new AtualizacaoDAO(conexoes), bairroDAO, categoriaDAO, sessoes);

        return new ResourceConfig()
                .property(ServerProperties.WADL_FEATURE_DISABLE, true)
                .register(new SessaoResource(sessoes))
                .register(new CidadaoResource(new CidadaoService(cidadaoDAO, bairroDAO), sessoes))
                .register(new ServidorResource(new ServidorService(servidorDAO, orgaoDAO), sessoes))
                .register(new BairroResource(new BairroService(bairroDAO), sessoes))
                .register(new OrgaoResource(new OrgaoService(orgaoDAO), sessoes))
                .register(new CategoriaResource(new CategoriaService(categoriaDAO, orgaoDAO), sessoes))
                .register(new OcorrenciaResource(ocorrenciaService))
                .register(new RelatorioResource(new RelatorioService(new RelatorioDAO(conexoes))))
                .register(ErroHandler.class)
                .register(JacksonFeature.withoutExceptionMappers())
                .register(new JacksonConfig());
    }
}
