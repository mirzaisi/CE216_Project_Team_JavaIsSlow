package com.playforgemanager.football;

import com.playforgemanager.core.Tactic;

import java.util.Objects;

public class BootstrapFootballTactic implements Tactic {
    private final String name;

    public BootstrapFootballTactic(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tactic name cannot be blank.");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}