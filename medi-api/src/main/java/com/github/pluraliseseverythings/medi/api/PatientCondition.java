package com.github.pluraliseseverythings.medi.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
public abstract class PatientCondition {
    private static final String PATIENT_ID = "patient_id";
    private static final String CONDITION_ID = "condition_id";

    @NotNull
    @JsonProperty(PATIENT_ID)
    public abstract String getPatientId();

    @NotNull
    @JsonProperty(CONDITION_ID)
    public abstract String getConditionId();

    @NotNull
    public static PatientCondition.Builder builder() {
        return new AutoValue_PatientCondition.Builder();
    }

    @JsonCreator
    private static Builder create() { return PatientCondition.builder(); }

    @AutoValue.Builder
    public static abstract class Builder {
        @Nullable
        @JsonProperty(PATIENT_ID)
        public abstract Builder patientId(@NotNull String id);

        @NotNull
        @JsonProperty(CONDITION_ID)
        public abstract Builder conditionId(@NotNull String name);

        @NotNull
        public abstract PatientCondition build();
    }
}
