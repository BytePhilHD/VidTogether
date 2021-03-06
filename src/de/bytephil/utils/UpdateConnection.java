package de.bytephil.utils;

import de.bytephil.app.App;
import de.bytephil.enums.MessageType;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UpdateConnection {

    private JSONObject jsonObject;

    public void connect(final String url) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();

        int i;

        while ((i = reader.read()) != -1) {
            builder.append((char) i);
        }

        this.jsonObject = new JSONObject(builder.toString());
    }

    public boolean latestIsBeta() {
        return jsonObject.getString("Version").endsWith("-BETA")
                || jsonObject.getString("Version").endsWith("-SNAPSHOT");
    }

    public boolean isLatest() {
        if (jsonObject == null)
            return true;

        final String version = jsonObject.getString("Version");
        final String current = App.getInstance().getVersion();

        if (current.compareTo(version) > 0) {
            return true;
        }
        else
            return version.equals(current);
    }

    public boolean isMaintenance() {
        if (jsonObject == null)
            return false;

        return jsonObject.getBoolean("Maintenance");
    }

    public String getDownload() {
        if (jsonObject == null)
            return null;

        return jsonObject.getString("Download");
    }

    public JSONObject getResponse() {
        return jsonObject;
    }

    public static class Downloader {

        private final UpdateConnection connection;

        public Downloader(final UpdateConnection connection) {
            this.connection = connection;
        }

        public void download() {
            final int j = 1024;

            try {
                final URL url = new URL(connection.getDownload());
                final File file = new File("VidTogether.jar");

                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                final BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                final FileOutputStream fileOutputStream = new FileOutputStream(file);
                final BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream, j);

                de.bytephil.utils.Console.printout("Youre not on the latest Version!", MessageType.WARNING);
                de.bytephil.utils.Console.printout("Starting download...", MessageType.INFO);

                final byte[] bytes = new byte[j];
                int read;

                while ((read = inputStream.read(bytes, 0, j)) >= 0) {
                    outputStream.write(bytes, 0, read);
                }

                outputStream.close();
                inputStream.close();

                de.bytephil.utils.Console.printout("Download completed.", MessageType.INFO);
                de.bytephil.utils.Console.printout("Shutting down...", MessageType.WARNING);
                System.exit(1);
            } catch (Exception ex) {
                de.bytephil.utils.Console.printout("Network error", MessageType.ERROR);
            }
        }

    }

}