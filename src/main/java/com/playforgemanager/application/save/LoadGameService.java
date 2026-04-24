package com.playforgemanager.application.save;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.core.GameSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class LoadGameService {
    private final SaveGameReader reader;
    private final SportRegistry sportRegistry;
    private final SaveGameRestorationRegistry restorationRegistry;

    public LoadGameService(
            SaveGameReader reader,
            SportRegistry sportRegistry,
            SaveGameRestorationRegistry restorationRegistry
    ) {
        this.reader = Objects.requireNonNull(reader, "Save reader cannot be null.");
        this.sportRegistry = Objects.requireNonNull(sportRegistry, "Sport registry cannot be null.");
        this.restorationRegistry = Objects.requireNonNull(
                restorationRegistry,
                "Save restoration registry cannot be null."
        );
    }

    public GameSession load(Path savePath) throws IOException {
        SaveGameDocument document = reader.read(savePath);
        validateDocument(document);

        SaveSessionData savedSession = document.session();
        SportRegistration registration = sportRegistry.getRegistration(savedSession.sportId());
        return restorationRegistry
                .getRestorer(savedSession.sportId())
                .restore(document, registration);
    }

    private void validateDocument(SaveGameDocument document) {
        Objects.requireNonNull(document, "Save document cannot be null.");
        if (!SaveGameFormat.FORMAT_ID.equals(document.formatId())) {
            throw new IllegalArgumentException("Unsupported save format: " + document.formatId());
        }
        if (document.formatVersion() != SaveGameFormat.CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported save version: " + document.formatVersion());
        }
    }
}
