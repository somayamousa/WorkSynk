package com.example.worksyck;
public class Designations {
    private String id;
    private String name;
    public Designations(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public Designations(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return  name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
