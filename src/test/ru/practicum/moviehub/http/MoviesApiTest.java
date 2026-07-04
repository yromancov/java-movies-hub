package ru.practicum.moviehub.http;


import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static final Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        server.stop();
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    private static HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new AssertionError("HTTP req fail: " + request.uri(), e);
        }
    }

    private static HttpRequest get(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .GET()
                .build();
    }

    private static HttpRequest delete(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .DELETE()
                .build();
    }

    private static HttpRequest post(String path, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private static String movieJson(String title, int year) {
        return "{"
                + "\"title\": \"" + title + "\","
                + "\"year\": " + year +
                "}";
    }

    private static Movie parseMovies(String json) {
        return gson.fromJson(json, Movie.class);
    }

    private static List<Movie> parseMoviesList(String json) {
        return gson.fromJson(json, new ListOfMoviesTypeToken().getType());
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        // Создайте и запустите MoviesServer
        HttpResponse<String> resp = send(get("/movies"));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenStoreHasMovies_returnMovies() throws Exception {
        send(post("/movies", movieJson("Interstellar", 2014)));
        send(post("/movies", movieJson("Inception", 2010)));

        HttpResponse<String> response = send(get("/movies"));

        assertEquals(200, response.statusCode());

        List<Movie> movies = parseMoviesList(response.body());

        assertEquals(2, movies.size());
    }

    @Test
    void postMovie_whenTitleEmpty_returns400() throws Exception {
        HttpResponse<String> response = send(
                post("/movies", movieJson("", 2014))
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenTitleOver100symmbols_returns400() throws Exception {
        String longTitle = "A".repeat(120);

        HttpResponse<String> response = send(
                post("/movies", movieJson(longTitle, 2014))
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenYearBelow1888_returns400() throws Exception {
        HttpResponse<String> response = send(
                post("/movies", movieJson("Test", 1879))
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenYearOverDateNow_returns400() throws Exception {
        HttpResponse<String> response = send(
                post("/movies", movieJson("Test", 2028))
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenContnet_TypeNotCorrect_returns415() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "alication/json")
                .POST(HttpRequest.BodyPublishers.ofString(movieJson("Test", 2026)))
                .build();

        HttpResponse<String> response = send(request);

        assertEquals(415, response.statusCode());
    }

    @Test
    void postMovie_return201() throws Exception {
        HttpResponse<String> response = send(
                post("/movies", movieJson("TENET", 2023))
        );

        assertEquals(201, response.statusCode());
    }

    @Test
    void getMovieById_whenMovieExists_returnsMovie() throws Exception {
        HttpResponse<String> createResponse =
                send(post("/movies", movieJson("TENET", 2023)));

        Movie created = parseMovies(createResponse.body());

        HttpResponse<String> response =
                send(get("/movies/" + created.getId()));

        assertEquals(200, response.statusCode());

        Movie found = parseMovies(response.body());

        assertEquals(created, found);
    }

    @Test
    void getMovieById_WhenMovieNotFound_returns404() throws Exception {
        HttpResponse<String> response = send(get("/movies/9999"));

        assertEquals(404, response.statusCode());
    }

    @Test
    void getMovieById_WhenIdNotNumber_returns400() throws Exception {
        HttpResponse<String> response = send(get("/movies/abc"));

        assertEquals(400, response.statusCode());
    }

    @Test
    void deleteMovie_WhenMovieNotFound_return204() throws Exception {
        HttpResponse<String> createResponse =
                send(post("/movies", movieJson("TENET", 2023)));

        Movie created = parseMovies(createResponse.body());

        HttpResponse<String> deleteResponse =
                send(delete("/movies/" + created.getId()));

        assertEquals(204, deleteResponse.statusCode());

    }

    @Test
    void deleteMovie_WhenMovieSucsflDelete_return404() throws Exception {
        HttpResponse<String> response =
                send(delete("/movies/999"));

        assertEquals(404, response.statusCode());

    }

    @Test
    void getMovieByYear_WhenYearIsCorrect_return200() throws Exception {
        HttpResponse<String> createResponse =
                send(post("/movies", movieJson("TENET", 2023)));

        Movie created = parseMovies(createResponse.body());

        HttpResponse<String> response =
                send(get("/movies?year=" + created.getYear()));

        assertEquals(200, response.statusCode());
    }

    @Test
    void getMovieByYear_WhenYearIsNotCorrect_return400() throws Exception {
        HttpResponse<String> response =
                send(get("/movies?year=SKO;IIKUF"));

        assertEquals(400, response.statusCode());

    }
}

