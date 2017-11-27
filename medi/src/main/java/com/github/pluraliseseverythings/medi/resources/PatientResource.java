package com.github.pluraliseseverythings.medi.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pluraliseseverythings.medi.api.PatientCondition;
import com.github.pluraliseseverythings.medi.api.Person;
import com.github.pluraliseseverythings.medi.db.PersonDAO;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import com.github.pluraliseseverythings.medi.exception.StorageException;
import pluraliseseverythings.events.EventServiceProducer;
import pluraliseseverythings.events.SaveEventException;
import pluraliseseverythings.events.api.Event;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/patient")
public class PatientResource {
    private PersonDAO personDAO;
    private EventServiceProducer eventServiceProducer;

    public PatientResource(PersonDAO personDAO, EventServiceProducer eventServiceProducer) {
        this.personDAO = personDAO;
        this.eventServiceProducer = eventServiceProducer;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Person> getPatient(@QueryParam("name") String name) {
        return personDAO.findPersonByName(name);
    }

    @GET
    @Timed
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public long getPatientCount() {
        return personDAO.countType(PersonDAO.PATIENT);
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String putPatient(Person patient) throws StorageException, SaveEventException, JsonProcessingException, DomainConstraintViolated {
        Person patientWithId = Person.makeId(patient);
        eventServiceProducer.saveEvent(Event.builder().type("put_patient").content(patientWithId).key(patientWithId.getId()).build());
        return personDAO.insertPatient(patientWithId);
    }

    @PUT
    @Timed
    @Path("{patientId}/condition/{conditionId}")
    public void addCondition(@QueryParam("conditionId") String conditionId,
                             @QueryParam("patientId") String patientId) throws SaveEventException, JsonProcessingException {
        boolean added = personDAO.addCondition(conditionId, patientId);
        // This is an idempotent operation, we can avoid event duplicates
        if (added) {
            eventServiceProducer.saveEvent(Event.builder()
                    .type("add_condition")
                    // TODO better as an object
                    .content(PatientCondition.builder().conditionId(conditionId).patientId(patientId).build())
                    .key(String.join(":", conditionId, patientId))
                    .build());
        }
    }
}
