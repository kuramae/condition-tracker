package com.github.pluraliseseverythings.medi.db;

import com.github.pluraliseseverythings.medi.exception.StorageException;

import java.util.Collection;
import java.util.stream.Collectors;

public class RedisUtil {
    public static void checkResult(Collection<Object> result) throws StorageException {
        if (result.stream().anyMatch(s -> s.toString().equalsIgnoreCase("FAIL"))) {
            throw new StorageException(String.join("; ", result.stream().map(Object::toString).collect(Collectors.toList())));
        }
    }
}
