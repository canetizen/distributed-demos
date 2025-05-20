package com.github.canetizen.zookeeper_leader_election.impl;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import com.github.canetizen.zookeeper_leader_election.service.LeaderElectionHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author canetizen
 * @date May 20, 2025
 * 
 * Description: Leader election logic.
 */
public class LeaderElectionClient implements Watcher {

    private static final String ELECTION_NAMESPACE = "/election";
    private final ZooKeeper zooKeeper;
    private final LeaderElectionHandler handler;
    private String currentZnodeName;

    /**
     * Creates a new LeaderElectionClient with ZooKeeper connection and election event handler.
     *
     * @param connectString ZooKeeper connection string (e.g., localhost:2181)
     * @param handler Callback handler for leader election events
     */
    public LeaderElectionClient(String connectString, LeaderElectionHandler handler) throws IOException {
        this.zooKeeper = new ZooKeeper(connectString, 1000, this); // 1000ms session timeout.
        this.handler = handler;
    }

    /**
     * Starts the leader election process: ensures base znode, creates ephemeral sequential node, and attempts leadership.
     */
    public void start() throws Exception {
        ensureElectionZnode();
        createElectionZnode();
        attemptLeadership();
    }

    /**
     * Gracefully shuts down the ZooKeeper client.
     */
    public void stop() throws Exception {
        zooKeeper.close();
    }

    /**
     * Ensures that the election base path exists in ZooKeeper.
     */
    private void ensureElectionZnode() throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(ELECTION_NAMESPACE, false);
        if (stat == null) {
            zooKeeper.create(ELECTION_NAMESPACE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * Creates an ephemeral sequential node under the election namespace.
     * This node will be used to determine the order in leader election.
     */
    private void createElectionZnode() throws KeeperException, InterruptedException {
        String znodeFullPath = zooKeeper.create(
            ELECTION_NAMESPACE + "/c_",
            new byte[]{},
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL
        );
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
        System.out.println("Created znode: " + currentZnodeName);
    }

    /**
     * Attempts to determine if this node is the leader.
     * If not, it sets a watch on the previous znode in sequence.
     */
    private void attemptLeadership() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);
        int index = children.indexOf(currentZnodeName);

        if (index == 0) {
            // This node has the lowest sequence number → it's the leader
            System.out.println("I am the leader: " + currentZnodeName);
            handler.onLeader();
            return;
        }

        // Watch the znode with the next-lowest sequence number to avoid herd effect
        String previousZnodeName = children.get(index - 1);
        System.out.println("Not the leader. Watching previous node: " + previousZnodeName);
        handler.onWorker();

        Stat stat = zooKeeper.exists(
            ELECTION_NAMESPACE + "/" + previousZnodeName,
            watchedEvent -> {
                // When the previous znode is deleted, re-attempt leadership
                if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
                    try {
                        attemptLeadership();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        // If the watched node no longer exists (race condition), retry immediately
        if (stat == null) {
            attemptLeadership();
        }
    }

    /**
     * Processes global ZooKeeper events such as connection state changes.
     */
    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.Disconnected) {
            System.out.println("Disconnected from ZooKeeper");
            handler.onDisconnected();
        }
    }
}
