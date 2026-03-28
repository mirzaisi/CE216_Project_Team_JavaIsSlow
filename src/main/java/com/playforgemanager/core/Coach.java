package com.playforgemanager.core;

public abstract class Coach {
    private final String id;
    private final String name;
    private final String role;

    protected Coach(String id, String name, String role) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Coach id cannot be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Coach name cannot be blank.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Coach role cannot be blank.");
        }
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}