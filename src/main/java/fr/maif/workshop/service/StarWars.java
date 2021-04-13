package fr.maif.workshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.maif.json.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static fr.maif.json.JsonRead.__;
import static fr.maif.json.JsonRead._list;
import static fr.maif.json.JsonRead._opt;
import static fr.maif.workshop.service.StarWarsCharacter.readCharacter;
import static java.util.function.Function.identity;

public class StarWars {

    private final static HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public static CompletionStage<List<StarWarsCharacter>> getPage(Integer pageNumber) {
        var request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create("https://swapi.dev/api/people/?page=" + pageNumber))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    JsonNode json = Json.parse(response.body());
                    return Json.fromJson(json, _opt("results", _list(readCharacter()))).fold(
                            errs -> CompletableFuture.failedFuture(new RuntimeException("parse error " + errs)),
                            ok -> CompletableFuture.completedStage(ok.toList().flatMap(identity()).toJavaList())
                    );
                });
    }
}
