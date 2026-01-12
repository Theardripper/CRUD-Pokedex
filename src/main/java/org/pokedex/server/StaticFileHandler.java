package org.pokedex.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Optional;


public class StaticFileHandler {

    private static final String RESOURCES_BASE = "src/main/resources/html";
    private static final String IMG_BASE = RESOURCES_BASE + "img/";
    private static final String BASE_DIR = "src/main/resources/";


    public static void register(HttpServer server) {
        // HTMLs (estão na pasta html)
        serveFile(server, "/", BASE_DIR + "html/login.html", "text/html");
        serveFile(server, "/login", BASE_DIR + "html/login.html", "text/html");
        serveFile(server, "/register", BASE_DIR + "html/register.html", "text/html");
        serveFile(server, "/pokedex", BASE_DIR + "html/pokedex.html", "text/html");

        // CSS (está na pasta css)
        serveFile(server, "/css/style.css", BASE_DIR + "css/style.css", "text/css");

        // JS (estão na pasta js)
        serveFile(server, "/js/app.js", BASE_DIR + "js/app.js", "application/javascript");
        serveFile(server, "/js/login.js", BASE_DIR + "js/login.js", "application/javascript");
        serveFile(server, "/js/register.js", BASE_DIR + "js/register.js", "application/javascript");

        // Imagens (usando seu Handler genérico)
        server.createContext("/img/", new GenericFileHandler(BASE_DIR + "img/", "/img/"));
    }

    static class GenericFileHandler implements HttpHandler {
        private final String baseDir;
        private final String basePath;

        public GenericFileHandler(String baseDir, String basePath) {
            this.baseDir = baseDir;
            this.basePath = basePath;
        }

        private Optional<String> getContentType(String path) {
            if (path.endsWith(".png")) return Optional.of("image/png");
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return Optional.of("image/jpeg");
            if (path.endsWith(".ico")) return Optional.of("image/x-icon");
            return Optional.empty();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String requestPath = exchange.getRequestURI().getPath();

            String relativePath = requestPath.substring(basePath.length());
            Path filePath = Paths.get(baseDir, relativePath).normalize();

            if (!filePath.startsWith(Paths.get(baseDir).normalize())) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            try {
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                byte[] response = Files.readAllBytes(filePath);

                String contentType = getContentType(requestPath).orElse("application/octet-stream");

                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (IOException e) {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    private static void serveFile(HttpServer server, String route, String filePath, String contentType) {
        server.createContext(route, exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            try {
                byte[] response = Files.readAllBytes(Paths.get(filePath));
                exchange.getResponseHeaders().add("Content-Type", contentType + "; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (IOException e) {
                exchange.sendResponseHeaders(404, -1);
            }
        });
    }
}