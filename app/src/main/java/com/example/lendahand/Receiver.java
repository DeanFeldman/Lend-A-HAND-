package com.example.lendahand;

public class Receiver {

    public int userId;
    public int requestId;
    public String name;
    public String email;
    public String biography;
    public int quantityNeeded;
    public int quantityToDonate = 0;

    public Receiver(int requestId, int userId, String name, String biography, int quantityNeeded) {
        this.requestId = requestId;
        this.userId = userId;
        this.name = name;
        this.biography = biography;
        this.quantityNeeded = quantityNeeded;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
