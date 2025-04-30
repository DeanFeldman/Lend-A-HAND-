package com.example.lendahand;

public class Receiver {

    public int userId;
    public String name;
    public String biography;
    public int quantityNeeded;
    public int quantityToDonate = 0;

    public Receiver(int userId, String name, String bio, int needed) {
        this.userId = userId;
        this.name = name;
        this.biography = bio;
        this.quantityNeeded = needed;
    }

}
