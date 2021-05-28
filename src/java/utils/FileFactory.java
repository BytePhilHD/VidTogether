package utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class FileFactory {

    public JSONObject load(File file) throws IOException {

            FileReader reader = new FileReader(file);
            StringBuilder builder = new StringBuilder();

            int read;

            while ((read = reader.read()) != -1) {
                builder.append((char) read);
            }

            reader.close();

            return new JSONObject(builder.toString());

    }

    public JSONObject loadJoin(File... files) {
        JSONObject jsonObject = new JSONObject();

        Arrays.stream(files).map(file -> {
            try {
                return load(file).toMap();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).forEach(map -> map.keySet().forEach(key -> jsonObject.put(key, map.get(key))));
        return jsonObject;
    }

    public void save(File file, JSONObject jsonObject) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonObject.toString());
            writer.close();
        } catch (Exception ex) {
            System.out.println("Datei konnte nicht gespeichert werden! [" + file.getName() + "]");
        }
    }

}
