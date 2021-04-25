package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import fr.maif.workshop.service.StarWars;
import fr.maif.workshop.service.StarWarsCharacter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class Step5Solution {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        /*
        Récupérer tous les personnages de star wars :

         * https://swapi.dev/api/people/?page=n
         * Utiliser `Source.unfoldAsync`

         */
        int page = 1;
        CompletionStage<List<StarWarsCharacter>> page1 = StarWars.getPage(page);

    }


}
