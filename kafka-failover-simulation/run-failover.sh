#!/bin/bash

#
# Author: canetizen
# Created on Fri May 30 2025
# Description: Kafka failover simulation runner script.
#

set -e

PROJECT_NAME="kafka-failover-simulation"

echo "[INFO] Cleaning up previous logs..."
rm -rf logs/*
echo "[INFO] Previous logs removed."

echo "[INFO] Building the Maven project..."
mvn clean package -DskipTests=false -X
echo "[INFO] Maven build completed."

echo "[INFO] Starting services using Docker Compose..."
docker-compose up -d

echo "[INFO] Waiting for kafka1 broker to become ready..."
until docker exec kafka1 kafka-topics.sh --bootstrap-server localhost:9092 --list &>/dev/null; do
  echo "[DEBUG] kafka1 not ready yet, waiting 2 seconds..."
  sleep 2
done
echo "[INFO] kafka1 broker is ready."

echo "[INFO] Waiting for kafka2 broker to become ready..."
until docker exec kafka2 kafka-topics.sh --bootstrap-server localhost:9092 --list &>/dev/null; do
  echo "[DEBUG] kafka2 not ready yet, waiting 2 seconds..."
  sleep 2
done
echo "[INFO] kafka2 broker is ready."

echo "[INFO] Creating topic 'failover-test' with replication factor 2 and 1 partition..."
docker exec kafka1 kafka-topics.sh --create \
  --topic failover-test \
  --bootstrap-server localhost:9092,localhost:9094 \
  --replication-factor 2 \
  --partitions 1 || {
    echo "[WARN] Topic already exists or could not be created."
  }

# Initialize PIDs
PRODUCER_PID=
CONSUMER_PID=

cleanup() {
  echo "[INFO] Shutting down Producer and Consumer processes..."
  if [[ -n "$PRODUCER_PID" ]]; then
    kill $PRODUCER_PID 2>/dev/null && echo "[INFO] Producer process stopped." || echo "[WARN] Failed to stop Producer process or already stopped."
  fi
  if [[ -n "$CONSUMER_PID" ]]; then
    kill $CONSUMER_PID 2>/dev/null && echo "[INFO] Consumer process stopped." || echo "[WARN] Failed to stop Consumer process or already stopped."
  fi
}
trap cleanup EXIT

echo "[INFO] Starting Producer application with detailed logs..."
java -Dlogfile.name=producer \
     -Dlog4j.configuration=file:src/main/resources/log4j.properties \
     -Dlog4j.debug=false \
     -cp "target/${PROJECT_NAME}-1.0-SNAPSHOT.jar:target/lib/*" \
     com.github.canetizen.ProducerApp &
PRODUCER_PID=$!
echo "[INFO] Producer started with PID $PRODUCER_PID."

echo "[INFO] Starting Consumer application with detailed logs..."
java -Dlogfile.name=consumer \
     -Dlog4j.configuration=file:src/main/resources/log4j.properties \
     -Dlog4j.debug=false \
     -cp "target/${PROJECT_NAME}-1.0-SNAPSHOT.jar:target/lib/*" \
     com.github.canetizen.ConsumerApp &
CONSUMER_PID=$!
echo "[INFO] Consumer started with PID $CONSUMER_PID."

echo "[INFO] Letting Producer and Consumer run for 60 seconds..."
sleep 60

echo "[INFO] Simulating broker failure: stopping kafka1 broker..."
docker-compose stop kafka1
echo "[INFO] kafka1 broker stopped."

echo "[INFO] Letting Producer and Consumer run for another 15 seconds after broker failover..."
sleep 15

echo "[INFO] Test completed. Cleaning up..."
docker-compose down -v
