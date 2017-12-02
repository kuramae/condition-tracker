package com.github.pluraliseseverythings.conditio.db;

import com.github.pluraliseseverythings.conditio.core.StatsComputer;
import com.github.pluraliseseverythings.medi.api.Ids;
import com.github.pluraliseseverythings.medi.api.PatientCondition;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.MILLIS;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConditionStatsDAO {

    private static Logger LOG = LoggerFactory.getLogger(ConditionStatsDAO.class);


    public static final String STATS_DAY = "stats:day";
    public static final String DAYS = "days";
    public static final String STATSSET_DAY = "statsset:day";
    private Pool<Jedis> jedisPool;

    public ConditionStatsDAO(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Ha! This is tricky.
     * Each day is a redis hash. We increment field counters associated to the particular day.
     * In addition to each day we add events to a day bucket. So we have a sorted set for the day that also contains
     * the events in their order.
     * When we have to get the full count, we get all the days until the requested one, we merge them and then play the events.
     * TODO: we can have months and year buckets.
     * This is nice because if we have some delayed events, we don't have to take care of setting a cutoff time.
     * Obviously it increases the number of writes required per event and the reads as well.
     * TODO: the logic changes a bit if we have deletes as well
     * I'd probably use a CRDT for that (keep timestamps of the deletes in the set)
     * @param content       The patient condition tuple
     */
    public void addPatientCondition(PatientCondition content, long timestamp) {
        long dayBucket = Duration.of(timestamp, MILLIS).toDays();
        String bucketId = Ids.id(STATS_DAY, String.valueOf(dayBucket));
        String setId = Ids.id(STATSSET_DAY, String.valueOf(dayBucket));
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy(bucketId, content.getConditionId(), 1);
            // Add the condition with a timestamp to the set of events for that day
            // Just forcing redis to use an ordered set to keep repeated elements
            // (it's just very convenient to use the range command on lookup)
            jedis.zadd(setId, timestamp, content.getConditionId() + "#" + timestamp);
            // Add the day bucket to a list of days
            jedis.zadd(DAYS, dayBucket, bucketId);
        }
    }

    public Map<String, Long> getConditionCounts(long timestamp) {
        long dayBucket = Duration.of(timestamp, MILLIS).toDays();
        Map<String, Long> conditionToCount = new HashMap<>();
        Set<String> allBucketIds;
        if (dayBucket == 0) {
            // edge condition
            allBucketIds  = Collections.emptySet();
        } else {
            try(Jedis jedis = jedisPool.getResource()) {
                allBucketIds = jedis.zrangeByScore(DAYS, 0, dayBucket - 1);
            }
        }
        // TODO: Could parallelize this
        for(String bucketId : allBucketIds) {
            try(Jedis jedis = jedisPool.getResource()) {
                Map<String, String> dayMap = jedis.hgetAll(bucketId);
                for (Map.Entry<String, String> e : dayMap.entrySet()) {
                        conditionToCount.merge(e.getKey(), Long.parseLong(e.getValue()), (a, b) -> a + b);
                }
            }
        }
        String setId = Ids.id(STATSSET_DAY, String.valueOf(dayBucket));
        try(Jedis jedis = jedisPool.getResource()) {
            Set<String> eventsInTheDay = jedis.zrangeByScore(setId, 0, timestamp);
            LOG.info("Got all elements during day {} before {}, count: {}", dayBucket, timestamp, eventsInTheDay.size());
            for(String event : eventsInTheDay) {
                conditionToCount.merge(event.split("#")[0], 1L, (a, b) -> a + b);
            }
        }
        return conditionToCount;
    }
}
