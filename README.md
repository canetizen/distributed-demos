# distributed-demos

**Description:**
A curated set of example projects that illustrate foundational ideas in distributed systems—such as coordination, leader election, failover, consensus, and scalable load balancing—using real-world, industry-standard tools and frameworks. The projects are designed for clarity, correctness, and ease of experimentation.

---

## Table of Contents

* [Repository Structure](#repository-structure)
* [Install Requirements](#install-requirements)
* [Build and Run a Project](#build-and-run-a-project)
* [Projects](#projects)
  * [zookeeper\-leader\-election\-algorithm](#zookeeper-leader-election-algorithm)
  * [kafka\-failover\-simulation](#kafka\-failover\-simulation)
  * [haproxy\-load\-balancing\-simulator](#haproxy\-load\-balancing\-simulator)
  * [cyclonedds\-discovery\-simulation](#cyclonedds\-discovery\-simulation)
  
---

## Repository Structure

```plaintext
distributed-demos/
├── example_project/                  # Example project folder (language and tech may vary)
│   ├── src/                          # Source files (Java, Python, etc.)
│   ├── project config/build files    # (pom.xml, requirements.txt, Dockerfile, etc.)
│   ├── ...                           # Other project-related directories/files
├── ...                               # Additional projects
├── README.md                         # This file
```

---

### Install Requirements

* Java (version 17+ recommended)
* Maven (for Java projects)

---

### Build and Run a Project
Navigate to the desired project folder (e.g., `zookeeper_leader_election_algorithm`) and build the project using the appropriate tool for the language or framework. For example, for a Maven-based Java project:

```bash
mvn clean package
java -jar target/zookeeper-leader-election-algorithm.jar
```

For other languages or environments, use the corresponding build and run commands (such as `python`, `go run`, `npm install && npm start`, etc.).

Unless otherwise specified (e.g., via a provided runner script or Docker Compose configuration), projects can be built and executed using the standard workflow for their respective technology.

---

## Projects

### zookeeper\-leader\-election\-algorithm

**Description:**
A distributed leader election demo built using **Apache ZooKeeper**.
Each instance connects to ZooKeeper, creates an ephemeral sequential znode, and monitors the znode before it to detect leadership changes.
This avoids the **herd effect**, ensures **failover safety**, and demonstrates a realistic coordination pattern in distributed systems.

#### Key Features:

* Ephemeral sequential znodes for ordering
* Watch-based failover detection
* Only watches the previous znode to prevent herd effect
* Clean abstraction using a `LeaderElectionHandler` interface
* Maven-managed, runnable `.jar`

---

**How to Build:**

```bash
cd zookeeper_leader_election
mvn clean package
```

**How to Run:**

```bash
java -jar target/zookeeper-leader-election-algorithm.jar
```

To simulate failover, start multiple terminals, run the app in each, then stop the leader (first one started).
The next node detects the failure and takes leadership.

---

### kafka-failover-simulation

**Description:**
A distributed Kafka failover simulation built with **Java** and **Docker Compose**.
All build, startup, and failover scenarios are managed by a single shell script.
The project demonstrates how producers and consumers respond to broker failures and recover automatically in a real-world, containerized Kafka environment.

#### Key Features:

* Single script (`run-failover.sh`) automates build, environment setup, and execution
* Simulates broker failures and automatic client recovery
* Java-based Kafka producer and consumer implementations
* Docker Compose for isolated, reproducible local Kafka setup
* Logs output to `logs/` directory for analysis

---

**How to Build and Run:**

```bash
./run-failover.sh
```

No manual steps are required. The script will:

* Build the project with Maven
* Start Kafka and Zookeeper with Docker Compose
* Launch both producer and consumer clients
* Simulate failover scenarios and output logs

While the program is running, you can examine the logs to observe the behavior of the consumer and producer during failover transitions.

---

### haproxy-load-balancing-simulator

**Description:**
A comprehensive load balancing simulation project built with **Java**, **HAProxy**, and **Docker Compose**.
This project demonstrates HTTP and TCP load balancing, health checks, and advanced failover behaviors using multiple backend services and a backup server.
You can easily observe how HAProxy distributes traffic, handles server failures, and automatically routes requests to backup infrastructure, all within an isolated container environment.

#### Key Features:

* Supports both **Layer 4 (TCP)** and **Layer 7 (HTTP)** load balancing with separate HAProxy configurations
* Path-based routing and per-endpoint failover logic for HTTP requests (Layer 7)
* Transparent round robin balancing and failover for generic TCP traffic (Layer 4)
* Automatic backup server activation in case of backend failures
---

**How to Build and Run:**

```bash
docker compose up --build
```

All components will be built and started automatically. This includes:

* Three main Java backend servers and a dedicated backup server
* HAProxy in both Layer 4 (TCP) and Layer 7 (HTTP) proxy modes
* Pre-configured routing and health check scenarios

---

**How to Simulate Failover and Load Balancing:**

* **Test HTTP Layer 7:**
  Send requests to [http://localhost:8087/](http://localhost:8087/) with different endpoints (e.g., `/api/hello`, `/auth/hello`, `/other/hello`, `/`)
  Observe how traffic is routed according to endpoint and how backup takes over if a primary server fails.

* **Test TCP Layer 4:**
  Send requests to [http://localhost:8084/](http://localhost:8084/) and see round robin balancing among healthy servers.
  When all primary servers are down, backup-server automatically handles all traffic.

* **Simulate Failover:**
  While the system is running, stop any backend server using:

  ```bash
  docker stop server2
  ```

  or

  ```bash
  docker stop server1
  ```

  Watch as HAProxy reroutes traffic to the remaining healthy servers or backup according to your configuration.

* **Logs and Troubleshooting:**
  All HAProxy and backend logs can be viewed using:

  ```bash
  docker logs <container_name>
  ```

---

## cyclonedds-discovery-simulation

A distributed sensor communication demo built using **Eclipse Cyclone DDS**, implemented in **C++**.
Each node functions as both a **publisher** (emitting synthetic sensor readings) and a **subscriber** (receiving others' messages), creating a decentralized publish-subscribe mesh.

> **Eclipse Cyclone DDS** is a high-performance, open-source implementation of the **Data Distribution Service (DDS)** standard — designed for scalable, real-time, and fault-tolerant communication in distributed systems such as robotics, IoT, and industrial automation.

The primary goal is to simulate **how DDS handles dynamic node participation**, particularly focusing on the behavior of **late-joining participants** — i.e., nodes that join an already active DDS network.
This provides insight into **discovery propagation**, **data availability**, and **communication stability** in real-time distributed systems.

---

### Core Concepts

* **Data-Centric Pub-Sub:** All nodes communicate through a shared DDS topic (`SensorTopic`) without point-to-point configuration.
* **Peer Discovery:** Eclipse Cyclone DDS automatically discovers other participants using multicast or shared configurations.
* **Late Join Simulation:** One node (`node4`) deliberately joins after others are already publishing and subscribing, testing how well discovery and integration work in practice.
* **Resilient Messaging:** DDS ensures reliable delivery using `RELIABLE` QoS and bounded history (`KEEP_LAST`), helping new nodes catch up with ongoing traffic.
* **Per-Node Logging:** Each node records its own publish/receive events into a dedicated log file (`/logs/<node>.log`), making interaction patterns traceable.

---

### Simulation Flow

1. Nodes node1, node2, and node3 start at the same time and immediately begin publishing and subscribing to sensor data.

2. Each node publishes synthetic temperature and humidity readings every 2 seconds.

3. For the first 10 seconds, these three nodes communicate exclusively with each other, exchanging messages seamlessly.

4. After a 10-second delay, node4 joins the network, simulating a late joiner.

5. Thanks to DDS dynamic discovery, node4 is integrated automatically and begins both publishing its own messages and receiving data from the existing nodes.

6. All nodes are designed to ignore their own messages and only log those published by other participants.

7. Throughout the simulation, no disruption, delay, or message loss is observed — the system remains stable and synchronized even after the late join.

---

### Folder Structure

```
├── node/                  # Application source code (main.cpp, CMakeLists.txt, Dockerfile)
│
├── shared/                # Shared configuration and IDL files
│   ├── SensorData.idl     # IDL definition for SensorData type
│   └── cyclonedds.xml     # Cyclone DDS participant discovery config
│
├── logs/                  # Mounted directory where each node writes its own log file
│
├── docker-compose.yml     # Defines and orchestrates DDS nodes as containers
├── Makefile               # Provides helper commands (up, clean, logs)

```
---

### How to Build

Make sure Docker and Docker Compose are installed.

```bash
make up
```

This command:

* Builds all Docker images
* Launches the four DDS nodes
* Ensures shared logs and config files are mounted

---

### How to Clean Up

```bash
make clean
```

Removes all running containers and associated volumes.

---

### View Logs

To inspect logs from a specific node:

```bash
make logs NODE=node2
```

This shows messages published and received by `node2`.

Log files are also persisted to the host under:

```
./logs/node1.log
./logs/node2.log
...
```

---
