package org.pokedex;

import org.pokedex.server.HttpServerFactory;
import org.pokedex.server.StaticFileHandler;

import java.io.*;

/**
 * Ponto de entrada da aplicação.
 * Executar essa classe inicia o servidor HTTP em uma porta (8080 no exemplo).
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // Inicia servidor na porta 8080
        HttpServerFactory.startServer(8080);


    }
}