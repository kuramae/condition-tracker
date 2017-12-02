package com.github.pluraliseseverythings.medi.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pluraliseseverythings.medi.api.Consultation;
import com.github.pluraliseseverythings.medi.api.Ids;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Collections;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.util.Pool;

import java.io.IOException;
import java.util.Set;

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
     *
     * @param consultation Consultation being inserted
     * @throws JsonProcessingException The object is somehow malformed
     * @throws DomainConstraintViolated There is an overlap
     */
    public String insertConsultation(Consultation consultation)
            throws JsonProcessingException, DomainConstraintViolated {
        // Write to DB
        String key = Ids.id(CONSULTATION, consultation.getId());
        try (Jedis jedis = jedisPool.getResource()) {
            String doctorConsultationId = Ids.id(DOCTOR_CONSULTATION, consultation.getDoctorId());
            String patientConsultationId = Ids
                    .id(PATIENT_CONSULTATION, consultation.getPatientId());
            String doctorPatients = Ids.id(PersonDAO.DOCTOR_PATIENTS, consultation.getDoctorId());
            jedis.watch(doctorConsultationId, patientConsultationId);
            if (!jedis.smembers(doctorPatients).contains(consultation.getPatientId())) {
                throw new DomainConstraintViolated("Doctor doesn't have this patient");
            }
            checkOverlap(consultation, jedis, doctorConsultationId);
            checkOverlap(consultation, jedis, patientConsultationId);
            Transaction transaction = jedis.multi();
            transaction.set(key, MAPPER.writeValueAsString(consultation));
            transaction.zadd(doctorConsultationId, consultation.getStart(), key + "_start");
            transaction.zadd(doctorConsultationId, consultation.getEnd(), key + "_end");
            transaction.zadd(patientConsultationId, consultation.getStart(), key + "_start");
            transaction.zadd(patientConsultationId, consultation.getEnd(), key + "_end");
            transaction.exec();
        }
        return key;
    }

    private void checkOverlap(Consultation consultation, Jedis jedis, String doctorConsultationId)
            throws DomainConstraintViolated {
        // TODO this assumes consultations don't last for longer than 7 days, it can be generalised. It can also be optimized
        // TODO validation that start is before end
        Set<Tuple> overlappingConsultationsDoctor = jedis
                .zrangeByScoreWithScores(doctorConsultationId, consultation.getStart() - Duration
                        .of(7, DAYS).toMillis(), consultation.getEnd() + Duration
                        .of(7, DAYS).toMillis());
        // There is some start or end during this consultation
        if (overlappingConsultationsDoctor.stream()
                .anyMatch(p -> p.getScore() >= consultation.getStart() && p.getScore() < consultation.getStart())
            // There is a start before a start
            || overlappingConsultationsDoctor.stream()
                .filter(p -> p.getScore() <= consultation.getStart()).sorted(Collections.reverseOrder())
                .findFirst().map(Tuple::getElement)
                .orElse("nothing")
                .endsWith("_start")
            // There is an end after the end
            || overlappingConsultationsDoctor.stream()
                .filter(p -> p.getScore() >= consultation.getEnd()).sorted()
                .findFirst()
                .map(Tuple::getElement)
                .orElse("nothing")
                .endsWith("_end")) {
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
