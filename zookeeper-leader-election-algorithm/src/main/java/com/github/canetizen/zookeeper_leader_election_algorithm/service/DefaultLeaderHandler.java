package com.github.canetizen.zookeeper_leader_election_algorithm.service;

/**
 * @author canetizen
 * @date May 20, 2025
 * 
 * Description: Node type interface.
 */
public class DefaultLeaderHandler implements LeaderElectionHandler {

    @Override
    public void onLeader() {
        System.out.println("[LEADER] I'm the leader. Running leader-specific tasks.");
    }

    @Override
    public void onWorker() {
        System.out.println("[WORKER] I'm in standby mode.");
    }

    @Override
    public void onDisconnected() {
        System.out.println("[DISCONNECTED] Lost connection to ZooKeeper.");
    }
}
