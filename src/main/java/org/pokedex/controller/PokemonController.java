package org.pokedex.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.pokedex.dao.PokemonDAO;
import org.pokedex.model.Pokemon;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class PokemonController implements HttpHandler {
    private final PokemonDAO dao = new PokemonDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        boolean hasId = path.matches(".*/api/pokemons/[^/]+$");


        if ("OPTIONS".equalsIgnoreCase(method)) {
            handleOptions(exchange);
            return;
        }

        try {
            switch (method) {
                case "GET" -> {
                    if (hasId) handleGetById(exchange, path);
                    else handleGetAll(exchange);
                }
                case "POST" -> handlePost(exchange);
                case "PUT" -> {
                    if (hasId) handlePut(exchange, path);
                    else exchange.sendResponseHeaders(400, -1);
                }
                case "DELETE" -> {
                    if (hasId) handleDelete(exchange, path);
                    else exchange.sendResponseHeaders(400, -1);
                }
                default -> exchange.sendResponseHeaders(405, -1);
            }
        } catch (IllegalArgumentException iae) {
            sendText(exchange, 400, "invalid id");
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }


    private void handleOptions(HttpExchange exchange) throws IOException {
        // Headers necessários para permitir CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");


        exchange.sendResponseHeaders(204, -1);
    }


    private void handleGetAll(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        List<org.pokedex.model.Pokemon> pokemons = dao.findAll();
        String json = gson.toJson(pokemons);
        sendJson(exchange, 200, json);
    }

    private void handleGetById(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String id = path.substring(path.lastIndexOf('/') + 1);
        Pokemon p = dao.findById(id);
        if (p == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        sendJson(exchange, 200, gson.toJson(p));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Pokemon pokemon = gson.fromJson(body, Pokemon.class);
        dao.insert(pokemon);
        exchange.sendResponseHeaders(201, -1);
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String id = path.substring(path.lastIndexOf('/') + 1);
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        JsonElement je = gson.fromJson(body, JsonElement.class);
        if (je != null && je.isJsonObject()) {
            JsonObject jo = je.getAsJsonObject();

            if (jo.size() == 1 && jo.has("catched")) {
                boolean catched = jo.get("catched").getAsBoolean();
                dao.updateCatch(id, catched);
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            Pokemon pokemon = gson.fromJson(body, Pokemon.class);
            pokemon.setId(id);
            dao.update(pokemon);
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        exchange.sendResponseHeaders(400, -1);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String id = path.substring(path.lastIndexOf('/') + 1);
        dao.delete(id);
        exchange.sendResponseHeaders(204, -1);
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}