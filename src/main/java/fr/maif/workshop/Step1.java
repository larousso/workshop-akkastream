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
        this.jokeService = JokeService.localJokeService();
        this.system = ActorSystem.create();
    }

    public void run() {
        //
        // Filtrer pour garder les non explicit
        // Doubler chaque catégorie
        // Récupérer 1 blague par catégorie avec 5 requêtes en parallèle
        // Garder uniquement l'attribut blague (value)
        // Concaténer toutes les blagues
        Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"));

    }

}
