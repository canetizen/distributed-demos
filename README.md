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