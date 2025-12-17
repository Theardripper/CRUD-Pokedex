package org.pokedex.dao;


import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.pokedex.model.User;

import java.util.Optional;


public class UserDAO {
    private final MongoClient client;
    private final MongoDatabase db;
    private final MongoCollection<Document> users;

    public UserDAO() {
        String uri = System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017");
        client = MongoClients.create(uri);
        db = client.getDatabase(System.getenv().getOrDefault("MONGO_DB", "pokedexdb"));
        users = db.getCollection("users");
    }

    public Optional<User> findByEmail(String email) {
        Document doc = users.find(Filters.eq("email", email)).first();
        if (doc == null) return Optional.empty();
        User u = new User();
        u.setId(doc.getObjectId("_id").toHexString());
        u.setEmail(doc.getString("email"));
        u.setPasswordHash(doc.getString("passwordHash"));
        u.setName(doc.getString("name"));
        return Optional.of(u);
    }

    public User create(User user) {
        Document doc = new Document()
                .append("email", user.getEmail())
                .append("passwordHash", user.getPasswordHash())
                .append("name", user.getName());
        users.insertOne(doc);
        ObjectId id = doc.getObjectId("_id");
        user.setId(id.toHexString());
        return user;
    }

    public Optional<User> findById(String id) {
        Document doc = users.find(Filters.eq("_id", new ObjectId(id))).first();
        if (doc == null) return Optional.empty();
        User u = new User();
        u.setId(doc.getObjectId("_id").toHexString());
        u.setEmail(doc.getString("email"));
        u.setPasswordHash(doc.getString("passwordHash"));
        u.setName(doc.getString("name"));
        return Optional.of(u);
    }

    public void close() {
        client.close();
    }
}