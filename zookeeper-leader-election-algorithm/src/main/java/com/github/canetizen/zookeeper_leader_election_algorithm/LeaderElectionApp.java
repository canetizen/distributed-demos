package com.github.canetizen.zookeeper_leader_election_algorithm;

import com.github.canetizen.zookeeper_leader_election_algorithm.impl.LeaderElectionClient;
import com.github.canetizen.zookeeper_leader_election_algorithm.service.*;

/**
 * @author canetizen
 * @date May 20, 2025
 * 
 * Description: Client app.
 */
public class LeaderElectionApp {
    public static void main(String[] args) {
        try {
            LeaderElectionHandler handler = new DefaultLeaderHandler();
            LeaderElectionClient client = new LeaderElectionClient("localhost:2181", handler);

            client.start();

            System.out.println("ZooKeeper leader election started. Press Ctrl+C to exit.");
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
