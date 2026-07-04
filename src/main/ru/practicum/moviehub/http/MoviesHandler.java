package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.CreateMovieRequest;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store;
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        String[] parts = path.split("/");
        System.out.println("Началась обработка " + method + " /movies запроса от клиента");
        if (method.equalsIgnoreCase("GET")) {

            if (query != null && query.startsWith("year=")) {
                hadleGetYearById(ex);
                return;
            }

            if (parts.length == 3) {
                handleGetById(ex);
                return;
            }
            String json = gson.toJson(store.getAllMovies());
            sendJson(ex, 200, json);
            return;

        }
        if (method.equalsIgnoreCase("POST")) {
            handlePost(ex);
            return;
        }
        if (method.equalsIgnoreCase("DELETE")) {
            handleDeleteById(ex);
            return;
        }


    }

    private void handlePost(HttpExchange ex) throws IOException {
        List<String> errors = new ArrayList<>();
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            Gson gson = new Gson();
            CreateMovieRequest req = gson.fromJson(body, CreateMovieRequest.class);
            if (req.title == null || req.title.isBlank()) {
                errors.add("Tittle не может быть пустым");

            }
            if (req.title.length() > 100) {
                errors.add("Tittle слишком длинный");

            }
            if (req.year < 1888 || req.year > LocalDate.now().getYear()) {
                errors.add("Год должен быть между 1888 и 2026");

            }
            if (ex.getRequestHeaders().getFirst("Content-Type") == null || !ex.getRequestHeaders().getFirst("Content-Type").equals("application/json")) {
                sendError(ex, 415, new ErrorResponse("Не поддерживаемый формат", List.of("Content-Type должен быть application/json")));

            }
            if (!errors.isEmpty()) {
                sendError(ex, 400, new ErrorResponse("Ошибка валидации", errors));
                return;
            }

            Movie created = store.addMovie(req.title, req.year);
            sendJson(ex, 201, gson.toJson(created));

        } catch (Exception e) {
            sendError(ex, 400, new ErrorResponse("Invalid JSON", List.of("Request body is not valid JSON")));
        }
    }

    private void handleGetById(HttpExchange ex) throws IOException {
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            CreateMovieRequest req = gson.fromJson(body, CreateMovieRequest.class);
            String[] parts = ex.getRequestURI().getPath().split("/");
            int id = Integer.parseInt(parts[2]);
            if (id > 0 && store.getListOfMovie().containsKey(id)) {
                Movie movie = store.getMovieById(id);
                sendJson(ex, 200, gson.toJson(movie));
            }//Доделать функцию
            else {
                sendError(ex, 404, new ErrorResponse("Фильм не найден"));
            }
        } catch (NumberFormatException e) {
            sendError(ex, 400, new ErrorResponse("Bad Request", List.of("Некорректный ID")));
        }
    }

    private void handleDeleteById(HttpExchange ex) throws IOException {
        try {
            String[] parts = ex.getRequestURI().getPath().split("/");
            int id = Integer.parseInt(parts[2]);
            if (id > 0 && store.getListOfMovie().containsKey(id)) {
                store.deleteMovieById(id);
                sendNoContent(ex);
            }//Доделать функцию
            else {
                sendError(ex, 404, new ErrorResponse("Фильм не найден"));
            }
        } catch (NumberFormatException e) {
            sendError(ex, 400, new ErrorResponse("Bad Request", List.of("Некорректный ID")));
        }
    }

    private void hadleGetYearById(HttpExchange ex) throws IOException {
        try {
            String querry = ex.getRequestURI().getQuery();
            String yearString = querry.substring("year=".length());
            int year = Integer.parseInt(yearString);
            List<Movie> listOfMovie = store.getMovieByYear(year);
            sendJson(ex, 200, gson.toJson(listOfMovie));
        } catch (NumberFormatException e) {
            sendError(ex, 400, new ErrorResponse("Bad Request", List.of("Некорректный параметр запроса 'year'")));
        }
    }


}
