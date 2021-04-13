package fr.maif.workshop.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;

import java.util.Properties;

import static io.vavr.API.Option;

public class PgConfig {

    private Vertx vertx;
    private PgPool client;
    private ReactivePgAsyncPool reactivePgAsyncPool;
    private HikariDataSource hikariDataSource;
    private Config config = ConfigFactory.load();
    private Config postgresqlConfig = config.getConfig("postgresql");

    public DataSource cleverDataSource() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.portNumber", postgresqlConfig.getString("port"));
        props.setProperty("dataSource.serverName", postgresqlConfig.getString("host"));
        props.setProperty("dataSource.user", postgresqlConfig.getString("user"));
        props.setProperty("dataSource.password", postgresqlConfig.getString("password"));
        props.setProperty("dataSource.databaseName", postgresqlConfig.getString("dbName"));
        HikariConfig hikariConfig = new HikariConfig(props);
        return new HikariDataSource(hikariConfig);
    }

    public DataSource localDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("postgresql://localhost:5432/workshop");
        config.setUsername("workshop");
        config.setPassword("workshop");
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        hikariDataSource = new HikariDataSource(config);
        return hikariDataSource;
    }

    public PgAsyncPool localAsyncPool() {
        this.close();
        int port = 5432;
        DefaultConfiguration jooqConfig = new DefaultConfiguration();
        jooqConfig.setSQLDialect(SQLDialect.POSTGRES);
        PgConnectOptions options = new PgConnectOptions()
                .setPort(port)
                .setHost("localhost")
                .setDatabase("workshop")
                .setUser("workshop")
                .setPassword("workshop");
        PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
        this.vertx = Vertx.vertx();
        this.client = PgPool.pool(vertx, options, poolOptions);
        this.reactivePgAsyncPool = new ReactivePgAsyncPool(client, jooqConfig);
        return reactivePgAsyncPool;
    }


    public PgAsyncPool cleverAsyncPool() {
        this.close();
        Config config = ConfigFactory.load();
        Config postgresqlConfig = config.getConfig("postgresql");
        DefaultConfiguration jooqConfig = new DefaultConfiguration();
        jooqConfig.setSQLDialect(SQLDialect.POSTGRES);
        PgConnectOptions options = new PgConnectOptions()
                .setPort(postgresqlConfig.getInt("port"))
                .setHost(postgresqlConfig.getString("host"))
                .setDatabase(postgresqlConfig.getString("dbName"))
                .setUser(postgresqlConfig.getString("username"))
                .setPassword(postgresqlConfig.getString("password"));
        PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
        this.vertx = Vertx.vertx();
        this.client = PgPool.pool(vertx, options, poolOptions);
        this.reactivePgAsyncPool = new ReactivePgAsyncPool(client, jooqConfig);
        return reactivePgAsyncPool;
    }

    public void close() {
        Option(this.client).forEach(SqlClient::close);
        Option(this.vertx).forEach(Vertx::close);
        Option(this.hikariDataSource).forEach(HikariDataSource::close);
    }

}
