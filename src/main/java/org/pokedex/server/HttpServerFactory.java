package org.pokedex.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;


public class HttpServerFactory {
    public static void startServer(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        StaticFileHandler.register(server);
        ApiRouter.register(server);

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:" + port);
    }
}