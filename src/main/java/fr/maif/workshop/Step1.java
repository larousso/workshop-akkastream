package fr.maif.workshop;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import fr.maif.workshop.service.Joke;
import fr.maif.workshop.service.JokeService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;


public class Step1 {

    public static void main(String[] args) {
        new Step1().runBis();
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
        CompletionStage<String> allJokes = Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"))
                .filterNot(s -> s.equals("explicit"))
                .mapConcat(s -> List.of(s, s))
                .mapAsync(2, categorie -> jokeService.getRandomJokeByCategory(categorie))
                .map(Joke::getValue)
                .intersperse("[", ",", "]")
                .scan("", String::concat)
                .map(s -> {
                    if (s.length() % 10 == 0) {
                        System.out.println("Size = "+s.length()+ " : " + s);
                    }
                    return s;
                })
                .runWith(Sink.last(), system);

        allJokes.whenComplete((jokes, exception) -> {
            if (Objects.nonNull(exception)) {
                exception.printStackTrace();
            } else {
                System.out.println("All jokes "+jokes);
            }
            system.terminate();
            try {
                jokeService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void runBis() {
        //
        // Filtrer pour garder les non explicit
        // Doubler chaque catégorie
        // Récupérer 1 blague par catégorie avec 5 requêtes en parallèle
        // Garder uniquement l'attribut blague (value)
        // Concaténer toutes les blagues

        Flow<String, String, akka.NotUsed> garderLesNonExplicits = Flow.<String>create().filterNot(s -> s.equals("explicit"));
        Flow<String, String, akka.NotUsed> doublerLesCategories = Flow.<String>create().mapConcat(s -> List.of(s, s));
        Flow<String, String, akka.NotUsed> rechercherLesBlaguesParCategorie = Flow.<String>create()
                .mapAsync(2, categorie -> jokeService.getRandomJokeByCategory(categorie))
                .map(Joke::getValue);
        Flow<String, String, akka.NotUsed> concatener = Flow.<String>create()
                .intersperse("[", ",", "]")
                .scan("", String::concat)
                .map(s -> {
                    if (s.length() % 10 == 0) {
                        System.out.println("Size = "+s.length()+ " : " + s);
                    }
                    return s;
                })
                .fold("", (__, elt) -> elt);

        CompletionStage<String> allJokes = Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"))
                .via(garderLesNonExplicits)
                .via(doublerLesCategories)
                .via(rechercherLesBlaguesParCategorie)
                .via(concatener)
                .runWith(Sink.head(), system);

        allJokes.whenComplete((jokes, exception) -> {
            if (Objects.nonNull(exception)) {
                exception.printStackTrace();
            } else {
                System.out.println("All jokes "+jokes);
            }
            system.terminate();
            try {
                jokeService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
