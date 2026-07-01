package ru.practicum.moviehub.model;


import java.util.Objects;

public class Movie {
    private int id;
    private int year;
    private String title;


    public Movie(int id, int year, String title) {
        this.id = id;
        this.year = year;
        this.title = title;
    }

    public int getYear() {
        return year;
    }


    public String getTitle() {
        return title;
    }


    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Movie movie = (Movie) object;
        return getId() == movie.getId() && getYear() == movie.getYear() && Objects.equals(getTitle(), movie.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getYear(), getTitle());
    }
}