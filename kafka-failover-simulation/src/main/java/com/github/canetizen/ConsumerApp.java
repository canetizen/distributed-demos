/*
 * Author: canetizen
 * Created on Fri May 30 2025
 * Description: Failover simulation consumer app. 
 */

package com.github.canetizen;

import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerApp {
    // SLF4J logger for structured logging output
    private static final Logger logger = LoggerFactory.getLogger(ConsumerApp.class);
    
    public static void main(String[] args) {
        // Set the log file name for log4j configuration
        System.setProperty("logfile.name", "consumer");

        // Define properties for the Kafka consumer
        Properties props = new Properties();
        
        // List of Kafka brokers for bootstrap; ensures high availability and failover
        props.put("bootstrap.servers", "localhost:9092,localhost:9094,localhost:9096");
        
        // Consumer group ID to coordinate with other consumers for load balancing and fault tolerance
        props.put("group.id", "failover-group");

        props.put("reconnect.backoff.ms", "5000");
        
        // Specify deserializer for the key (String format in this case)
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        // Specify deserializer for the value (String format in this case)
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        // Automatically reset the offset to the earliest available message if no offset is present
        props.put("auto.offset.reset", "earliest");

        // Initialize the Kafka consumer with the configured properties
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // Subscribe to the specified topic; using singletonList for single topic subscription
            consumer.subscribe(Collections.singletonList("failover-test"));

            // Log the start of the consumer
            logger.info("Consumer started and listening for messages...");

            // Continuously poll for new messages from the topic
            while (true) {
                // Poll for records with a timeout; non-blocking wait for up to 1000 milliseconds
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    // Log each received message value for monitoring and debugging
                    logger.info("Received Message: " + record.value());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
