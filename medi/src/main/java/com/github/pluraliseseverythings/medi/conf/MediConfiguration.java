package com.github.pluraliseseverythings.medi.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import pluraliseseverythings.events.util.KafkaConfiguration;

public class MediConfiguration extends Configuration {
    @JsonProperty
    public KafkaConfiguration kafkaConfiguration;

    @JsonProperty
    public String redisHost;

    @JsonProperty
    private int maxPatientsPerDoctor = 20;
    private int maxDoctorsPerPatient = 2;

    public KafkaConfiguration getKafkaConfiguration() {
        return kafkaConfiguration;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getMaxPatientsPerDoctor() {
        return maxPatientsPerDoctor;
    }

    public void setMaxPatientsPerDoctor(int maxPatientsPerDoctor) {
        this.maxPatientsPerDoctor = maxPatientsPerDoctor;
    }

    public int getMaxDoctorsPerPatient() {
        return maxDoctorsPerPatient;
    }

    public void setMaxDoctorsPerPatient(int maxDoctorsPerPatient) {
        this.maxDoctorsPerPatient = maxDoctorsPerPatient;
    }
}
