package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;


public class MoviesHandler extends BaseHttpHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        System.out.println("Началась обработка " + method + " /movies запроса от клиента");
        if (!method.equalsIgnoreCase("GET") || !"/movies".equals(path)) {
            sendJson(ex, 404, "[]");
            return;
        }
        sendJson(ex, 200, "[]");

    }


}
