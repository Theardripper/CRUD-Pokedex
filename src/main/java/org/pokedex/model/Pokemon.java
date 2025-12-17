package org.pokedex.model;


public class Pokemon {
    private String id;
    private String name;
    private String type;
    private Double weight;
    private Double height;
    private Boolean catched;

    public Pokemon(){

    }

    public Pokemon(String id, String name, String type, Double weight, Double height, Boolean catched) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.height = height;
        this.catched = catched;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public boolean isCatched() {
        return catched;
    }

    public void setCatched(Boolean catched) {
        this.catched = catched;
    }

    public String toString(){
        return name + ", " + type + ", "  + weight + ", " + height + ", " + catched;
    }
}
