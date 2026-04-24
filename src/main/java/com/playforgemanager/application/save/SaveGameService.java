package com.playforgemanager.application.save;

import com.playforgemanager.core.GameSession;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class SaveGameService {
    private final SaveGameDocumentMapper documentMapper;
    private final SaveGameWriter writer;
    private final Clock clock;

    public SaveGameService(SaveGameWriter writer) {
        this(new SaveGameDocumentMapper(), writer, Clock.systemUTC());
    }

    public SaveGameService(SaveGameDocumentMapper documentMapper, SaveGameWriter writer, Clock clock) {
        this.documentMapper = Objects.requireNonNull(documentMapper, "Document mapper cannot be null.");
        this.writer = Objects.requireNonNull(writer, "Save writer cannot be null.");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null.");
    }

    public SaveGameResult save(GameSession session, Path savePath) throws IOException {
        Objects.requireNonNull(savePath, "Save path cannot be null.");

        SaveGameDocument document = documentMapper.toDocument(session);
        writer.write(document, savePath);

        SaveSessionData savedSession = document.session();
        return new SaveGameResult(
                savePath.toAbsolutePath().normalize(),
                document.formatId(),
                document.formatVersion(),
                savedSession.sportId(),
                savedSession.controlledTeamId(),
                Instant.now(clock)
        );
    }
}
