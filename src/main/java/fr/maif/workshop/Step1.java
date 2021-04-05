package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.JavaFlowSupport;
import fr.maif.workshop.service.JokeService;

import java.util.List;

import static akka.stream.javadsl.JavaFlowSupport.*;

public class Step1 {

    public static void main(String[] args) {
        new Step1().run();
    }

    private final JokeService jokeService;
    private final ActorSystem system;

    public Step1() {
        this.jokeService = new JokeService();
        this.system = ActorSystem.create();
    }

    public void run() {
        //
        Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"))
                .to(Sink.asPublisher(AsPublisher.WITH_FANOUT));
        // Filtrer pour garder les non explicit
        // Doubler chaque catégorie
        // Récupérer 1 blague par catégorie avec 5 requêtes en parallèle
        // Garder uniquement l'attribut blague (value)
        // Concaténer toutes les blagues
    }

}
