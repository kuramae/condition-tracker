#!/bin/bash

SEED=$(( ( RANDOM  )  + 1 ))
export CURLY="curl -sS -X"

echo "Adding a patient"
PATIENTID=$($CURLY PUT http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "John'$SEED' Doe", "email": "john'$SEED'@Doe.com", "address": "Bishops Street 94, London"}')
echo $PATIENTID
echo
echo "Adding it again, same email so we should get the same id and an update\n"
$CURLY PUT http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "John'$SEED' Doe the second", "email": "john'$SEED'@Doe.com", "address": "Bishops Street 94, London"}'
echo
echo
echo "Reading patient from updated name\n"
$CURLY GET 'http://127.0.0.1:8080/patient?name=John'$SEED'%20Doe%20the%20second' -H 'content-type: application/json'
echo
echo
echo "Adding a doctor"
DOCTORID1=$($CURLY PUT http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Rick'$SEED' Sanchez", "email": "rick'$SEED'@vindicators.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID1
echo
echo "...and another"
DOCTORID2=$($CURLY PUT http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Morty'$SEED' Smith", "email": "morty'$SEED'@vindicators.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID2
echo
echo "...and another"
DOCTORID3=$($CURLY PUT http://127.0.0.1:8080/doctor -H 'content-type: application/json' -d '{"name": "Gerry'$SEED' Smith", "email": "gerry'$SEED'@smith.com", "address": "11 Unknown Street, USA"}')
echo $DOCTORID3
echo
echo "Adding patient to doctor 1"
$CURLY PUT http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 1, yes the same"
$CURLY PUT http://127.0.0.1:8080/doctor/${DOCTORID1}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 2, now we should have two doctors... the next one should fail"
$CURLY PUT http://127.0.0.1:8080/doctor/${DOCTORID2}/patient/${PATIENTID}
echo
echo "Adding patient to doctor 3, this one should fail"
$CURLY PUT http://127.0.0.1:8080/doctor/${DOCTORID3}/patient/${PATIENTID}
echo
echo
echo "Adding a new patient, just for testing"
PATIENTID2=$($CURLY PUT http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "Hey'$SEED' Joe", "email": "hey'$SEED'@joe.com", "address": "Oxford Way 11, London"}')
echo $PATIENTID
echo
echo "Adding a consultation for this last patient, but the doctor is not associated to it"
$CURLY PUT http://127.0.0.1:8080/consultation -H 'content-type: application/json' -d '{"doctorId":"'$DOCTORID1'", "patientId":"'$PATIENTID2'", "start":1511804374223 , "end":1511808474223, "description":"Checking constant headaches" }'
echo
echo "Adding a consultation for the first patient this should be ok"
$CURLY PUT http://127.0.0.1:8080/consultation -H 'content-type: application/json' -d '{"doctorId":"'$DOCTORID1'", "patientId":"'$PATIENTID'", "start":1511804374223 , "end":1511808474223, "description":"Checking constant headaches" }'
echo
echo "Adding an overlapping one, we want an error"
$CURLY PUT http://127.0.0.1:8080/consultation -H 'content-type: application/json' -d '{"doctorId":"'$DOCTORID2'", "patientId":"'$PATIENTID'", "start":1511804375223 , "end":1511808475223, "description":"Checking constant headaches" }'
echo
echo
echo "Now let's add a bunch of conditions and patients"
TIMESTAMPBEFORE=$(($(date +%s)*1000))
sleep 1
for i in `seq 1 5`;
do
        # Three different conditions
        SEEDCOND=${SEED}_${i}
        CONDITION=condition${SEEDCOND}
        case $(($i % 3)) in
        1) SEVERITY='high'
           ;;
        2) SEVERITY='low'
           ;;
        *) SEVERITY='medium'
           ;;
        esac
        echo
        CID=$($CURLY POST http://127.0.0.1:8080/condition -H 'content-type: application/json' -d '{"name":"condition'$CONDITION'", "severity":"'$SEVERITY'", "description":"Some description" }')
        echo Added condition with id $CID name $CONDITION with severity $SEVERITY
        echo
        PATIENTIDX=$($CURLY PUT http://127.0.0.1:8080/patient -H 'content-type: application/json' -d '{"name": "Patient'$SEEDCOND' Joe", "email": "heyhey'$SEEDCOND'@joe.com", "address": "Oxford Way 11, London"}')
        echo New patient $PATIENTID
        echo
        echo Associating condition $CID to $PATIENTIDX
        $CURLY PUT http://127.0.0.1:8080/patient/$PATIENTIDX/condition/$CID
done
echo
echo
sleep 1
TIMESTAMPAFTER=$(($(date +%s)*1000))
echo "Time to get the stats, after everything has happened (top 3)"
$CURLY GET "http://127.0.0.1:8960/conditions/top?number=3"
echo
echo
echo "Before we added these new conditions $TIMESTAMPBEFORE"
$CURLY GET "http://127.0.0.1:8960/conditions/top?number=3&ts=$TIMESTAMPBEFORE"
echo
echo
sleep 1
echo Adding more stuff to the initial patient, after this one of the conditions should have a slightly higher percentage
$CURLY PUT http://127.0.0.1:8080/patient/$PATIENTID/condition/$CID
echo
echo
echo Checking now
echo `$CURLY GET "http://127.0.0.1:8960/conditions/top?number=3"`
echo
echo Checking the old version with TS $TIMESTAMPAFTER
echo `$CURLY GET "http://127.0.0.1:8960/conditions/top?number=3&ts=$TIMESTAMPAFTER"`