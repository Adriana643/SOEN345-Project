package com.soen345.Ticket.Reservation.Application.dto;

public class FirebaseAuthRequest {
    private String idToken;
    private String role;
    private String name;

    public String getIdToken() { return idToken; }
    public String getRole() { return role; }
    public String getName() { return name; }
}