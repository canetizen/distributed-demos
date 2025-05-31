# distributed-demos

**Description:**
A collection of example projects demonstrating core concepts in **distributed systems** such as leader election, coordination, failover, and consensus.
All projects use real-world technologies like **Apache ZooKeeper**, and are structured to emphasize clarity, and correctness.

---

## Table of Contents

* [Repository Structure](#repository-structure)
* [Build and Run Instructions](#build-and-run-instructions)
* [Projects](#projects)
  * [zookeeper\-leader\-election\-algorithm](#zookeeper-leader-election-algorithm)
  * [kafka\-failover\-simulation](#kafka\-failover\-simulation)
  * [haproxy\-load\-balancing\-simulator](#haproxy\-load\-balancing\-simulator)
  
---

## Repository Structure

```plaintext
distributed-demos/
├── example_project/              # Java Maven project
│   ├── src/                      # Java source files
│   ├── pom.xml                   # Maven build file
├── ...                           # Other projects
├── README.md                     # This file
```

---

## Build and Run Instructions

Each project is independently buildable and runnable.
For Java-based projects, Maven is used as the build system.

### 1. Install Requirements

* Java (version 17+ recommended)
* Apache ZooKeeper running at `localhost:2181`
* Maven (`sudo apt install maven` on Ubuntu)

---

### 2. Build a Project

Navigate to the project folder (e.g., `zookeeper_leader_election_algorithm`) and run:

```bash
mvn clean package
```

This will produce a runnable JAR under `target/`.

---

### 3. Run a Project

Example:

```bash
java -jar target/zookeeper-leader-election-algorithm.jar
```

You can run multiple instances from different terminals to simulate multiple nodes participating in the leader election.

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