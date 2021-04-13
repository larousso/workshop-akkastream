package fr.maif.workshop.utils;

import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.ProducerSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.maif.workshop.service.Category;
import lombok.SneakyThrows;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;

public class KafkaSettings {

    private static Config config = ConfigFactory.load();
    private static Config kafkaConfig = config.getConfig("kafka");

    public static ConsumerSettings<String, Category> localConsumerSettings(ActorSystem system, String groupId) {
        return ConsumerSettings.create(system, Category.stringDeserializer, Category.kafkaDeserializer)
                .withBootstrapServers("localhost:29092")
                .withGroupId(groupId)
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    }

    @SneakyThrows
    public static ConsumerSettings<String, Category> mapConsumerSettings(ActorSystem system, String groupId) {
        return ConsumerSettings.create(system, Category.stringDeserializer, Category.kafkaDeserializer)
                .withGroupId(groupId)
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                .withBootstrapServers(kafkaConfig.getString("bootstrapServers"))
                .withProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
                .withProperty(BrokerSecurityConfigs.SSL_CLIENT_AUTH_CONFIG, "required")
                .withProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, null)
                .withProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
                .withProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "src/main/resources/localhost-dev.p12")
                .withProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaConfig.getString("password"));
    }

    public static ProducerSettings<String, Category> localProducerSettings(ActorSystem system) {
        return ProducerSettings.create(system, Category.stringSerializer, Category.kafkaSerializer)
                .withBootstrapServers("localhost:29092");
    }

    @SneakyThrows
    public static ProducerSettings<String, Category> mapProducerSettings(ActorSystem system) {
        return ProducerSettings.create(system, Category.stringSerializer, Category.kafkaSerializer)
                .withBootstrapServers(kafkaConfig.getString("bootstrapServers"))
                .withProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
                .withProperty(BrokerSecurityConfigs.SSL_CLIENT_AUTH_CONFIG, "required")
                .withProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, null)
                .withProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
                .withProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "src/main/resources/localhost-dev.p12")
                .withProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaConfig.getString("password"));
    }

}
