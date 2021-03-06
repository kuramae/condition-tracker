package com.github.pluraliseseverythings.medi.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pluraliseseverythings.medi.api.Consultation;
import com.github.pluraliseseverythings.medi.db.ConsultationDAO;
import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;
import pluraliseseverythings.events.EventServiceProducer;
import pluraliseseverythings.events.SaveEventException;
import pluraliseseverythings.events.api.Event;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/consultation")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultationResource {
    private ConsultationDAO consultationDAO;
    private EventServiceProducer eventServiceProducer;

    public ConsultationResource(ConsultationDAO consultationDAO, EventServiceProducer eventServiceProducer) {
        this.consultationDAO = consultationDAO;
        this.eventServiceProducer = eventServiceProducer;
    }

    @GET
    @Timed
    public Consultation getDoctor(@PathParam("id") String id) throws IOException {
        return consultationDAO.findConsultationById(id);
    }

    @PUT
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String putConsultation(Consultation consultation) throws DomainConstraintViolated, JsonProcessingException, SaveEventException {
        String result = consultationDAO.insertConsultation(consultation);
        eventServiceProducer.saveEvent(Event.<Consultation>builder().type("put_consultation").key(consultation.getId()).content(consultation).build());
        return result;
    }
}
