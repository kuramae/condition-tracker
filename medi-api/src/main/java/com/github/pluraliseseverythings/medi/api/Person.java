package com.github.pluraliseseverythings.medi.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = AutoValue_Person.Builder.class)
public abstract class Person {

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";

    public Person() {
    }

    @NotNull
    @JsonProperty(NAME)
    public abstract String getName();

    @NotNull
    @JsonProperty(EMAIL)
    public abstract String getEmail();

    @NotNull
    @JsonProperty(ADDRESS)
    public abstract String getAddress();


    public abstract Builder toBuilder();


    @NotNull
    public static Builder builder() {
        return new AutoValue_Person.Builder();
    }

    @JsonCreator
    private static Builder create() { return Person.builder(); }

    @AutoValue.Builder
    public static abstract class Builder {

        @NotNull
        @JsonProperty(NAME)
        public abstract Builder name(@NotNull String name);

        @NotNull
        @JsonProperty(EMAIL)
        public abstract Builder email(@NotNull String email);

        @NotNull
        @JsonProperty(ADDRESS)
        public abstract Builder address(@NotNull String address);

        @NotNull
        public abstract Person build();
    }

}
