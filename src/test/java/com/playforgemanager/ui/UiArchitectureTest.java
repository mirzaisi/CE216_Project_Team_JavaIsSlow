package com.playforgemanager.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UiArchitectureTest {

    @Test
    void uiClassesDoNotImportFootballOrHandballPackages() throws IOException {
        Path uiRoot = Path.of("src", "main", "java", "com", "playforgemanager", "ui");
        List<Path> violations;
        try (Stream<Path> sourceFiles = Files.walk(uiRoot)) {
            violations = sourceFiles
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::importsConcreteSportPackage)
                    .toList();
        }

        assertTrue(violations.isEmpty(), "UI files must use shared services only: " + violations);
    }

    private boolean importsConcreteSportPackage(Path path) {
        try {
            String source = Files.readString(path);
            return source.contains("import com.playforgemanager.football.")
                    || source.contains("import com.playforgemanager.handball.");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read source file: " + path, exception);
        }
    }
}
