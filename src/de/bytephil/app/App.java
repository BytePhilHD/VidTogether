package de.bytephil.app;

import de.bytephil.utils.Console;
import de.bytephil.utils.MessageType;
import de.bytephil.utils.ServiceState;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import jline.console.ConsoleReader;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import de.bytephil.utils.UpdateConnection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.*;
import de.bytephil.utils.ServerConfiguration;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;
    private static Javalin app;
    private ServerConfiguration config;

    private ConsoleReader reader = new ConsoleReader();
    private PrintWriter writer = new PrintWriter(reader.getOutput());

    public static App getInstance() {
        return instance;
    }

    public String version = "0.0.3";

    public HashMap<String, Session> sessionHashMap = new HashMap<>();
    public HashMap<String, WsConnectContext> sessionctx = new HashMap<>();
    public HashMap<Integer, byte[]> cachedImages = new HashMap<>();
    public List<String> sessions1 = new ArrayList<>();

    public boolean showProcesses = false;

    public ServiceState serviceState = ServiceState.STARTING;

    public App() throws IOException {
        instance = this;

        this.updateConnection = new UpdateConnection();
        this.downloader = new UpdateConnection.Downloader(updateConnection);
    }

    public void start() throws IOException {

        if (!new File("server.config").exists()) {
            de.bytephil.utils.Console.printout("The config file is missing!", MessageType.WARNING);
        }

        // Load config
        config = new ServerConfiguration("server.config");
        if (config.loaded) {
            Console.printout("Config was successfully loaded!", MessageType.INFO);
        } else {
            Console.printout("Config not loaded! Using default.", MessageType.WARNING);
        }

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
            config.server(() -> {
                Server server = new Server();
                ArrayList<Connector> connectors = new ArrayList<>();
                if (this.config.https) {
                    ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory());
                    sslConnector.setPort(this.config.sslPort);
                    connectors.add(sslConnector);
                }
                if (this.config.http) {
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(this.config.port);
                    connectors.add(connector);
                }
                server.setConnectors(connectors.toArray(new Connector[0]));
                return server;
            });
        } ).start();
        App.app = app;

        app.ws("/websockets", ws -> {
            ws.onConnect(ctx -> {
                if (serviceState == ServiceState.ONLINE) {
                    Console.printout("Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress()
                            , MessageType.INFO);
                    App.getInstance().sessionHashMap.put(ctx.getSessionId(), ctx.session);
                    App.getInstance().sessions1.add(ctx.getSessionId());
                    ctx.send("Client connects..");
                    sessionctx.put(ctx.getSessionId(), ctx);
                }
            });
            ws.onClose(ctx -> {
                Console.printout("Client disconnected (Session-ID: " + ctx.getSessionId() + ")"
                        , MessageType.INFO);
                App.getInstance().sessionHashMap.remove(ctx.getSessionId());
                App.getInstance().sessions1.remove(ctx.getSessionId());
                sessionctx.remove(ctx.getSessionId());
            });
            ws.onError(ctx -> {
                Console.printout("Websocket Error", MessageType.ERROR);
                ctx.send("ERROR");
            });
        });

        app.get("/testpage", ctx -> {
            ctx.render("/public/index.html");
        });

        Console.printout("Connecting to BytePhil.de ...", MessageType.INFO);

        try {
            updateConnection.connect("https://bytephil.de/lib/UploadServer/rest.json");

            if (updateConnection.isMaintenance()) {
                Console.printout("Database is currently in maintenance!", MessageType.ERROR);
                return;
            }
            Console.printout("Successfully connected to Update Server.", MessageType.INFO);
        } catch (Exception ex) {
            Console.printout("Server connection failed!", MessageType.ERROR);
        }

        {
            if (!updateConnection.isLatest()) {
                if (!updateConnection.latestIsBeta()) {
                    downloader.download();
                } else {
                    Console.printout("You aren't up to date. Please download the latest version.", MessageType.WARNING);
                }
            } else {
                Console.printout("Your running on the latest Version! (" + version + ")", MessageType.INFO);
                serviceState = ServiceState.ONLINE;
            }
        }
        Console.printout("Prerendering all Pictures...", MessageType.INFO);
        loadPics();

        if (!thread.isAlive()) {
           thread.start();
        }
        Console.empty();

        Console.printout("All Services started! Waiting for Client connection on YourIP:" + app.port(), MessageType.INFO);
        Console.empty();

        Console.input();
    }

    public Thread thread = new Thread() {
        @Override
        public void run() {
            int u = 1;
            while (thread.isAlive()) {
                if (u==11) u=1; else u++;

                for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                    String sessionid = App.getInstance().sessions1.get(i);
                    WsConnectContext session = App.getInstance().sessionctx.get(sessionid);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    ByteBuffer buf = ByteBuffer.wrap(cachedImages.get(u));
                    session.send(buf);
                    if (showProcesses) {
                        Console.printout("Sending Picture " + u + " to " + App.getInstance().sessionHashMap.size() + " Clients", MessageType.INFO);
                    }
                    //session.send("Aktuelle Zeit: " + ZonedDateTime.now(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                try {
                    thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void loadPics() throws IOException {
        BufferedImage originalImage = null;
        for (int i = 1; i < 12; i++) {
            try { originalImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("public/assets/img/stopmotion/" + i + "JPG.jpg"));
            } catch (IOException e) { e.printStackTrace(); }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "jpg", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                cachedImages.put(i, imageInByte);
            } catch(Exception e1) {
                Console.printout("ERROR BufferedImage: " + e1.getMessage(), MessageType.ERROR);
            }
        }
    }


        public static void shutdown() {
            App.getInstance().serviceState = ServiceState.STOPPING;
            Console.printout("System is stopping!", MessageType.ERROR);
            app.stop();
            System.exit(0);
        }
        private SslContextFactory getSslContextFactory() {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(config.keystorePath);
            sslContextFactory.setKeyStorePassword(config.keystorePW);
            return sslContextFactory;
        }
    }