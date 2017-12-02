package pluraliseseverythings.events.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.validation.constraints.NotNull;

@AutoValue
@JsonDeserialize(builder = AutoValue_Event.Builder.class)
public abstract class Event<T> {
    private static final String CONTENT = "content";
    private static final String TIMESTAMP = "timestamp";
    private static final String TYPE = "type";
    private static final String KEY = "key";

    @NotNull
    @JsonProperty(CONTENT)
    public abstract T getContent();

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
    public static <T> Builder<T> builder() {
        return new AutoValue_Event.Builder<T>().timestamp(System.currentTimeMillis());
    }

    @AutoValue.Builder
    public static abstract class Builder<T> {
        @NotNull
        @JsonProperty(CONTENT)
        public abstract Builder<T> content(@NotNull T content);

        @NotNull
        @JsonProperty(TIMESTAMP)
        public abstract Builder<T> timestamp(@NotNull long timestamp);

        @NotNull
        @JsonProperty(TYPE)
        public abstract Builder<T> type(@NotNull String type);

        @NotNull
        @JsonProperty(KEY)
        public abstract Builder<T> key(@NotNull String key);

        @NotNull
        public abstract Event<T> build();
    }
}
