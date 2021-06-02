package app;

import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import jline.console.ConsoleReader;
import org.eclipse.jetty.websocket.api.Session;
import utils.Console;
import utils.MessageType;
import utils.ServiceState;
import utils.UpdateConnection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;
    private static Javalin app;

    private ConsoleReader reader = new ConsoleReader();

    public static App getInstance() {
        return instance;
    }

    public String version = "0.0.3";

    public HashMap<String, Session> sessionHashMap = new HashMap<>();
    public HashMap<String, WsConnectContext> sessionctx = new HashMap<>();
    public HashMap<Integer, byte[]> cachedImages = new HashMap<>();
    public List<String> sessions1 = new ArrayList<>();

    public ServiceState serviceState = ServiceState.STARTING;

    public App() throws IOException {
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
        Console.printout("Prerendering all Pictures...", MessageType.INFO);
        loadPics();

        if (!thread.isAlive()) {
           thread.start();
        }
        Console.empty();

        Console.printout("All Services started! Waiting for Client connection on YourIP:" + app.port(), MessageType.INFO);
        Console.empty();

        input();
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
                    //session.send("Aktuelle Zeit: " + ZonedDateTime.now(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                try {
                    thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void loadPics() {
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