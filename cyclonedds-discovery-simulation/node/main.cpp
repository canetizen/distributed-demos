/*
 * Author: canetizen
 * Created on Sat Jun 14 2025
 * Description: Cyclone DDS discovery mechanism simulation.
 */

#include <dds/dds.h>               // Cyclone DDS main API
#include "SensorData.h"            // IDL-generated data type

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <signal.h>
#include <pthread.h>
#include <stdarg.h>

// Global flag used for graceful shutdown
volatile int running = 1;

// File pointer for logging
FILE* log_fp = NULL;

// Handle Ctrl+C to stop the application cleanly
void handle_sigint(int sig) {
    (void)sig;
    running = 0;
}

// Simple logger that prints to log file or stdout
void logf(const char* format, ...) {
    va_list args;
    va_start(args, format);
    if (log_fp) {
        vfprintf(log_fp, format, args);
        fflush(log_fp);
    } else {
        vfprintf(stdout, format, args);
        fflush(stdout);
    }
    va_end(args);
}

// Set up a log file for the current node
bool setup_logging(const char* node_name) {
    char log_path[256];
    snprintf(log_path, sizeof(log_path), "/logs/%s.log", node_name);
    log_fp = fopen(log_path, "w");
    if (!log_fp) {
        fprintf(stderr, "[ERROR] Cannot open log file: %s\n", log_path);
        return false;
    }
    logf("[INFO] Logging started for %s at %s\n", node_name, log_path);
    return true;
}

// Create a default QoS configuration with reliable delivery and buffer limits
dds_qos_t* create_default_qos() {
    dds_qos_t* qos = dds_create_qos();
    dds_qset_reliability(qos, DDS_RELIABILITY_RELIABLE, DDS_SECS(10)); // reliable delivery
    dds_qset_history(qos, DDS_HISTORY_KEEP_LAST, 100); // keep last 100 samples
    dds_qset_resource_limits(qos, 100, 100, 100); // buffer limits
    return qos;
}

// Holds all DDS entities and node metadata
typedef struct {
    dds_entity_t participant;
    dds_entity_t writer;
    dds_entity_t reader;
    const char* node_name;
} DDSContext;

// Initialize DDS entities: participant, topic, writer, reader
DDSContext create_dds_context(const char* node_name, dds_qos_t* qos) {
    DDSContext ctx = { 0 };
    ctx.node_name = node_name;

    // Create participant (represents this node)
    ctx.participant = dds_create_participant(DDS_DOMAIN_DEFAULT, NULL, NULL);
    if (ctx.participant < 0) {
        logf("[ERROR] dds_create_participant failed: %d\n", ctx.participant);
        return ctx;
    }

    // Create topic (shared name and type across nodes)
    dds_entity_t topic = dds_create_topic(ctx.participant, &SensorData_desc, "SensorTopic", NULL, NULL);
    if (topic < 0) {
        logf("[ERROR] dds_create_topic failed: %d\n", topic);
        dds_delete(ctx.participant);
        ctx.participant = -1;
        return ctx;
    }

    // Create publisher
    ctx.writer = dds_create_writer(ctx.participant, topic, qos, NULL);
    if (ctx.writer < 0) {
        logf("[ERROR] dds_create_writer failed: %d\n", ctx.writer);
        dds_delete(ctx.participant);
        ctx.participant = -1;
        return ctx;
    }

    // Create subscriber
    ctx.reader = dds_create_reader(ctx.participant, topic, qos, NULL);
    if (ctx.reader < 0) {
        logf("[ERROR] dds_create_reader failed: %d\n", ctx.reader);
        dds_delete(ctx.participant);
        ctx.participant = -1;
    }

    return ctx;
}

// Cleanup DDS entities and log file
void cleanup_dds(DDSContext* ctx) {
    if (ctx && ctx->participant > 0) {
        dds_delete(ctx->participant);
    }
    if (log_fp) {
        fclose(log_fp);
    }
}

// Publishes a new SensorData message every 2 seconds
void* publisher_thread(void* arg) {
    DDSContext* ctx = (DDSContext*)arg;

    while (running) {
        SensorData data = { 0 };
        data.sensor_id = strdup(ctx->node_name);
        data.temperature = 20.0f + (rand() % 1000) / 100.0f;
        data.humidity = 30.0f + (rand() % 1000) / 100.0f;
        data.timestamp = (int32_t)time(NULL);

        dds_return_t ret = dds_write(ctx->writer, &data);
        if (ret < 0) {
            logf("[ERROR] %s: dds_write failed: %d\n", ctx->node_name, ret);
        } else {
            logf("[PUBLISHED] %s: %.2f°C, %.2f%%, ts=%d\n",
                  data.sensor_id, data.temperature, data.humidity, data.timestamp);
        }

        free(data.sensor_id);
        sleep(2); // wait before sending next message
    }

    return NULL;
}

// Receives incoming SensorData messages and logs them.
void* subscriber_thread(void* arg) {
    DDSContext* ctx = (DDSContext*)arg;

    while (running) {
        void* samples[1] = { NULL };
        dds_sample_info_t infos[1];

        // Attempt to take one sample
        dds_return_t ret = dds_take(ctx->reader, samples, infos, 1, 1);
        if (ret > 0 && infos[0].valid_data && samples[0]) {
            SensorData* sample = (SensorData*)samples[0];
            // Filter out self-published messages
            if (sample->sensor_id && ctx->node_name &&
                strcmp(sample->sensor_id, ctx->node_name) != 0) {
                logf("[RECEIVED] %s: %.2f°C, %.2f%%, ts=%d\n",
                      sample->sensor_id, sample->temperature, sample->humidity, sample->timestamp);
            }
        }

        // Release sample memory loaned by DDS
        if (ret > 0) {
            dds_return_loan(ctx->reader, samples, ret);
        }

    }

    return NULL;
}

int main() {
    signal(SIGINT, handle_sigint);           // Handle Ctrl+C
    srand((unsigned int)time(NULL));         // Seed RNG for sensor data

    const char* node_name = getenv("NODE_NAME");
    if (!node_name) node_name = "nodeX";

    // Set up logging to file
    if (!setup_logging(node_name)) {
        return EXIT_FAILURE;
    }

    logf("[INFO] Starting %s...\n", node_name);

    // Set up QoS (reliability, history, buffer limits)
    dds_qos_t* qos = create_default_qos();

    // Initialize DDS participant, topic, writer, reader
    DDSContext ctx = create_dds_context(node_name, qos);
    dds_delete_qos(qos); // QoS no longer needed after use

    if (ctx.participant < 0) {
        return EXIT_FAILURE;
    }

    logf("[DEBUG] %s: Waiting for DDS discovery...\n", node_name);
    sleep(5); // wait for other participants to be discovered

    pthread_t pub_thread, sub_thread;

    // Start publisher and subscriber threads
    if (pthread_create(&pub_thread, NULL, publisher_thread, &ctx) != 0 ||
        pthread_create(&sub_thread, NULL, subscriber_thread, &ctx) != 0) {
        logf("[ERROR] Failed to create threads\n");
        cleanup_dds(&ctx);
        return EXIT_FAILURE;
    }

    // Wait for threads to finish (they stop on SIGINT)
    pthread_join(pub_thread, NULL);
    pthread_join(sub_thread, NULL);

    logf("[INFO] Shutting down %s...\n", node_name);
    cleanup_dds(&ctx);

    return EXIT_SUCCESS;
}
