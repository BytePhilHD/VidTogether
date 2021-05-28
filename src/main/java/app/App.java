package app;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.websocket.api.Session;
import utils.ConfigService;
import utils.Console;
import utils.MessageType;
import utils.UpdateConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;

    public static App getInstance() {
        return instance;
    }
    public String version = "0.0.2";

    public HashMap<String, Session> sessionHashMap = new HashMap<>();

    public App() {
        instance = this;

        this.updateConnection = new UpdateConnection();
        this.downloader = new UpdateConnection.Downloader(updateConnection);
    }

    public void start() throws IOException {
        Javalin app = Javalin.create().start(70);

        app.get("/", App::renderHelloPage);

        app.ws("/websockets", ws -> {
            ws.onConnect(ctx -> Console.printout("Client connected with Session-ID: " + ctx.getSessionId() + " IP: "
                    + ctx.session.getLocalAddress()
                    , MessageType.INFO ));
            ws.onClose(ctx -> Console.printout("Client disconnected (Session-ID: " + ctx.getSessionId()
                    , MessageType.INFO));
        });

        app.post("/upload-example", ctx -> {
            ctx.uploadedFiles("files").forEach(file -> {
                FileUtil.streamToFile(file.getContent(), "upload/" + file.getFilename());
            });
            ctx.html("Deine Datei wurde erfolgreich hochgeladen!");
        });



        Console.printout("Connecting to BytePhil.de ...", MessageType.INFO);

        try {
            updateConnection.connect("https://bytephil.de/lib/UploadServer/rest.json");

            if (updateConnection.isMaintenance()) {
                Console.printout("Database is currently in maintenance!", MessageType.ERROR);
                return;
            }
        } catch (Exception ex) {
            Console.printout("Server connection failed!", MessageType.ERROR);
        }

        Console.printout("Successfully connected.", MessageType.INFO);


    {
        if (!updateConnection.isLatest()) {
            if (!updateConnection.latestIsBeta()) {
                downloader.download();
            } else {
                Console.printout("You aren't up to date. Please download the latest version.", MessageType.WARNING);
            }
        } else {
            Console.printout("Your running on the latest Version! (" + version + ")", MessageType.INFO);
        }
    }
        System.out.println("");
        input(app);
    }

    private static void input(Javalin app) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input = reader.readLine();

        switch (input) {
            case "exit":
            case "stop": {
                Console.printout("PROGRAM WILL EXIT", MessageType.WARNING);
                System.exit(1);
            }
            case "help": {
                System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                System.out.println(" Stop Program » exit");
                System.out.println(" Show help » help");
                App.input(app);
            } case "stopweb": {
                app.stop();
                System.out.println("The Webserver will be stopped!");
            }
        }
    }

    private static void renderHelloPage(Context ctx) {
            HelloPage page = new HelloPage();
            page.userName = "admin";
            page.userpassword = "admin234";
            ctx.render("startpage.jte", Collections.singletonMap("page", page));
    }
}