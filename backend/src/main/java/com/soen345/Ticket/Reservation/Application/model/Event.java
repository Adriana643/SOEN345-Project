package com.soen345.Ticket.Reservation.Application.model;

public class Event {
    private String id;
    private String title;
    private String date;
    private String location;
    private String category;
    private String description;
    private String status; // active or cancelled

    public Event() {}


    public Event(String id, String title, String date, String location,
                 String category, String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
        this.description = description;
        this.status = "active";
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }


    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
}

