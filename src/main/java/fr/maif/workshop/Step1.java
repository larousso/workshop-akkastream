package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import fr.maif.workshop.service.JokeService;

import java.util.List;

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
        Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"));
        // Filtrer non explicit
        // Doubler les catégories
        // Récupérer 1 blague par catégorie avec 5 requêtes en parallèle
        // Garder uniquement les blagues
        // Concatener toutes les blagues
    }

}
