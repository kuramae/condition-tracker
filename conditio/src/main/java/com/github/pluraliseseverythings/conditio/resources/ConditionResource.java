package com.github.pluraliseseverythings.conditio.resources;

import com.codahale.metrics.annotation.Timed;
import com.github.pluraliseseverythings.conditio.db.ConditionStatsDAO;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import static java.time.temporal.ChronoUnit.MILLIS;

@Path("/conditions")
public class ConditionResource {
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
    // TODO this needs some caching. Possibly something smart (tolerate time mismatch of X seconds)
    // TODO cache also the condition names, they don't change often
    public void top(@QueryParam("number") @DefaultValue("20") String number,
                    @QueryParam("ts") Optional<Long> ts) throws IOException {
        Long patients = mediClient.target(mediClientURI).path("patient/count").request().get(Long.class);
        Long timestamp = ts.orElse(System.currentTimeMillis());
        long day = Duration.of(timestamp, MILLIS).toDays();
        Map<String, Long> counts = conditionStatsDAO.getConditionCounts(day, timestamp);
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();
        // TODO Finish this
    }
}
