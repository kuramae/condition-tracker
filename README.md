# Try it out

There's a script to run in test_local

Note: the script shouldn't be seen as a tests, it's just a quick demo

# Assumptions and compromises

## Dual writes

I went for dual writes here mainly because it's simpler. That makes the event source messier since it puts
together writing to the "source of truth" and building a view.
Also, it will cause the event pipeline to have duplicate due to retries caused by db write failures. Also, PUT requests
are more likely to fail (as they are performing two writes). Also, if the events are replayed the db might end
up in a different state.

The other advantage is I can delegate the logic constraints to transactions.

## High severity

These are sent to Kafka to a topic that uses key compaction. As soon as the event is sent the keys are compacted
so in case of failure, when we replay all the events, we won't send duplicates.

For simplicity here, I'm assuming another service takes care of these. The logic is: (1) subscribe to
the alert topic, (2) send the alert and in case of success publish that to the alert topic (the topic uses
compaction), (3) when the service restarts it reads the alerts from the beginning.
The "alert sent" event overrides the "alert received" event on compaction.
When failing to publish the alert, the service should fail a healthcheck and page someone or trigger an automatic response.

## Others

- Skipped the id getters in the resources because time
- Didn't bother with Person, Doctor, Patient hierarchy, it's not needed here and it's easy to refactor
- If latency is a concern the event and db calls can be made non-blocking
- Service interaction is basic. Circuit breakers and retries are missing (retries are especially
required since transactions are handled by optimistic concurrency control).

