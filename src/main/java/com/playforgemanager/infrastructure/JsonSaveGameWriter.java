package com.playforgemanager.infrastructure;

import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class JsonSaveGameWriter implements SaveGameWriter {
    private final SaveGameJsonSerializer serializer;

    public JsonSaveGameWriter() {
        this(new SaveGameJsonSerializer());
    }

    JsonSaveGameWriter(SaveGameJsonSerializer serializer) {
        this.serializer = Objects.requireNonNull(serializer, "JSON serializer cannot be null.");
    }

    @Override
    public void write(SaveGameDocument document, Path savePath) throws IOException {
        Objects.requireNonNull(document, "Save document cannot be null.");
        Objects.requireNonNull(savePath, "Save path cannot be null.");

        Path normalizedPath = savePath.toAbsolutePath().normalize();
        Path parentDirectory = normalizedPath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        Path tempDirectory = parentDirectory == null ? Path.of(".").toAbsolutePath().normalize() : parentDirectory;
        Path tempFile = Files.createTempFile(tempDirectory, normalizedPath.getFileName().toString(), ".tmp");

        try {
            Files.writeString(tempFile, serializer.serialize(document), StandardCharsets.UTF_8);
            try {
                Files.move(
                        tempFile,
                        normalizedPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempFile, normalizedPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
