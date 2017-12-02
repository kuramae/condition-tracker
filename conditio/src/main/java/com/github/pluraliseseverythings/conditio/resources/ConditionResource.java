package com.github.pluraliseseverythings.conditio.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pluraliseseverythings.conditio.db.ConditionStatsDAO;

import com.github.pluraliseseverythings.medi.api.PatientCondition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import static java.time.temporal.ChronoUnit.MILLIS;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/conditions")
public class ConditionResource {
    private static Logger LOG = LoggerFactory.getLogger(ConditionResource.class);

    private ConditionStatsDAO conditionStatsDAO;
    private Client mediClient;
    private URI mediClientURI;

    public ConditionResource(ConditionStatsDAO conditionStatsDAO, Client mediClient, URI mediClientURI) {
        this.conditionStatsDAO = conditionStatsDAO;
        this.mediClient = mediClient;
        this.mediClientURI = mediClientURI;
    }

    @GET
    @Timed
    @Path("top")
    @Produces(MediaType.APPLICATION_JSON)
    // TODO this needs some caching. Possibly something smart (tolerate time mismatch of X seconds)
    // TODO cache also the condition names, they don't change often
    public List<ScoredCondition> top(
            @QueryParam("number") @DefaultValue("20") String number,
            @QueryParam("ts") @DefaultValue("-1") long ts) throws IOException {
        Long patients = mediClient.target(mediClientURI).path("patient/count").request().get(Long.class);
        Long timestamp = ts == -1 ? System.currentTimeMillis() : ts;
        LOG.info("Checking top conditions with ts {},{}", ts, timestamp);
        Map<String, Long> counts = conditionStatsDAO.getConditionCounts(timestamp);
        // Better if this was a heap with "number" as max size
        PriorityQueue<ScoredCondition> priorityQueue = new PriorityQueue<>(
                Comparator.comparing(p -> -p.score));
        for (String condition : counts.keySet()) {
            priorityQueue.add(new ScoredCondition(condition, (double) counts.get(condition) / patients));
        }
        List<ScoredCondition> scoredConditions = new ArrayList<>(priorityQueue.size());
        for(int i = 0; i <  Math.min(priorityQueue.size(), Integer.parseInt(number)); i++) {
            scoredConditions.add(priorityQueue.poll());
        }
        return scoredConditions;
    }


    private class ScoredCondition {
        @JsonProperty("name")
        final String name;
        @JsonProperty("score")
        final double score;

        @JsonCreator
        public ScoredCondition(String name, double score) {
            this.name = name;
            this.score = score;
        }
    }
}
