package com.github.pluraliseseverythings.conditio;

import com.github.pluraliseseverythings.conditio.config.ConditioConfiguration;
import com.github.pluraliseseverythings.conditio.core.StatsComputer;
import com.github.pluraliseseverythings.conditio.db.ConditionStatsDAO;
import com.github.pluraliseseverythings.conditio.resources.ConditionResource;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import pluraliseseverythings.events.EventServiceConsumer;
import pluraliseseverythings.events.KafkaEventServiceConsumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

import javax.ws.rs.client.Client;
import java.util.concurrent.Executors;

public class ConditioApplication extends Application<ConditioConfiguration> {

    public static final String ADD_CONDITION = "add_condition";

    public static void main(final String[] args) throws Exception {
        new ConditioApplication().run(args);
    }

    @Override
    public String getName() {
        return "conditio";
    }

    @Override
    public void initialize(final Bootstrap<ConditioConfiguration> bootstrap) {
    }

    @Override
    public void run(final ConditioConfiguration configuration,
                    final Environment environment) {
        EventServiceConsumer eventServiceProducer = configureKafka(configuration);
        Pool<Jedis> jedisPool = configureRedis(configuration);
        ConditionStatsDAO conditionStatsDAO = new ConditionStatsDAO(jedisPool);
        StatsComputer statsComputer = new StatsComputer(conditionStatsDAO);
        eventServiceProducer.consume(ADD_CONDITION, statsComputer.eventConsumer(),
                Executors.newFixedThreadPool(configuration.getEventExecutionThreads()));
        final Client mediClient = new JerseyClientBuilder(environment)
                .using(configuration.getMediClientConfiguration())
                .build(getName());

        environment.jersey().register(new ConditionResource(conditionStatsDAO, mediClient, configuration.getMediClientURI()));
    }

    private Pool<Jedis> configureRedis(ConditioConfiguration configuration) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        return new JedisPool(poolConfig, configuration.getRedisHost());
    }

    private KafkaEventServiceConsumer configureKafka(ConditioConfiguration configuration) {
        return new KafkaEventServiceConsumer(new KafkaConsumer<>(configuration.getKafkaConfiguration().consumer));
    }
}
