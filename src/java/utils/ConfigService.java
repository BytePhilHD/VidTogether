package utils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ConfigService {

    private final FileFactory factory;
    private final JSONObject configuration;
    private final boolean first;

    public ConfigService() throws IOException {
        this.factory = new FileFactory();
        File file = new File("database");

        if (!file.exists())
            file.mkdirs();

        file = new File("user");

        if (!file.exists())
            file.mkdirs();

        file = new File("backup");

        if (!file.exists())
            file.mkdir();

        file = new File("backup/temp");

        if (!file.exists())
            file.mkdirs();

        file = new File("config.json");

        if (!file.exists()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("AutoUpdate", true);
            jsonObject.put("Port", 2291);
            jsonObject.put("SecondaryBackup", true);
            jsonObject.put("UpdateServer", "https://joker-games.org/lib/myjfql/rest.json");

            factory.save(file, jsonObject);
            first = true;
        } else
            first = false;

        this.configuration = factory.load(file);

        if (configuration.opt("SecondaryBackup") == null)
            configuration.put("SecondaryBackup", true);
    }

    public boolean isFirst() {
        return first;
    }

    public JSONObject getConfiguration() {
        return configuration;
    }

    public FileFactory getFactory() {
        return factory;
    }
}