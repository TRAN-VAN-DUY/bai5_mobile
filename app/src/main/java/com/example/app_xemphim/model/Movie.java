package com.example.app_xemphim.model;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String genre;
    private String duration;
    private String posterUrl;
    private String description;
    private String theater;
    private int price;
    private List<String> showtimes;

    public Movie() {
        showtimes = new ArrayList<>();
    }

    public Movie(String id, String title, String genre, String duration, String posterUrl,
            String description, String theater, int price, List<String> showtimes) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.description = description;
        this.theater = theater;
        this.price = price;
        this.showtimes = showtimes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<String> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<String> showtimes) {
        this.showtimes = showtimes;
    }
}
