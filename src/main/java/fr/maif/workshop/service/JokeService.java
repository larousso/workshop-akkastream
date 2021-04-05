package fr.maif.workshop.service;

import akka.NotUsed;
import akka.stream.javadsl.Source;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class JokeService {

    private final HttpClient client;

    public JokeService() {
        this(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build()
        );
    }

    public JokeService(HttpClient client) {
        this.client = client;
    }

    public CompletionStage<Joke> getJoke(String id) {
        System.out.println(id + " -> start");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.chucknorris.io/jokes/random"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jokeJsonString -> {
                    Joke joke = Joke.fromJsonString(jokeJsonString);
                    System.out.println(id + " [" + Thread.currentThread().getName() + "]" +  " -> " + joke);
                    return joke;
                });
    }

    public CompletionStage<Joke> getRandomJokeByCategory(String category) {
        System.out.println(category + " -> start");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.chucknorris.io/jokes/random?category=%s".formatted(category)))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jokeJsonString -> {
                    Joke joke = Joke.fromJsonString(jokeJsonString);
                    System.out.println(category + " [" + Thread.currentThread().getName() + "]" +  " -> " + joke);
                    return joke;
                });
    }
    public Source<Joke, NotUsed> streamRandomJokeByCategory(String category) {
        return Source.repeat("dummy")
                .mapAsync(1, __ -> getRandomJokeByCategory(category));
    }
}
