package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import fr.maif.workshop.service.StarWars;

import java.util.Objects;
import java.util.Optional;

public class Step5 {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        Source
                .unfoldAsync(1, page ->
                        StarWars.getPage(page).thenApply(starWarsCharacters -> {
                            if (starWarsCharacters.isEmpty()) {
                                return Optional.empty();
                            } else {
                                return Optional.of(Pair.create(page + 1, starWarsCharacters));
                            }
                        })
                )
                .mapConcat(l -> l)
                .runWith(Sink.foreach(System.out::println), system)
                .whenComplete((r, e) -> {
                    if (Objects.nonNull(e)) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Done");
                    }
                    system.terminate();
                });

    }


}
