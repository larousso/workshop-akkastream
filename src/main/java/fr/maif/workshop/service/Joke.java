package fr.maif.workshop.service;


import com.fasterxml.jackson.databind.JsonNode;
import fr.maif.json.Json;
import fr.maif.json.JsonRead;
import io.vavr.Value;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

import static fr.maif.json.JsonRead.__;
import static fr.maif.json.JsonRead._list;
import static fr.maif.json.JsonRead._string;

@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Joke {

    public final String id;
    public final String iconUrl;
    public final String url;
    public final String value;
    public final List<Category> categories;

    public static JsonRead<Joke> read =
            _string("id", Joke.builder()::id)
            .and(_string("icon_url"), JokeBuilder::iconUrl)
            .and(_string("url"), JokeBuilder::url)
            .and(_string("value"), JokeBuilder::value)
            .and(__("categories", _list(Category.format)).map(Value::toJavaList), JokeBuilder::categories)
            .map(JokeBuilder::build);

    public static Joke fromJson(JsonNode json) {
        return Json.fromJson(json, read).get();
    }
    public static Joke fromJsonString(String json) {
        return Json.fromJson(Json.parse(json), read).get();
    }
}
