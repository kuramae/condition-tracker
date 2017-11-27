package com.github.pluraliseseverythings.medi.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pluraliseseverythings.medi.api.Condition;
import com.github.pluraliseseverythings.medi.db.ConditionDAO;
import pluraliseseverythings.events.EventServiceProducer;
import pluraliseseverythings.events.SaveEventException;
import pluraliseseverythings.events.api.Event;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.github.pluraliseseverythings.medi.api.Severity.HIGH;


@Path("/condition")
@Produces(MediaType.APPLICATION_JSON)
public class ConditionResource {
    private ConditionDAO conditionDAO;
    private EventServiceProducer eventServiceProducer;

    public ConditionResource(ConditionDAO conditionDAO, EventServiceProducer eventServiceProducer) {
        this.conditionDAO = conditionDAO;
        this.eventServiceProducer = eventServiceProducer;
    }

    @GET
    @Timed
    @Path("{id}")
    public Condition getCondition(@PathParam("id") String id) throws IOException {
        return conditionDAO.getCondition(id);
    }

    @POST
    @Timed
    public void putCondition(Condition condition) throws SaveEventException, JsonProcessingException {
        Condition conditionWithId = Condition.makeId(condition);
        eventServiceProducer.saveEvent(Event.builder().type("put_condition").key(conditionWithId.getId()).content(conditionWithId).build());
        if (condition.getSeverity().equals(HIGH)) {
            eventServiceProducer.saveEvent(Event.builder().type("alert").key(conditionWithId.getId()).content(conditionWithId).build());
        }
        conditionDAO.insertCondition(condition);
    }
}
