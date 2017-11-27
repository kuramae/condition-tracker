package com.github.pluraliseseverythings.medi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonDeserialize(builder = AutoValue_Condition.Builder.class)
public abstract class Condition {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SEVERITY = "severity";
    public static final String DESCRIPTION = "description";

    public Condition() {
    }

    @Nullable
    @JsonProperty(ID)
    public abstract String getId();

    @NotNull
    @JsonProperty(NAME)
    public abstract String getName();

    @NotNull
    @JsonProperty(SEVERITY)
    public abstract Severity getSeverity();

    @NotNull
    @JsonProperty(DESCRIPTION)
    public abstract String getDescription();

    public static Condition makeId(Condition condition) {
        String existingId = condition.getId();
        String partial = existingId == null ? Ids.uniqueID(): existingId;
        return condition.toBuilder().id(partial).build();
    }

    public abstract Builder toBuilder();

    @NotNull
    public static Builder builder() {
        return new AutoValue_Condition.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        @NotNull
        @JsonProperty(ID)
        public abstract Builder id(@NotNull String id);

        @NotNull
        @JsonProperty(NAME)
        public abstract Builder name(@NotNull String name);

        @NotNull
        @JsonProperty(SEVERITY)
        public abstract Builder severity(@NotNull Severity severity);

        @NotNull
        @JsonProperty(DESCRIPTION)
        public abstract Builder description(@NotNull String description);

        @NotNull
        public abstract Condition build();
    }

}
