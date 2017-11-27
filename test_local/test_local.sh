#!/bin/bash

if [ ! -d services/kafka ]; then
  mkdir -p services/redis
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
nohup services/kafka/bin/zookeeper-server-start.sh services/kafka/config/zookeeper.properties > /dev/null 2>&1 &
nohup services/kafka/bin/kafka-server-start.sh services/kafka/config/server.properties > /dev/null 2>&1 &
nohup redis-server > /dev/null 2>&1 &

java -jar ../medi/target/medi-0.1-SNAPSHOT.jar server config/medi.yml

echo "Adding a patient"
PATIENTID=$(curl -X POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "John Doe", "email": "john@Doe.com", "address": "Bishops Street 94, London"}')

echo "Reading patient from name"
curl -X GET \
  'http://127.0.0.1:8080/patient?name=John%20Doe' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json'

echo "Adding a doctor"
DOCTORID1=$(curl -X POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "Rick Sanchez", "email": "rick@vindicators.com", "address": "11 Unknown Street, USA"}')
echo "...and another"
DOCTORID2=$(curl -X POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "Morty Smith", "email": "morty@vindicators.com", "address": "11 Unknown Street, USA"}')
echo "...and another"
DOCTORID3=$(curl -X POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "Gerry Smith", "email": "gerry@smith.com", "address": "11 Unknown Street, USA"}')

echo "Adding patient to doctor 1"
curl -X POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}

echo "Adding patient to doctor 1, yes the same"
curl -X POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}

echo "Adding patient to doctor 2, now we should have two doctors... the next one should fail"
curl -X POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}

echo "Adding patient to doctor 3, this one should fail"
curl -X POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}