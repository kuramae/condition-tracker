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


    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String putDoctor(Person doctor) throws StorageException, DomainConstraintViolated, SaveEventException, JsonProcessingException {
        Person doctorWithId = Person.makeId(doctor);
        eventServiceProducer.saveEvent(Event.builder().type("put_doctor").key(doctorWithId.getId()).content(doctorWithId).build());
        return personDAO.insertDoctor(doctorWithId);
    }

    @POST
    @Timed
    @Path("{doctorId}/patient/{patientId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Map<String, Long> addPatient(@PathParam("doctorId") String doctorId,
                          @PathParam("patientId")String patientId) throws DomainConstraintViolated, StorageException, SaveEventException, JsonProcessingException {
        eventServiceProducer.saveEvent(Event.builder()
                .type("add_patient")
                .key(String.join(":", doctorId, patientId))
                .content(ImmutableMap.of("doctorId", doctorId, "patientId", patientId))
                .build());
        return personDAO.addPatient(doctorId, patientId);
    }
}
