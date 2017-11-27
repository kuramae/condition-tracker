#!/bin/bash

mkdir -p services/kafka
if [ ! -d services/kafka/bin ]; then
  curl http://apache.mirror.anlx.net/kafka/1.0.0/kafka_2.11-1.0.0.tgz | tar xvz --strip-components 1 -C services/kafka
fi

which redis-server
if [ $? -eq 0 ]; then
    echo "Cool, Redis is installed here"
else
    echo "Attempting to brew install redis, if this fails, please install redis manually to run this"
    brew install redis
    if [ $? -eq 0 ]; then
        echo "Done!"
    else
        echo "Nope, install Redis please"
    fi
fi

# Ok, not the best way to do this, but if they are running already these fails and we still have one instance
nohup services/kafka/bin/zookeeper-server-start.sh services/kafka/config/zookeeper.properties 2>&1 &
sleep 3000
nohup services/kafka/bin/kafka-server-start.sh services/kafka/config/server.properties 2>&1 &
nohup redis-server > /dev/null 2>&1 &

java -jar ../medi/target/medi-0.1-SNAPSHOT.jar server config/medi.yml
