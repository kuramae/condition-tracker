package com.github.pluraliseseverythings.medi.api;

import java.util.UUID;

public class Ids {
    public static String uniqueID() {
        return UUID.randomUUID().toString();
    }

    public static String id(String type, String id) {
        return String.join(":", type, id);
    }
}
