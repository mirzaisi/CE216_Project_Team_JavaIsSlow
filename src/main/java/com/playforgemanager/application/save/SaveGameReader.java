package com.playforgemanager.application.save;

import java.io.IOException;
import java.nio.file.Path;

public interface SaveGameReader {
    SaveGameDocument read(Path savePath) throws IOException;
}
