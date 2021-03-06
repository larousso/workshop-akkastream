package fr.maif.workshop;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.kafka.CommitterSettings;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.ProducerMessage;
import akka.kafka.ProducerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Committer;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Producer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import fr.maif.workshop.service.Category;
import fr.maif.workshop.service.JokeService;
import fr.maif.workshop.utils.KafkaSettings;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.file.Path;
import java.util.Objects;

public class Step2 {

    private ProducerSettings<String, Category> producerSettings;
    private ConsumerSettings<String, Category> consumerSettings;
    private String topicName;

    public static class ProducerMain {
        public static void main(String[] args) {
            Step2 step2 = new Step2();
            step2.runProducer();
        }
    }

    public static class ConsumerMain {
        public static void main(String[] args) {
            Step2 step2 = new Step2();
            step2.runConsumer();
        }
    }


    private final JokeService jokeService;
    private final ActorSystem system;

    public Step2() {
        this.jokeService = JokeService.localJokeService();
        this.system = ActorSystem.create();
        this.topicName = "test-ade";
        this.producerSettings = KafkaSettings.localProducerSettings(system);
        this.consumerSettings = KafkaSettings.localConsumerSettings(system, "test");
    }

    public void runProducer() {
        /*
        Lire les catégories depuis un fichier csv et les envoyer dans kafka :

         * Pour lire depuis un fichier, il faut utiliser `FileIO`.
         * Pour envoyer dans kafka il y'a 2 approches
           * `Flow` : `Producer.flexFlow`
           * `Sink` : `Producer.plainSink`

         */
        FileIO.fromPath(Path.of("src/main/resources/categories.csv"));

    }

    public void runConsumer() {
        /*
            Lire les catégories depuis kafka, rechercher des blagues et stocker les blagues dans postgresql et commiter une fois ok :

             * `Consumer.committableSource` pour lire un topic
             * Utiliser le `JokeService.asyncJokeUpsert` pour stocker dans postgresql
             * `Committer.flow(settings)` ou `Committer.sink(settings)` pour commiter
             * `asSourceWithContext` pour mettre l'offset de côté
       */
        CommitterSettings settings = CommitterSettings.create(system);
        Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName));
    }


}
