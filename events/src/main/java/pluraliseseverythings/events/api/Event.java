package pluraliseseverythings.events.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.validation.constraints.NotNull;

@AutoValue
@JsonDeserialize(builder = AutoValue_Event.Builder.class)
public abstract class Event {
    private static final String CONTENT = "content";
    private static final String TIMESTAMP = "timestamp";
    private static final String TYPE = "type";
    private static final String KEY = "key";

    @NotNull
    @JsonProperty(CONTENT)
    public abstract Object getContent();

    @NotNull
    @JsonProperty(KEY)
    public abstract String getKey();

    @NotNull
    @JsonProperty(TYPE)
    public abstract String getType();

    @NotNull
    @JsonProperty(TIMESTAMP)
    public abstract long getTimestamp();

    @NotNull
    public static Builder builder() {
        return new AutoValue_Event.Builder().timestamp(System.currentTimeMillis());
    }

    @AutoValue.Builder
    public static abstract class Builder {
        @NotNull
        @JsonProperty(CONTENT)
        public abstract Builder content(@NotNull Object content);

        @NotNull
        @JsonProperty(TIMESTAMP)
        public abstract Builder timestamp(@NotNull long timestamp);

        @NotNull
        @JsonProperty(TYPE)
        public abstract Builder type(@NotNull String type);

        @NotNull
        @JsonProperty(KEY)
        public abstract Builder key(@NotNull String key);

        @NotNull
        public abstract Event build();
    }
}
