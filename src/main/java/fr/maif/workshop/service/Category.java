package fr.maif.workshop.service;

import com.fasterxml.jackson.databind.node.TextNode;
import fr.maif.json.Json;
import fr.maif.json.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Optional;

import static fr.maif.json.Json.$;
import static fr.maif.json.JsonRead._string;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Category {

    public final String name;

    public static JsonFormat<Category> format = JsonFormat.of(
            _string("name").map(Category::new),
            category -> Json.obj($("name", category.name))
    );

    public static StringSerializer stringSerializer = new StringSerializer();
    public static StringDeserializer stringDeserializer = new StringDeserializer();

    public static Serializer<Category> kafkaSerializer = (s, category) ->
            stringSerializer.serialize(s, Json.stringify(Json.toJson(category, format)));

    public static Deserializer<Optional<Category>> kafkaDeserializer = (s, bytes) -> {
        try {
            String strValue = stringDeserializer.deserialize(s, bytes);
            return Json.fromJson(Json.parse(strValue), format).fold(
                    err -> {
                        System.err.println(strValue + " - " + err);
                        return Optional.empty();
                    },
                    Optional::ofNullable
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    };
}
