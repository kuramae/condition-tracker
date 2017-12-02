package com.github.pluraliseseverythings.medi.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pluraliseseverythings.medi.api.Condition;
import com.github.pluraliseseverythings.medi.api.Ids;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.io.IOException;

public class ConditionDAO {
    private static ObjectMapper MAPPER = new ObjectMapper();

    public static final String CONDITION = "condition";
    private Pool<Jedis> jedisPool;

    public ConditionDAO(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public String insertCondition(Condition condition) throws JsonProcessingException {
        String key = Ids.id(CONDITION, condition.getId());
        // Write to DB
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, MAPPER.writeValueAsString(condition));
        }
        return key;
    }

    public Condition getCondition(String id) throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            return MAPPER.readValue(jedis.get(Ids.id(CONDITION, id)), Condition.class);
        }
    }
}
