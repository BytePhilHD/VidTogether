package de.bytephil.app;

import de.bytephil.enums.VideoState;
import de.bytephil.utils.*;
import de.bytephil.utils.Console;
import de.bytephil.enums.MessageType;
import de.bytephil.enums.ServiceState;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;
    private static Javalin app;
    private ServerConfiguration config;

    public static App getInstance() {
        return instance;
    }

    public String version = "0.0.5";

    public HashMap<String, Session> sessionHashMap = new HashMap<>();
    public HashMap<String, WsConnectContext> sessionctx = new HashMap<>();
    public ArrayList<WsConnectContext> wsCMDctx = new ArrayList<>();
    public ArrayList<WsConnectContext> infoctx = new ArrayList<>();
    public HashMap<String, byte[]> currentVideoBytes = new HashMap<>();
    public List<String> sessions = new ArrayList<>();
    public String currentPlaying = null;

    public boolean showProcesses = false;

    public ServiceState serviceState = ServiceState.STARTING;
    public VideoState videoState = VideoState.LOADING;

    public App() throws IOException {
        instance = this;

        this.updateConnection = new UpdateConnection();
        this.downloader = new UpdateConnection.Downloader(updateConnection);
    }


    public void start() throws IOException {

        boolean firstStart = false;

        if (!new File("server.config").exists()) {
            de.bytephil.utils.Console.printout("The config file is missing! Creating default one.", MessageType.WARNING);
            final File newFile = new File("server.config");
            copyFile(newFile, "default.config");
        }
        if (!new File("Files/Help.yml").exists()) {
            File dir = new File("Files");
            if (!dir.exists()) dir.mkdirs();
            final File newFile = new File("Files/Help.yml");
            final File newFile2 = new File("Files/Example.mp4");
            final File newFile3 = new File("Files/Demo.mp4");
            copyFile(newFile, "Help.yml");
            copyFile(newFile2, "Example.mp4");
            copyFile(newFile3, "Demo.mp4");
            firstStart = true;
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
        }).start();
        App.app = app;

        app.error(404, ctx -> {
            ctx.render("/public/errors/404.html");
        });

        app.ws("/wsinfo", ws -> {
            ws.onConnect(ctx -> {
                if (currentPlaying == null) {
                    ctx.send("Welcome, currently theres nothing playing!");
                } else {
                    ctx.send("Welcome, currently theres playing \"" + currentPlaying.replace(".mp4", "") + "\"");
                }
            });
        });

        app.ws("/wscmd", ws -> {
            ws.onConnect(ctx -> {
                if (videoState == VideoState.PLAYING) {
                    ctx.send("play");
                } else if (videoState == VideoState.PAUSED) {
                    ctx.send("pause");
                }
                wsCMDctx.add(ctx);
            });
            ws.onClose(ctx -> {
                wsCMDctx.remove(ctx);
            });
            ws.onMessage(ctx -> {
                int max = wsCMDctx.size();
                String message = ctx.message();
                if (message.equalsIgnoreCase("play")) {
                    videoState = VideoState.PLAYING;
                    if (message.equalsIgnoreCase("play")) {
                        for (int i = 0; i < max; i++) {
                            wsCMDctx.get(i).send("play");
                        }
                    }
                } else if (message.equalsIgnoreCase("pause")) {
                    videoState = VideoState.PAUSED;
                    if (message.equalsIgnoreCase("pause")) {
                        for (int i = 0; i < max; i++) {
                            wsCMDctx.get(i).send("play");
                        }
                    }
                }
            });
        });

        app.ws("/websockets", ws -> {
            ws.onMessage(ctx -> {
                String message = ctx.message();

                Console.printout("WebSocket: " + message + " | Session-ID: " + ctx.getSessionId(), MessageType.INFO);

                int max = wsCMDctx.size();
                if (message.equalsIgnoreCase("play")) {
                    videoState = VideoState.PLAYING;
                    for (int i = 0; i < max; i++) {
                        WsConnectContext wsConnectContext = wsCMDctx.get(i);
                        if (wsConnectContext.getSessionId() != ctx.getSessionId()) {
                            wsConnectContext.send("play");
                        }
                    }
                } else if (message.equalsIgnoreCase("pause")) {
                    videoState = VideoState.PAUSED;
                    for (int i = 0; i < max; i++) {
                        WsConnectContext wsConnectContext = wsCMDctx.get(i);
                        if (wsConnectContext.getSessionId() != ctx.getSessionId()) {
                            wsConnectContext.send("pause");
                        }
                    }
                }
            });
            ws.onConnect(ctx -> {
                if (serviceState == ServiceState.ONLINE) {
                    Console.printout("Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress()
                            , MessageType.INFO);
                    App.getInstance().sessionHashMap.put(ctx.getSessionId(), ctx.session);
                    App.getInstance().sessions.add(ctx.getSessionId());
                    ctx.send("Client connects..");
                    sessionctx.put(ctx.getSessionId(), ctx);
                    if (currentPlaying != null) {
                        ByteBuffer buf = ByteBuffer.wrap(currentVideoBytes.get(currentPlaying));
                        ctx.send(buf);
                    } else {
                        ByteBuffer buf = null;
                        try {
                            buf = ByteBuffer.wrap(Converter.convert("Files/Demo.mp4", "Demo.mp4", true));
                        } catch (Exception e1) {
                        }
                        if (buf != null)
                            ctx.send(buf);
                        else {
                            ctx.send("No Video is playing!");
                        }
                    }
                }
            });
            ws.onClose(ctx -> {
                Console.printout("Client disconnected (Session-ID: " + ctx.getSessionId() + ")"
                        , MessageType.INFO);
                App.getInstance().sessionHashMap.remove(ctx.getSessionId());
                App.getInstance().sessions.remove(ctx.getSessionId());
                sessionctx.remove(ctx.getSessionId());
            });
            ws.onError(ctx -> {
                Console.printout("Websocket Error (Session-ID: " + ctx.getSessionId() + ")", MessageType.ERROR);
                ctx.send("ERROR");
            });
        });

        app.get("/testpage", ctx -> {
            ctx.render("/public/alt.html");
        });

        if (config.autoUpdate) {
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
        } else {
            serviceState = serviceState.ONLINE;
        }

        Thread thread = UpdateThread.thread;
        if (!thread.isAlive()) {
            //thread.start();
        }
        Console.printout("Maximal usable Memory: " + Runtime.getRuntime().maxMemory() / 1000000000 + " GB", MessageType.INFO);
        Console.printout("Total CPU's: " + Runtime.getRuntime().availableProcessors(), MessageType.INFO);
        Console.empty();

        Console.printout("All Services started! Waiting for Client connection on YourIP:" + app.port(), MessageType.INFO);
        Console.empty();
        if (firstStart) {
            Console.printout("It seems like you're running VidTogether for the first time!", MessageType.WARNING);
            Console.printout("For Help look into your Files Folder or on our GitHub Page!", MessageType.WARNING);
            Console.empty();
            Console.empty();
            Console.empty();
        }
        Console.input();
    }

    public void copyFile(File newFile, String existingFile) throws IOException {
        newFile.createNewFile();
        final FileOutputStream configOutputStream = new FileOutputStream(newFile);
        byte[] buffer = new byte[4096];
        final InputStream defaultConfStream = getClass().getClassLoader().getResourceAsStream(existingFile);
        int readBytes;
        while ((readBytes = defaultConfStream.read(buffer)) > 0) {
            configOutputStream.write(buffer, 0, readBytes);
        }
        defaultConfStream.close();
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