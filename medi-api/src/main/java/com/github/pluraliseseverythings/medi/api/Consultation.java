package com.github.pluraliseseverythings.medi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonDeserialize(builder = AutoValue_Consultation.Builder.class)
public abstract class Consultation {

    public static final String ID = "id";
    public static final String DOCTOR_ID = "doctorId";
    public static final String PATIENT_ID = "patientId";
    public static final String START = "start";
    public static final String END = "end";
    public static final String DESCRIPTION = "description";

    public Consultation() {
    }

    @Nullable
    @JsonProperty(ID)
    public abstract String getId();

    @NotNull
    @JsonProperty(DOCTOR_ID)
    public abstract String getDoctorId();

    @NotNull
    @JsonProperty(PATIENT_ID)
    public abstract String getPatientId();

    @NotNull
    @JsonProperty(START)
    public abstract long getStart();

    @NotNull
    @JsonProperty(END)
    public abstract long getEnd();

    @NotNull
    @JsonProperty(DESCRIPTION)
    public abstract String getDescription();

    public static Consultation makeId(Consultation consultation) {
        String existingId = consultation.getId();
        String partial = existingId == null ? Ids.uniqueID(): existingId;
        return consultation.toBuilder().id(partial).build();
    }

    public abstract Builder toBuilder();

    @NotNull
    public static Builder builder() {
        return new AutoValue_Consultation.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        @NotNull
        @JsonProperty(ID)
        public abstract Builder id(@NotNull String id);

        @NotNull
        @JsonProperty(DOCTOR_ID)
        public abstract Builder doctorId(@NotNull String doctorId);

        @NotNull
        @JsonProperty(PATIENT_ID)
        public abstract Builder patientId(@NotNull String patientId);

        @NotNull
        @JsonProperty(START)
        public abstract Builder start(long start);

        @NotNull
        @JsonProperty(END)
        public abstract Builder end(long end);

        @NotNull
        @JsonProperty(DESCRIPTION)
        public abstract Builder description(@NotNull String description);

        @NotNull
        public abstract Consultation build();
    }

}
