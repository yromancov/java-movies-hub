package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.CreateMovieRequest;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
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
        String[] parts = path.split("/");
        System.out.println("Началась обработка " + method + " /movies запроса от клиента");
        if (method.equalsIgnoreCase("GET")) {
            String json = gson.toJson(store.getAllMovies());
            sendJson(ex, 200, json);
            return;
        }
        if (method.equalsIgnoreCase("POST")) {
            handlePost(ex);
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
                errors.add("title не может быть пустым");
                return;
            }
            if (req.title.length() > 100) {
                errors.add("Tittle слишком длинный");
                return;
            }
            if (req.year < 1888 || req.year > LocalDate.now().getYear() + 1) {
                errors.add("Некорректный Year");
                return;
            }
            if (ex.getRequestHeaders().getFirst("Content-Type") == null ||
                    !ex.getRequestHeaders().getFirst("Content-Type").equals("application/json")) {
                sendError(ex, 415, new ErrorResponse("Не поддерживаеймы формат", List.of("Content-Type должен быть application/json")));
                return;
            }
            if (!errors.isEmpty()) {
                sendError(ex, 400, new ErrorResponse("Ошибка валидации", errors));
                return;
            }

            Movie created = store.addMovie(req.title, req.year);
            sendJson(ex, 201, gson.toJson(created));

        } catch (Exception e) {
            sendError(ex,
                    400,
                    new ErrorResponse("Invalid JSON", List.of("Request vody is not valid JSON")));
        }
    }


}
