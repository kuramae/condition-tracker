package com.github.pluraliseseverythings.conditio.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import pluraliseseverythings.events.util.KafkaConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class ConditioConfiguration extends Configuration {
    @JsonProperty
    public KafkaConfiguration kafkaConfiguration;

    @JsonProperty
    public String redisHost;

    @JsonProperty
    private int eventExecutionThreads = 5;

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration mediClientConfiguration = new JerseyClientConfiguration();

    @JsonProperty
    private URI mediClientURI;

    public JerseyClientConfiguration getMediClientConfiguration() {
        return mediClientConfiguration;
    }

    public KafkaConfiguration getKafkaConfiguration() {
        return kafkaConfiguration;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getEventExecutionThreads() {
        return eventExecutionThreads;
    }

    public URI getMediClientURI() {
        return mediClientURI;
    }
}
