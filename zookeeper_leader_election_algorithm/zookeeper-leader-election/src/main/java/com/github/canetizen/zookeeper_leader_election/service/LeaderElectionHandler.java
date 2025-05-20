package com.github.canetizen.zookeeper_leader_election.service;

/**
 * @author canetizen
 * @date May 20, 2025
 * 
 * Description: Election interface.
 */
public interface LeaderElectionHandler {
    void onLeader();
    void onWorker();
    void onDisconnected();
}
