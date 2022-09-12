package com.cladd.entities.api;

import com.cladd.entities.model.Pieza;

import java.util.List;

public class Finder {
    private String id;
    private String Tags;
    private List<String> TagsList;
    private List<List<Pieza>> Productos;

    public Finder(String id, String tags, List<String> tagsList, List<List<Pieza>> productos) {
        this.id = id;
        Tags = tags;
        TagsList = tagsList;
        Productos = productos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTags() {
        return Tags;
    }

    public void setTags(String tags) {
        Tags = tags;
    }

    public List<String> getTagsList() {
        return TagsList;
    }

    public void setTagsList(List<String> tagsList) {
        TagsList = tagsList;
    }

    public List<List<Pieza>> getProductos() {
        return Productos;
    }

    public void setProductos(List<List<Pieza>> productos) {
        Productos = productos;
    }
}
