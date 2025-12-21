package org.pokedex.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;
import org.pokedex.dao.UserDAO;
import org.pokedex.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class AuthController implements HttpHandler {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    private static final Map<String, String> SESSIONS = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();


        if ("OPTIONS".equalsIgnoreCase(method)) {
            handleOptions(exchange);
            return;
        }


        if ("POST".equalsIgnoreCase(method) && path.equals("/api/register")) {
            handleRegister(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(method) && path.equals("/api/login")) {
            handleLogin(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(method) && path.equals("/api/logout")) {
            handleLogout(exchange);
            return;
        }

        exchange.sendResponseHeaders(404, -1);
    }


    private void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }


    private String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String body = readBody(exchange.getRequestBody());
        var json = gson.fromJson(body, Map.class);
        String email = (String) json.get("email");
        String password = (String) json.get("password");
        String name = (String) json.getOrDefault("name", "");

        if (email == null || password == null) {
            sendJson(exchange, 400, Map.of("error", "email and password required"));
            return;
        }

        if (userDAO.findByEmail(email).isPresent()) {
            sendJson(exchange, 409, Map.of("error", "email already registered"));
            return;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(email, hash, name);
        userDAO.create(user);
        sendJson(exchange, 201, Map.of("message", "user created"));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        String body = readBody(exchange.getRequestBody());
        var json = gson.fromJson(body, Map.class);
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (email == null || password == null) {
            sendJson(exchange, 400, Map.of("error", "email and password required"));
            return;
        }

        var opt = userDAO.findByEmail(email);
        if (opt.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "invalid credentials"));
            return;
        }

        User user = opt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            sendJson(exchange, 401, Map.of("error", "invalid credentials"));
            return;
        }

        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, user.getId());

        String cookie = String.format("SESSION=%s; Path=/; HttpOnly; SameSite=Lax", token);
        exchange.getResponseHeaders().add("Set-Cookie", cookie);

        sendJson(exchange, 200, Map.of("message", "ok", "name", user.getName()));
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        var cookies = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Cookie"));
        if (cookies.isPresent()) {
            String c = cookies.get();
            for (String part : c.split(";")) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals("SESSION")) {
                    SESSIONS.remove(kv[1]);
                }
            }
        }

        exchange.getResponseHeaders().add("Set-Cookie", "SESSION=deleted; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
        sendJson(exchange, 200, Map.of("message", "logged out"));
    }

    private void sendJson(HttpExchange exchange, int status, Object obj) throws IOException {
        String json = gson.toJson(obj);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static Optional<String> getUserIdFromExchange(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return Optional.empty();
        for (String part : cookieHeader.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals("SESSION")) {
                String token = kv[1];
                return Optional.ofNullable(SESSIONS.get(token));
            }
        }
        return Optional.empty();
    }
}