package org.pokedex.server;

import com.sun.net.httpserver.HttpServer;
import org.pokedex.controller.AuthController;
import org.pokedex.controller.PokemonController;


public class ApiRouter {
    public static void register(HttpServer server) {
        // ENVOLVENDO o PokemonController e o AuthController no CorsApiHandler
        // Isso garante que os cabeçalhos CORS sejam adicionados e OPTIONS seja tratado antes da lógica do Controller
        server.createContext("/api/pokemons", new CorsApiHandler(new PokemonController()));
        server.createContext("/api/", new CorsApiHandler(new AuthController()));
    }
}