#!/bin/bash

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