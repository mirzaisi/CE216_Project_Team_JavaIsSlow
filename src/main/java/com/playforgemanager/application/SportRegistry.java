package com.playforgemanager.application;

import com.playforgemanager.core.SportFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class SportRegistry {
    private final Map<String, SportRegistration> registrationsById;
    private final Map<String, SportRegistration> registrationsByChoice;

    public SportRegistry() {
        this.registrationsById = new LinkedHashMap<>();
        this.registrationsByChoice = new LinkedHashMap<>();
    }

    public SportRegistry register(SportRegistration registration) {
        SportRegistration validatedRegistration =
                Objects.requireNonNull(registration, "Sport registration cannot be null.");

        String normalizedId = normalize(validatedRegistration.getSportId());
        if (registrationsById.containsKey(normalizedId)) {
            throw new IllegalArgumentException(
                    "A sport is already registered with id: " + validatedRegistration.getSportId());
        }

        String normalizedDisplayName = normalize(validatedRegistration.getDisplayName());
        if (registrationsByChoice.containsKey(normalizedDisplayName)) {
            throw new IllegalArgumentException(
                    "A sport is already registered with display name: " + validatedRegistration.getDisplayName());
        }

        registrationsById.put(normalizedId, validatedRegistration);
        registrationsByChoice.put(normalizedId, validatedRegistration);
        registrationsByChoice.put(normalizedDisplayName, validatedRegistration);
        return this;
    }

    public SportRegistration getRegistration(String sportChoice) {
        String normalizedChoice = normalize(validateChoice(sportChoice));
        SportRegistration registration = registrationsByChoice.get(normalizedChoice);
        if (registration == null) {
            throw new IllegalArgumentException("Unknown sport choice: " + sportChoice);
        }
        return registration;
    }

    public SportFactory getFactory(String sportChoice) {
        return getRegistration(sportChoice).getSportFactory();
    }

    public List<SportRegistration> getRegisteredSports() {
        return List.copyOf(registrationsById.values());
    }

    private String validateChoice(String sportChoice) {
        String cleaned = Objects.requireNonNull(sportChoice, "Sport choice cannot be null.").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Sport choice cannot be blank.");
        }
        return cleaned;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
