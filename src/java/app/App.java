package app;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;
import io.javalin.http.ErrorHandler;
import io.javalin.plugin.rendering.vue.VueComponent;
import io.javalin.websocket.WsConnectContext;
import jline.console.ConsoleReader;
import org.eclipse.jetty.websocket.api.Session;
import utils.Console;
import utils.MessageType;
import utils.ServiceState;
import utils.UpdateConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;
    private static Javalin app;

    public static App getInstance() {
        return instance;
    }

    public String version = "0.0.3";

    public HashMap<String, Session> sessionHashMap = new HashMap<>();
    public HashMap<String, WsConnectContext> sessionctx = new HashMap<>();
    public List<String> sessions1 = new ArrayList<>();

    public ServiceState serviceState = ServiceState.STARTING;

    public App() {
        instance = this;

        this.updateConnection = new UpdateConnection();
        this.downloader = new UpdateConnection.Downloader(updateConnection);
    }

    public void start() throws IOException {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
        }).start(80);
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
        Console.empty();
        if (!thread.isAlive()) {
            thread.start();
        }

        Console.printout("All Services started! Waiting for Client connection on YourIP:" + app.port(), MessageType.INFO);
        Console.empty();

        input();
    }

    public Thread thread = new Thread() {
        @Override
        public void run() {
            while (thread.isAlive()) {
                for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                    String sessionid = App.getInstance().sessions1.get(i);
                    WsConnectContext session = App.getInstance().sessionctx.get(sessionid);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    session.send("Aktuelle Zeit: " + ZonedDateTime.now(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

        private static void input() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String input = null;
            try {
                input = reader.readLine();
                switch (Objects.requireNonNull(input)) {
                    case "exit":
                    case "stop": {
                        shutdown();
                    }
                    case "help": {
                        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                        System.out.println(" Stop Program » exit");
                        System.out.println(" Show help » help");
                        System.out.println(" Show all connected Clients » list");
                        App.input();
                    }
                    case "stopweb": {
                        app.stop();
                        System.out.println("The Webserver will be stopped!");
                    }
                    case "list": {
                        Console.printout("All Connected Clients", MessageType.INFO);
                        for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                            String sessionid = App.getInstance().sessions1.get(i);
                            Session session = App.getInstance().sessionHashMap.get(sessionid);
                            Console.printout(sessionid + " | IP: " + session.getRemoteAddress(), MessageType.INFO);
                        }
                        App.input();
                    }
                    Console.printout("Command not found! Use \"help\" for Help!", MessageType.ERROR);
                    App.input();
                }
                App.input();
            } catch (Exception e1) {
                Console.printout("Reader Error: " + e1.getMessage(), MessageType.ERROR);
            }
        }

        public static void shutdown() {
            App.getInstance().serviceState = ServiceState.STOPPING;
            Console.printout("System is stopping!", MessageType.ERROR);
            app.stop();
            System.exit(0);
        }
    }