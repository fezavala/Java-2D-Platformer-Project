package io;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Class that Saves the games current progress
public class GameSaver {
    private static final String SAVE_PATH = "assets/saves/save_data.json";

    public static void saveGame(JSONObject levelSaveData) {
        try {
            Path path = Paths.get(SAVE_PATH);

            // Create the file if it doesn't exist
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            // Write content to the file (overwriting existing content)
            Files.write(path, levelSaveData.toString().getBytes());

            System.out.println("File created/updated and written to successfully at: " + SAVE_PATH);

        } catch (IOException e) {
            System.err.println("An error occurred during file operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
