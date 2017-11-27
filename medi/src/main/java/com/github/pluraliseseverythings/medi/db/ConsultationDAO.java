package com.github.pluraliseseverythings.medi.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pluraliseseverythings.medi.api.Consultation;
import com.github.pluraliseseverythings.medi.api.Ids;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.util.Pool;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class ConsultationDAO {
    private static ObjectMapper MAPPER = new ObjectMapper();

    private static final String CONSULTATION = "consultation";
    private static final String PATIENT_CONSULTATION = "patient:consultation";
    private static final String DOCTOR_CONSULTATION = "doctor:consultation";

    private Pool<Jedis> jedisPool;

    public ConsultationDAO(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * This one apart from the obvious consultation key, also adds 2 elements
     * to the doctor consultation set and 2 to the patient.
     * By adding both start and end, we can just check the overlaps by doing a range query.
     * @param consultation  Consultation being inserted
     * @throws JsonProcessingException  The object is somehow malformed
     * @throws DomainConstraintViolated There is an overlap
     */
    public void insertConsultation(Consultation consultation) throws JsonProcessingException, DomainConstraintViolated {
        // Sort out id
        String existingId = consultation.getId();
        String partial = existingId == null ? Ids.uniqueID(): existingId;
        String key = Ids.id(CONSULTATION, partial);
        Consultation build = consultation.toBuilder().id(partial).build();
        // Write to DB
        try (Jedis jedis = jedisPool.getResource()) {
            String doctorConsultationId = Ids.id(DOCTOR_CONSULTATION, consultation.getDoctorId());
            String patientConsultationId = Ids.id(PATIENT_CONSULTATION, consultation.getPatientId());
            String doctorPatients = Ids.id(PersonDAO.DOCTOR_PATIENTS, consultation.getDoctorId());
            jedis.watch(doctorConsultationId, patientConsultationId);
            if (!jedis.smembers(doctorPatients).contains(consultation.getPatientId())) {
                throw new DomainConstraintViolated("Doctor doesn't have this patient");
            }
            checkOverlap(consultation, jedis, doctorConsultationId);
            checkOverlap(consultation, jedis, patientConsultationId);
            Transaction transaction = jedis.multi();
            transaction.set(key, MAPPER.writeValueAsString(build));
            transaction.zadd(doctorConsultationId, consultation.getStart(), key);
            transaction.zadd(doctorConsultationId, consultation.getEnd(), key);
            transaction.zadd(patientConsultationId, consultation.getStart(), key);
            transaction.zadd(patientConsultationId, consultation.getEnd(), key);
            transaction.exec();
        }
    }

    private void checkOverlap(Consultation consultation, Jedis jedis, String doctorConsultationId) throws DomainConstraintViolated {
        Set<String> overlappingConsultationsDoctor = jedis.zrange(doctorConsultationId, consultation.getStart() - Duration.of(24, DAYS).toMillis(), consultation.getEnd());
        if (!overlappingConsultationsDoctor.isEmpty()) {
            throw new DomainConstraintViolated("Overlapping consultation exists for doctor");
        }
    }

    public Consultation findConsultationById(String id) throws IOException {
        String key = Ids.id(CONSULTATION, id);
        try (Jedis jedis = jedisPool.getResource()) {
            return MAPPER.readValue(jedis.get(key), Consultation.class);
        }
    }
}
