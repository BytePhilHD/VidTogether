package app;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;
import utils.ConfigService;
import utils.UpdateConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

public class App {

    private static App instance;

    private final UpdateConnection updateConnection;
    private final UpdateConnection.Downloader downloader;

    public static App getInstance() {
        return instance;
    }
    public String version = "0.0.1";

    public App() {
        instance = this;

        this.updateConnection = new UpdateConnection();
        this.downloader = new UpdateConnection.Downloader(updateConnection);
    }

    public void start() throws IOException {
        Javalin app = Javalin.create().start(70);

        app.get("/", App::renderHelloPage);

        app.ws("ws://localhost:70/websocket", ws -> {
            ws.onConnect(ctx -> System.out.println("Connected"));
        });

        app.post("/upload-example", ctx -> {
            ctx.uploadedFiles("files").forEach(file -> {
                FileUtil.streamToFile(file.getContent(), "upload/" + file.getFilename());
            });
            ctx.html("Deine Datei wurde erfolgreich hochgeladen!");
        });

        System.out.println("Connecting to BytePhil.de ...");

        try {
            updateConnection.connect("https://bytephil.de/lib/UploadServer/rest.json");

            if (updateConnection.isMaintenance()) {
                System.out.println("Database is currently in maintenance!");
                return;
            }
        } catch (Exception ex) {
            System.out.println("Server connection failed!");
        }

        System.out.println("Successfully connected.");


    {
        if (!updateConnection.isLatest()) {
            if (!updateConnection.latestIsBeta()) {
                downloader.download();
            } else {
                System.out.println("You aren't up to date. Please download the latest version.");
            }
        }
    }
        input(app);
    }

    private static void input(Javalin app) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input = reader.readLine();

        switch (input) {
            case "exit":
            case "stop": {
                System.out.println("PROGRAM WILL EXIT");
                System.exit(1);
            }
            case "help": {
                System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                System.out.println(" Stop Program » exit");
                System.out.println(" Show help » help");
            } case "stopweb": {
                app.stop();
                System.out.println("WebServer wurde gestoppt!");
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