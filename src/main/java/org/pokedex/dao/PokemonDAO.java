package org.pokedex.dao;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.pokedex.model.Pokemon;

import java.util.ArrayList;
import java.util.List;


public class PokemonDAO {

    private final MongoCollection<Document> collection;

    public PokemonDAO() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("pokedex");
        this.collection = db.getCollection("pokemons");
    }

    public void insert(Pokemon pokemon) {
        Document doc = new Document()
                .append("name", pokemon.getName())
                .append("type", pokemon.getType())
                .append("weight", pokemon.getWeight() != null ? pokemon.getWeight() : 0.0)
                .append("height", pokemon.getHeight() != null ? pokemon.getHeight() : 0.0)
                .append("catched", pokemon.isCatched()); // sem comparação com null, pois é boolean primitivo

        collection.insertOne(doc);
    }

    public void updateCatch(String id, boolean catched) {
        collection.updateOne(
                Filters.eq("_id", new ObjectId(id)),
                new Document("$set", new Document("catched", catched))
        );
    }

    public List<Pokemon> findAll() {
        List<Pokemon> list = new ArrayList<>();
        for (Document doc : collection.find()) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public Pokemon findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return doc != null ? fromDocument(doc) : null;
    }

    public void update(Pokemon pokemon) {
        Document doc = new Document()
                .append("name", pokemon.getName())
                .append("type", pokemon.getType())
                .append("weight", pokemon.getWeight() != null ? pokemon.getWeight() : 0.0)
                .append("height", pokemon.getHeight() != null ? pokemon.getHeight() : 0.0)
                .append("catched", pokemon.isCatched()); // idem aqui

        collection.updateOne(
                Filters.eq("_id", new ObjectId(pokemon.getId())),
                new Document("$set", doc)
        );
    }

    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
    }


    private Pokemon fromDocument(Document doc) {
        // obtém números de forma genérica e converte para double quando não nulos
        Number weightNum = doc.get("weight", Number.class);
        Number heightNum = doc.get("height", Number.class);
        Boolean catchedVal = doc.getBoolean("catched");

        double weight = weightNum != null ? weightNum.doubleValue() : 0.0;
        double height = heightNum != null ? heightNum.doubleValue() : 0.0;
        boolean catched = catchedVal != null ? catchedVal : false;

        return new Pokemon(
                doc.getObjectId("_id").toHexString(),
                doc.getString("name"),
                doc.getString("type"),
                weight,
                height,
                catched
        );
    }

}