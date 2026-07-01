package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    private int nextId = 1;
    Map<Integer, Movie> listOfMovie = new HashMap<>();

    public void addMovie(String title, int year) {
        listOfMovie.put(nextId, new Movie(nextId, year, title));
        nextId++;

    }

    public Collection<Movie> getAllMovies() {
        return listOfMovie.values();
    }

    public Movie getMovieId(int id) {
        if (!listOfMovie.containsKey(id)) {
            return null;
        }
        return listOfMovie.get(id);
    }

    public boolean deleteMovieId(int id) {
        if (!listOfMovie.containsKey(id)) {
            return false;
        }
        listOfMovie.remove(id);
        return true;
    }
    public List<Movie> getMovieByYear(int year){
        return listOfMovie.values().stream()
                .filter(movie -> movie.getYear()==year)
                .collect(Collectors.toList());
    }
}

