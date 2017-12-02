# Try it out

There's a script to run in test_local

Note: the script shouldn't be seen as a tests, it's just a quick demo

Run first 

```
test_local/start_local.sh
```

To start up the services. It will try to download and install Kafka and Redis as well and start them too.

Then 
```
test_local/test_local.sh
```

starts a set of curl requests that test the basic logic requested.

# Assumptions and compromises

## High severity

These are sent to Kafka to an alert topic.

For simplicity here, I'm assuming another service takes care of these. The logic is: (1) subscribe to
the alert topic, (2) send the alert and in case of success publish that to the alert topic (the topic uses
compaction), (3) when the service restarts it reads the alerts from the beginning.
The "alert sent" event overrides the "alert received" event on compaction.
When failing to publish the alert, the service should fail a healthcheck and page someone or trigger an automatic response.

Not implemented here, but the idea is that this should use key compaction. 
As soon as the event is sent the keys are compacted
so in case of failure, when we replay all the events, we won't send duplicates.

## Others

- If latency is a concern the event and db calls can be made non-blocking
- Service interaction is basic. Circuit breakers and retries are missing (retries are 
especially required since transactions are handled by optimistic concurrency control).
- Most likely there is some case sensitivity to fix somewhere
- The top 20 logic is sketched but I didn't really try it out properly
- Unit tests missing
- Some of the inserts are idempotent, which works well if we replay the events.
The others can be made idempotent.
- Some getters that are needed to make this work are missing.
