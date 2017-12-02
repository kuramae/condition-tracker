package com.github.pluraliseseverythings.medi.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pluraliseseverythings.medi.api.Person;
import com.github.pluraliseseverythings.medi.db.PersonDAO;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import com.github.pluraliseseverythings.medi.exception.StorageException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import pluraliseseverythings.events.EventServiceProducer;
import pluraliseseverythings.events.SaveEventException;
import pluraliseseverythings.events.api.Event;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/doctor")
@Produces(MediaType.APPLICATION_JSON)
public class DoctorResource {
    private PersonDAO personDAO;
    private EventServiceProducer eventServiceProducer;

    public DoctorResource(PersonDAO personDAO, EventServiceProducer eventServiceProducer) {
        this.personDAO = personDAO;
        this.eventServiceProducer = eventServiceProducer;
    }

    @GET
    @Timed
    public Collection<Person> getDoctor(@QueryParam("name") String name) {
        return personDAO.findPersonByName(name);
    }


    @PUT
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String putDoctor(Person doctor) throws StorageException, DomainConstraintViolated, SaveEventException, JsonProcessingException {
        String result = personDAO.insertDoctor(doctor);
        eventServiceProducer.saveEvent(Event.<Person>builder().type("put_doctor").key(doctor.getEmail()).content(doctor).build());
        return result;
    }

    @PUT
    @Timed
    @Path("{doctorId}/patient/{patientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> addPatient(@PathParam("doctorId") String doctorId,
                          @PathParam("patientId")String patientId) throws DomainConstraintViolated, StorageException, SaveEventException, JsonProcessingException {
        Map<String, Long> result = personDAO.addPatient(doctorId, patientId);
        eventServiceProducer.saveEvent(Event.<Map<String, String>>builder()
                .type("add_patient")
                .key(String.join(":", doctorId, patientId))
                .content(ImmutableMap.of("doctorId", doctorId, "patientId", patientId))
                .build());
        return result;
    }
}
