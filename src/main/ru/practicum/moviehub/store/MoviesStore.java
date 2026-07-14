package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    private int nextId = 1;
    private final Map<Integer, Movie> listOfMovie = new HashMap<>();

    public Movie addMovie(String title, int year) {
        Movie movie = new Movie(nextId, year, title);
        listOfMovie.put(nextId, movie);
        nextId++;
        return movie;

    }

    public Collection<Movie> getAllMovies() {
        return listOfMovie.values();
    }

    public Movie getMovieById(int id) {
        if (!listOfMovie.containsKey(id)) {
            return null;
        }
        return listOfMovie.get(id);
    }

    public boolean deleteMovieById(int id) {
        if (!listOfMovie.containsKey(id)) {
            return false;
        }
        listOfMovie.remove(id);
        return true;
    }

    public List<Movie> getMovieByYear(int year) {
        return listOfMovie.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public void clear() {
        listOfMovie.clear();
        nextId = 1;
    }

    public Map<Integer, Movie> getListOfMovie() {
        return listOfMovie;
    }


}

