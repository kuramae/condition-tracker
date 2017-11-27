package com.github.pluraliseseverythings.conditio.db;

import com.github.pluraliseseverythings.medi.api.Ids;
import com.github.pluraliseseverythings.medi.api.PatientCondition;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConditionStatsDAO {
    public static final String STATS_DAY = "stats:day";
    public static final String DAYS = "days";
    public static final String STATSSET_DAY = "statsset:day";
    private Pool<Jedis> jedisPool;

    public ConditionStatsDAO(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Ha! This is tricky.
     * Each day is a redis hash. We increment fields when they arrive here and that's done server side,
     * so it's nice and transactional.
     * In addition to each day we add events to a day bucket. So we have a sorted set for the day that also contains
     * the events in their order.
     * When we have to get the full count, we get all the days until the requested one, we merge them and then play the events.
     * TODO: we can have months and year buckets.
     * This is nice because if we have some delayed events, we don't have to take care of setting a cutoff time.
     * Obviously it increases the number of writes required per event and the reads as well.
     * TODO: the logic changes a bit if we have deletes as well
     * I'd probably use a CRDT for that (keep timestamps of the deletes in the set)
     * @param content       The patient condition tuple
     * @param dayBucket     Bucket of the event
     */
    public void addPatientCondition(PatientCondition content, long dayBucket, long timestamp) {
        String bucketId = Ids.id(STATS_DAY, String.valueOf(dayBucket));
        String setId = Ids.id(STATSSET_DAY, String.valueOf(dayBucket));
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy(bucketId, content.getConditionId(), 1);
            jedis.zadd(setId, timestamp, content.getConditionId());
            jedis.zadd(DAYS, dayBucket, STATS_DAY);
        }
    }

    public Map<String, Long> getConditionCounts(long dayBucket, long timestamp) {
        Map<String, Long> conditionToCount = new HashMap<>();
        Set<String> allDays;
        try(Jedis jedis = jedisPool.getResource()) {
            allDays = jedis.zrange(DAYS, 0, dayBucket - 1);
        }
        // TODO: Could parallelize this
        for(String day : allDays) {
            String bucketId = Ids.id(STATS_DAY, day);
            try(Jedis jedis = jedisPool.getResource()) {
                Map<String, String> dayMap = jedis.hgetAll(bucketId);
                for (Map.Entry<String, String> e : dayMap.entrySet()) {
                        conditionToCount.merge(e.getKey(), Long.parseLong(e.getValue()), (a, b) -> a + b);

                }
            }
        }
        String setId = Ids.id(STATSSET_DAY, String.valueOf(dayBucket));
        try(Jedis jedis = jedisPool.getResource()) {
            Set<String> eventsInTheDay = jedis.zrange(setId, 0, timestamp);
            for(String event : eventsInTheDay) {
                conditionToCount.merge(event, 1L, (a, b) -> a + b);
            }
        }
        return conditionToCount;
    }
}
