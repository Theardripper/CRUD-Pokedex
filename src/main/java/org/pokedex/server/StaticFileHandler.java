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

    private static final String RESOURCES_BASE = "src/main/resources/";
    private static final String IMG_BASE = RESOURCES_BASE + "img/";

    public static void register(HttpServer server) {
        // Arquivos na raiz de resources (HTML, CSS, JS)
        serveFile(server, "/", RESOURCES_BASE + "login.html", "text/html");
        serveFile(server, "/login", RESOURCES_BASE + "login.html", "text/html");
        serveFile(server, "/register", RESOURCES_BASE + "register.html", "text/html");
        serveFile(server, "/pokedex.html", RESOURCES_BASE + "pokedex.html", "text/html");
        serveFile(server, "/style.css", RESOURCES_BASE + "style.css", "text/css");
        serveFile(server, "/app.js", RESOURCES_BASE + "app.js", "application/javascript");
        serveFile(server, "/login.js", RESOURCES_BASE + "login.js", "application/javascript");
        serveFile(server, "/register.js", RESOURCES_BASE + "register.js", "application/javascript");

        server.createContext("/img/", new GenericFileHandler(IMG_BASE, "/img/"));
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