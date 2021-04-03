package fr.maif.workshop;

import fr.maif.workshop.service.Joke;
import fr.maif.workshop.service.JokeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Step0 {
    public static void main(String[] args) {
        new Step0().run();
    }

    private final JokeService jokeService;

    public Step0() {
        this.jokeService = new JokeService();
    }

    public void run() {
        System.out.println("Début");
        List<CompletionStage<Joke>> allFutures = IntStream.range(0, 50)
                .boxed()
                .map(String::valueOf)
                .map(this.jokeService::getJoke)
                .collect(Collectors.toList());

        sequence(allFutures).toCompletableFuture().join();
        System.out.println("Fin");
    }

    private <V> CompletionStage<List<V>> sequence(List<CompletionStage<V>> listOfFutures) {
        return listOfFutures.stream().reduce(
                CompletableFuture.completedStage(List.of()),
                (futureList, futureValue) -> futureValue.thenCombine(futureList, (value, list) ->
                        Stream.concat(list.stream(), Stream.of(value)).collect(Collectors.toList())
                ),
                (futureList1, futureList2) -> futureList1.thenCombine(futureList2, (list1, list2) ->
                    Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList())
                ));
    }
}
