package org.pokedex.service;

import org.pokedex.dao.PokemonDAO;
import org.pokedex.model.Pokemon;

import java.util.List;
import java.util.stream.Collectors;


public class PokemonService {
    private final PokemonDAO dao = new PokemonDAO();

    public List<Pokemon> listarTodos() {
        return dao.findAll();
    }

    public List<Pokemon> filtrarPorTipo(String tipo) {
        return dao.findAll().stream()
                .filter(p -> p.getType().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());
    }

}