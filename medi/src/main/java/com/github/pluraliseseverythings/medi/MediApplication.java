package com.github.pluraliseseverythings.medi;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.pluraliseseverythings.medi.conf.MediConfiguration;
import com.github.pluraliseseverythings.medi.db.ConditionDAO;
import com.github.pluraliseseverythings.medi.db.ConsultationDAO;
import com.github.pluraliseseverythings.medi.db.PersonDAO;
import com.github.pluraliseseverythings.medi.exception.mappers.DomainConstraintViolatedMapper;
import com.github.pluraliseseverythings.medi.resources.ConditionResource;
import com.github.pluraliseseverythings.medi.resources.ConsultationResource;
import com.github.pluraliseseverythings.medi.resources.DoctorResource;
import com.github.pluraliseseverythings.medi.resources.PatientResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.kafka.clients.producer.KafkaProducer;
import pluraliseseverythings.events.KafkaEventServiceProducer;
import pluraliseseverythings.events.EventServiceProducer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

public class MediApplication extends Application<MediConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MediApplication().run(args);
    }

    @Override
    public String getName() {
        return "medi";
    }

    @Override
    public void initialize(final Bootstrap<MediConfiguration> bootstrap) {
        bootstrap.getObjectMapper().registerModules(new Jdk8Module());
    }

    @Override
    public void run(final MediConfiguration configuration,
                    final Environment environment) {
        EventServiceProducer eventServiceProducer = configureKafka(configuration);
        Pool<Jedis> jedisPool = configureRedis(configuration);
        PersonDAO personDAO = new PersonDAO(jedisPool, configuration.getMaxPatientsPerDoctor(), configuration.getMaxDoctorsPerPatient());
        ConditionDAO conditionDAO = new ConditionDAO(jedisPool);
        ConsultationDAO consultationDAO = new ConsultationDAO(jedisPool);

        environment.jersey().register(new DomainConstraintViolatedMapper());

        environment.jersey().register(new PatientResource(personDAO, eventServiceProducer));
        environment.jersey().register(new DoctorResource(personDAO, eventServiceProducer));
        environment.jersey().register(new ConsultationResource(consultationDAO, eventServiceProducer));
        environment.jersey().register(new ConditionResource(conditionDAO, eventServiceProducer));
    }

    private Pool<Jedis> configureRedis(MediConfiguration configuration) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        // TODO configure from yml
        return new JedisPool(poolConfig, configuration.getRedisHost());
    }

    private KafkaEventServiceProducer configureKafka(MediConfiguration configuration) {
        return new KafkaEventServiceProducer(new KafkaProducer(configuration.getKafkaConfiguration().producer));
    }

}
