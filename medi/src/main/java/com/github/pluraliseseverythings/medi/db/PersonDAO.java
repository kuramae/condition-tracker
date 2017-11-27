package com.github.pluraliseseverythings.medi.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pluraliseseverythings.medi.api.Ids;
import com.github.pluraliseseverythings.medi.api.Person;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import com.github.pluraliseseverythings.medi.exception.InternalFormatException;
import com.github.pluraliseseverythings.medi.exception.StorageException;
import com.google.common.collect.ImmutableSet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.util.Pool;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;


public class PersonDAO {
    public static final String DOCTOR = "doctor";
    public static final String DOCTOR_PATIENTS = "doctor:patients";
    public static final String PATIENT = "patient";
    public static final String PATIENT_DOCTORS = "patient:doctors";
    public static final String PATIENT_CONDITIONS = "patient:conditions";
    public static final String EMAIL = "email";

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Pool<Jedis> jedisPool;
    private int patientsLimit;
    private int doctorsLimit;

    public PersonDAO(Pool<Jedis> jedisPool, int patientsLimit, int doctorsLimit) {
        this.jedisPool = jedisPool;
        this.patientsLimit = patientsLimit;
        this.doctorsLimit = doctorsLimit;
    }

    public Collection<Person> findPersonByName(String name) {
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.smembers(name);
        }
        try (Jedis jedis = jedisPool.getResource()) {
            ImmutableSet.Builder<Person> builder = ImmutableSet.builder();
            for (String key : keys) {
                try {
                    builder.add(objectMapper.readValue(jedis.get(key), Person.class));
                } catch (IOException e) {
                    throw new InternalFormatException("Something wrong in the db", e);
                }
            }
            return builder.build();
        }
    }

    public String insertPatient(Person person) throws StorageException, DomainConstraintViolated {
        return insertPerson(person, PATIENT, true);
    }

    public String insertDoctor(Person person) throws StorageException, DomainConstraintViolated {
        return insertPerson(person, DOCTOR, false);
    }

    // Uses optimistic concurrency control to check the set size
    public int addPatient(String doctorId, String patientId) throws DomainConstraintViolated, StorageException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.watch(Ids.id(DOCTOR, doctorId), Ids.id(PATIENT, patientId));
            Set<String> patients = jedis.smembers(Ids.id(DOCTOR_PATIENTS, doctorId));
            if (patients.size() >= patientsLimit) {
                throw new DomainConstraintViolated("Too many patients for doctor");
            }
            Set<String> doctors = jedis.smembers(Ids.id(PATIENT_DOCTORS, patientId));
            if (doctors.size() >= doctorsLimit) {
                throw new DomainConstraintViolated("Too many doctors for patient");
            }
            Transaction transaction = jedis.multi();
            // If these change then fail
            transaction.sadd(Ids.id(DOCTOR_PATIENTS, doctorId), patientId);
            transaction.sadd(Ids.id(PATIENT_DOCTORS, patientId), doctorId);
            RedisUtil.checkResult(transaction.exec());
            return patients.size();
        }
    }

    private String insertPerson(Person person, String type, boolean uniqueEmail) throws InternalFormatException, StorageException, DomainConstraintViolated {
        try (Jedis jedis = jedisPool.getResource()) {
            String emailId = Ids.id(EMAIL, person.getEmail());
            if (uniqueEmail) {
                jedis.watch(emailId);
                String emailExists = jedis.get(emailId);
                if (emailExists != null) {
                    throw new DomainConstraintViolated("The email exists already " + person.getEmail() + " assigned to " + emailExists);
                }
            }
            String key = Ids.id(type, person.getId());
            Transaction transaction = jedis.multi();
            transaction.set(key, objectMapper.writeValueAsString(person));
            transaction.sadd(person.getName(), key);
            transaction.set(emailId, key);
            transaction.incr(type);
            RedisUtil.checkResult(transaction.exec());
            return key;
        } catch (JsonProcessingException e) {
            throw new InternalFormatException("Could not serialize person", e);
        }
    }

    public boolean addCondition(String conditionId, String patientId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return 1 == jedis.sadd(Ids.id(PATIENT_CONDITIONS, patientId), Ids.id(ConditionDAO.CONDITION, conditionId));
        }
    }

    public long countType(String type) {
        try (Jedis jedis = jedisPool.getResource()) {
            return Long.parseLong(jedis.get(type));
        }
    }
}
