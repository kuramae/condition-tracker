#!/bin/bash

export SEED=$(( ( RANDOM  )  + 1 ))
export CURLY="curl -sS -X"

echo "Adding a patient"
PATIENTID=$($CURLY POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "John'$SEED' Doe", "email": "john'$SEED'@Doe.com", "address": "Bishops Street 94, London"}')
echo $PATIENTID
echo
echo "Adding it again, same email so we should get an error\n"
$CURLY POST http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "John'$SEED' Doe", "email": "john'$SEED'@Doe.com", "address": "Bishops Street 94, London"}'
echo
echo
echo "Reading patient from name\n"
$CURLY GET 'http://127.0.0.1:8080/patient?name=John'$SEED'%20Doe' -H 'content-type: application/json'
echo
echo
echo "Adding a doctor"
DOCTORID1=$($CURLY POST http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Rick'$SEED' Sanchez", "email": "rick'$SEED'@vindicators.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID1
echo
echo "...and another"
DOCTORID2=$($CURLY POST http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Morty'$SEED' Smith", "email": "morty'$SEED'@vindicators.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID2
echo
echo "...and another"
DOCTORID3=$($CURLY POST http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Gerry'$SEED' Smith", "email": "gerry'$SEED'@smith.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID3
echo
echo "Adding patient to doctor 1"
$CURLY POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 1, yes the same"
$CURLY POST http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 2, now we should have two doctors... the next one should fail"
$CURLY POST http://127.0.0.1:8080/doctor/${DOCTORID2}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 3, this one should fail"
$CURLY POST http://127.0.0.1:8080/doctor/${DOCTORID3}/patient/${PATIENTID}
echo
echo
echo "Adding a consultation"
$CURLY POST http://127.0.0.1:8080/consultation -H 'content-type: application/json' -d '{"doctorId":'$DOCTORID1'", "patientId":'$DOCTORID1'", "start":1511804374223 , "end":1511808374223 }'
echo