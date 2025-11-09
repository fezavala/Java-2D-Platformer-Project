package io;

import org.json.*;
import java.io.*;

// Class that loads the games save data
public class GameLoader {
    private static final String SAVE_PATH = "/saves/save_data.json";

    public static JSONObject loadSaveData() {
        try (InputStreamReader isr = new InputStreamReader(GameLoader.class.getResourceAsStream(SAVE_PATH))) {
            BufferedReader br = new BufferedReader(isr);
            JSONObject saveData = new JSONObject(br.readLine());  // Save data only on 1 line
            br.close();
            return saveData;
        } catch (NullPointerException e) {
            System.out.println("Save data not found, a new save file will be created later.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
