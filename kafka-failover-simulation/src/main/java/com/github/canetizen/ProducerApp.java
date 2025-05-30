/*
 * Author: canetizen
 * Created on Fri May 30 2025
 * Description: Failover simulation producer app. 
 */

package com.github.canetizen;

import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerApp {
    // SLF4J logger for structured and configurable logging
    private static final Logger logger = LoggerFactory.getLogger(ProducerApp.class);

    public static void main(String[] args) throws InterruptedException {
        // Set the log file name for log4j configuration
        System.setProperty("logfile.name", "producer");

        // Define properties for the Kafka producer
        Properties props = new Properties();

        // List of Kafka brokers for bootstrap; multiple addresses provide fault tolerance
        props.put("bootstrap.servers", "localhost:9092,localhost:9094,localhost:9096");

        // Specify serializer for the key (String type); required for converting keys to bytes
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Specify serializer for the value (String type); required for converting values to bytes
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Initialize the Kafka producer with the configured properties
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props))
             {
            int counter = 0;
            while (true) {
                // Prepare the message to be sent, incrementing the counter for uniqueness
                String message = "#" + counter++;
                
                // Send the message to the specified topic asynchronously
                producer.send(new ProducerRecord<>("failover-test", message));

                // Log the sent message for traceability and debugging
                logger.info("Sent Message: " + message);

                // Sleep for 2 seconds between messages to control the message rate
                Thread.sleep(2000);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
