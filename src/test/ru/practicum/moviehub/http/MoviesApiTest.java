package ru.practicum.moviehub.http;


import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.CreateMovieRequest;
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
    private static MoviesStore store;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }
//    @BeforeEach
//    void beforeEach(){
//        store.clear();
//    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        // Создайте и запустите MoviesServer
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

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
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "appliaction/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":Interstellar,
                        "year":"2014"
                        }"""))
                .build();
        HttpRequest secondMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "appliaction/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":Inception,
                        "year":"2010"
                        }"""))
                .build();
        client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        client.send(secondMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
        List<Movie> movies = new Gson().fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, movies.size());
    }

    @Test
    void postMovie_whenTitleEmpty_returns400() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"",
                        "year":2014
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenTitleOver100symmbols_returns400() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"Un fatto di sangue nel comune di Sculiana fra due uomini per causa di una vedova. Si 
                        sospettano moventi politici. Amore-Morte-Shimmy. Lugano belle. Tarantelle. Tarallucci e vino, 
                        1978",
                        "year":2014
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, response.statusCode());


    }

    @Test
    void postMovie_whenYearBelow1888_returns400() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"Un fatto",
                        "year":1879
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenYearOverDateNow_returns400() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"Un fatto",
                        "year":2028
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, response.statusCode());
    }

    @Test
    void postMovie_whenContnet_TypeNotCorrect_returns415() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "alication/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"Un fatto",
                        "year":2026
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(415, response.statusCode());
    }

    @Test
    void postMovie_return201() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"TENNET",
                        "year":2023
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, response.statusCode());
    }

    @Test
    void getMvieById_whenMovieExists_returnsMovie() throws Exception {
        HttpRequest firstMovie = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                        "title":"TENNET",
                        "year":2023
                        }"""))
                .build();
        HttpResponse<String> response = client.send(firstMovie, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Movie created = new Gson().fromJson(response.body(), Movie.class);
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + created.getId()))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(get, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
        Movie movie = new Gson().fromJson(getResponse.body(), Movie.class);
        assertEquals(created, movie);
    }
    @Test
    void getMovieById_WhenMovieNotFound_returns404() throws Exception{
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE+"/movies/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(get, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404,response.statusCode());
    }
    @Test
    void getMovieById_WhenIdNotNumber_returns400() throws Exception{
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE+"/movies/Lll"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(get, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400,response.statusCode());
    }
}

