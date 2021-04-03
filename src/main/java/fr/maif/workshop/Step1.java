package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import fr.maif.workshop.service.JokeService;

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
        Source.range(0, 20);
        // Récupérer les blagues comme dans step 0
    }

}
