package com.example.smarttasktracker;

public class Task {
    private int id;
    private String name;
    private double cost;
    private int expectedMinutes;
    private long actualSeconds;
    private long startTimestamp;
    private int status; // 0=Pending, 1=Running, 2=Completed

    public Task(int id, String name, double cost, int expectedMinutes, long actualSeconds, long startTimestamp, int status) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.expectedMinutes = expectedMinutes;
        this.actualSeconds = actualSeconds;
        this.startTimestamp = startTimestamp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public int getExpectedMinutes() {
        return expectedMinutes;
    }

    public long getActualSeconds() {
        return actualSeconds;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public int getStatus() {
        return status;
    }
}