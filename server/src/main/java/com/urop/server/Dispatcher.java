package com.urop.server;

//import com.esotericsoftware.kryonet.Connection;

import com.urop.common.Connection;
import com.urop.common.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.urop.server.Server.logAppend;


public class Dispatcher implements Runnable {

    List<Connection> availNodes;
    List<Connection> busyNodes;
    volatile List<Task> pendingTasks;
    volatile Map<Connection, Task> executingTasks;
    volatile Collection<String> finishedTasks;
//    volatile Map<Connection, Integer> realWorkingTime;

    Dispatcher() {
        availNodes = new LinkedList<>();
        busyNodes = new LinkedList<>();
        pendingTasks = new LinkedList<>();
        executingTasks = new HashMap<>();
        finishedTasks = new HashSet<>();
//        realWorkingTime = new HashMap<>();
    }

    public void dispatch() {
        //noinspection InfiniteLoopStatement
        while (true) {
            synchronized (this) {
                while (!availNodes.isEmpty() && !pendingTasks.isEmpty()) {
                    Connection avail = availNodes.remove(0);

                    Task t = pendingTasks.remove(0);
                    avail.send(t);
//                    logAppend(t.id + " sent");
                    busyNodes.add(avail);
                    executingTasks.put(avail, t);
//                    logAppend(t.meta + " added to executing");
                }
//                logAppend("wait");
                safeWait();
            }
        }
    }


    public synchronized boolean isAllTasksFinished() {
        return executingTasks.isEmpty() && pendingTasks.isEmpty();
    }

    public synchronized boolean isTaskFinished(String task) {
        return finishedTasks.contains(task);
    }

    public synchronized void blockUntilAllTasksFinish() {
        while (!isAllTasksFinished()) {
            safeWait();
        }
    }

    public synchronized void blockUntilSomeNodeConnect(int wait) {
        while (numOfNode() < wait) {
            logAppend("Waiting for any nodes to connect...");
            safeWait();
        }
    }

    public synchronized boolean commitTask(Connection conn, Task t) {
        if (!busyNodes.remove(conn)) {
            logAppend("busyNodes removal failed!");
            return false;
        }
        availNodes.add(conn);

        Task rem = executingTasks.remove(conn);
        if (rem == null) {
            logAppend("executingTasks removal failed!");
            logAppend("Task: " + t.getClass().getName() + " " + t.id);
            return false;
        } else {
            if (!rem.id.equals(t.id)) {
                logAppend(rem.id + " and " + t.id + " not equal!");
            }
//            logAppend(t.meta + " removed from executing");
        }
        finishedTasks.add(t.id);

//        Integer ws = realWorkingTime.getOrDefault(conn, 0);
//        ws += t.waitCount;
//        realWorkingTime.put(conn, ws);

        this.notifyAll();

//        printTaskCount();
        return true;
    }

    public synchronized void addAvailNode(Connection node) {
        availNodes.add(node);
        this.notifyAll();
    }


    public synchronized void addPendingTask(Task t) {
        pendingTasks.add(t);
        this.notifyAll();
    }

    public synchronized void removeNode(Connection conn) {
        availNodes.remove(conn);
        busyNodes.remove(conn);
        Task t = executingTasks.remove(conn);
        if (t != null) {
            pendingTasks.add(t);
        }
    }


    public synchronized int numOfNode() {
        return availNodes.size() + busyNodes.size();
    }


    public void broadcast(Task t) {
        Consumer<Connection> r = (conn) -> conn.send(t);
        availNodes.forEach(r);
        busyNodes.forEach(r);
    }

    @Override
    public void run() {
        dispatch();
    }

    public void safeWait() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
