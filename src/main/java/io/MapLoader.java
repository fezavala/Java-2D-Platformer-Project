package io;

import org.json.*;
import java.io.*;

// Class that loads a map, called by the TileMap class for further processing

public class MapLoader {
    private static final String MAP_DIR = "/levels/";
    private static final String FILE_TYPE = ".json";

    public static JSONObject loadMapJSON(String mapFile) {
        String filePath = MAP_DIR + mapFile + FILE_TYPE;
        try (InputStreamReader isr = new InputStreamReader(MapLoader.class.getResourceAsStream(filePath))) {
            BufferedReader br = new BufferedReader(isr);
            JSONObject map = new JSONObject(br.readLine());  // Entire map is on one line only
            br.close();
            return map;
        } catch (NullPointerException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

