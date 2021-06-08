package de.bytephil.utils;

import de.bytephil.enums.MessageType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfiguration extends Config {
    public boolean loaded = true;
    public ServerConfiguration(String path) {
        Properties prop = new Properties();
        String fileName = "server.config";
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            Console.printout("The config file is missing!", MessageType.WARNING);
            loaded = false;
        }
        try {
            prop.load(is);
        } catch (Exception ex) {
            Console.printout("Couldn't read config file", MessageType.WARNING);
            loaded = false;
        }
        port = Integer.parseInt(prop.getProperty("http.port", "80"));
        autoUpdate = Boolean.parseBoolean(prop.getProperty("app.autoUpdate", "true"));
        http = Boolean.parseBoolean(prop.getProperty("http.activated", "true"));
        https = Boolean.parseBoolean(prop.getProperty("ssl.activated", "false"));
        sslPort = Integer.parseInt(prop.getProperty("ssl.port", "443"));
        keystorePath = prop.getProperty("ssl.keystorePath", "keystore.jks");
        keystorePW = prop.getProperty("ssl.keystorePassword", "password");
    }
}
