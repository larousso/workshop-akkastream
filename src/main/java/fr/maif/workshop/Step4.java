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

public class Step4 {

    private ProducerSettings<String, Category> producerSettings;
    private ConsumerSettings<String, Category> consumerSettings;
    private String topicName;

    public static class ProducerMain {
        public static void main(String[] args) {
            Step4 step2 = new Step4();
            step2.runProducer();
        }
    }

    public static class ConsumerMain {
        public static void main(String[] args) {
            Step4 step2 = new Step4();
            step2.runConsumer();
        }
    }


    private final JokeService jokeService;
    private final ActorSystem system;

    public Step4() {
        this.jokeService = JokeService.localJokeService();
        this.system = ActorSystem.create();
        this.topicName = "test-ade";
        this.producerSettings = KafkaSettings.localProducerSettings(system);
        this.consumerSettings = KafkaSettings.localConsumerSettings(system, "test");
    }

    public void runProducer() {
        FileIO.fromPath(Path.of("src/main/resources/categories.csv"))
                .via(Framing.delimiter(ByteString.fromString("\n"), 10000, FramingTruncation.ALLOW))
                .map(ByteString::utf8String)
                .map(Category::new)
                .map(category -> ProducerMessage.single(new ProducerRecord<>("test-ade", category.name, category)))
                .via(Producer.flexiFlow(producerSettings))
                .runWith(Sink.ignore(), system)
                .whenComplete((__, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Producer is done");
                    }
                });

    }

    public void runConsumer() {
        CommitterSettings settings = CommitterSettings.create(system);
        Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName))
                .asSourceWithContext(ConsumerMessage.CommittableMessage::committableOffset)
                .filter(m -> Objects.nonNull(m.record().value()))
                .mapAsync(1, m -> jokeService.getRandomJokeByCategory(m.record().value().name))
                // TODO : remplacer par la version bloquante
                .mapAsync(1, joke -> jokeService.asyncJokeUpsert(joke))
                .asSource()
                .map(Pair::second)
                .via(Committer.flow(settings))
                .runWith(Sink.ignore(), system)
                .whenComplete((__, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Consumer is done");
                    }
                });
    }


}
