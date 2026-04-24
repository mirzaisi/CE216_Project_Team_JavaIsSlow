package com.playforgemanager.application.save;

import java.io.IOException;
import java.nio.file.Path;

public interface SaveGameWriter {
    void write(SaveGameDocument document, Path savePath) throws IOException;
}
