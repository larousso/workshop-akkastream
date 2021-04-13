package fr.maif.workshop.service;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.QueryResult;
import fr.maif.workshop.utils.PgConfig;
import io.vavr.collection.Stream;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.array;
import static org.jooq.impl.DSL.val;

public class JokeService implements Closeable {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final HttpClient client;
    private final DataSource dataSource;
    private final PgAsyncPool pgAsyncPool;
    private final DSLContext dslContext;
    private final PgConfig pgConfig;

    private JokeService(PgConfig pgConfig, PgAsyncPool pgAsyncPool, DataSource dataSource) {
        this(HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .build(),
                pgConfig,
                dataSource,
                pgAsyncPool
        );
    }

    private JokeService(HttpClient client, PgConfig pgConfig, DataSource dataSource, PgAsyncPool pgAsyncPool) {
        this.client = client;
        this.pgConfig = pgConfig;
        this.dataSource = dataSource;
        this.pgAsyncPool = pgAsyncPool;
        this.dslContext = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    public static JokeService localJokeService() {
        PgConfig pgConfig = new PgConfig();
        return new JokeService(pgConfig, pgConfig.localAsyncPool(), pgConfig.localDataSource()).initDatabase();
    }

    public static JokeService cleverJokeService() {
        PgConfig pgConfig = new PgConfig();
        return new JokeService(pgConfig, pgConfig.cleverAsyncPool(), pgConfig.cleverDataSource()).initDatabase();
    }

    public JokeService initDatabase() {
        dslContext.execute("""
                create table if not exists joke(
                    id text primary key not null,
                    icon_url text,
                    url text,
                    value text,
                    categories text[]    
                )               
                """);
        return this;
    }

    public CompletionStage<Joke> getJoke(String id) {
        System.out.println(id + " -> start");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.chucknorris.io/jokes/random"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jokeJsonString -> {
                    Joke joke = Joke.fromJsonString(jokeJsonString);
                    System.out.println(id + " [" + Thread.currentThread().getName() + "]" + " -> " + joke);
                    return joke;
                });
    }

    public CompletionStage<Joke> getRandomJokeByCategory(String category) {
        System.out.println(category + " -> start");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.chucknorris.io/jokes/random?category=%s".formatted(category)))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jokeJsonString -> {
                    Joke joke = Joke.fromJsonString(jokeJsonString);
                    System.out.println(category + " [" + Thread.currentThread().getName() + "]" + " -> " + joke);
                    return joke;
                });
    }

    public Source<Joke, NotUsed> streamRandomJokeByCategory(String category) {
        return Source.repeat("dummy")
                .mapAsync(1, __ -> getRandomJokeByCategory(category));
    }

    public Joke blockingJokeUpsert(Joke joke) {
        return upsertQuery(dslContext, joke)
                .fetchAny(r -> new Joke(
                        r.get(0, String.class),
                        r.get(1, String.class),
                        r.get(2, String.class),
                        r.get(3, String.class),
                        Stream.of(r.get(4, String[].class)).map(Category::new).collect(Collectors.toList())
                ));
    }

    public CompletionStage<Joke> asyncJokeUpsertRandomCrash(Joke joke) {
        int count = counter.incrementAndGet();
        if (count > 1 && count % 5 == 0) {
            return CompletableFuture.failedFuture(new RuntimeException("Chérie ça va couper"));
        } else {
            return asyncJokeUpsert(joke);
        }
    }

    public CompletionStage<Joke> asyncJokeUpsert(Joke joke) {
        return pgAsyncPool
                .queryOne(dsl -> upsertQuery(dsl, joke))
                .map(mayBeRecord -> {
                    System.out.println(mayBeRecord);
                    QueryResult queryResult = mayBeRecord.get();
                    return new Joke(
                            queryResult.get(0, String.class),
                            queryResult.get(1, String.class),
                            queryResult.get(2, String.class),
                            queryResult.get(3, String.class),
                            Stream.of(queryResult.get(4, String[].class)).map(Category::new).collect(Collectors.toList())
                    );
                })
                .onFailure(e -> e.printStackTrace())
                .toCompletableFuture();
    }

    private ResultQuery<Record> upsertQuery(DSLContext dslContext, Joke joke) {
        return dslContext.resultQuery("""
                            insert into joke(id, icon_url, url, value, categories) values ({0}, {1}, {2}, {3}, {4}) 
                            ON CONFLICT ON CONSTRAINT joke_pkey DO UPDATE 
                                SET icon_url = {1}, 
                                    url = {2}, 
                                    value = {3}, 
                                    categories = {4} 
                            returning id, icon_url, url, value, categories
                        """,
                val(joke.id),
                val(joke.iconUrl),
                val(joke.url),
                val(joke.value),
                array(joke.categories.stream().map(c -> c.name).toArray())
        );
    }

    @Override
    public void close() throws IOException {
        this.pgConfig.close();
    }
}
