package app;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

public class App {
    public static void main(String[] args) throws IOException {
        Javalin app = Javalin.create().start(70);

        app.get("/", App::renderHelloPage);

        app.post("/upload-example", ctx -> {
            ctx.uploadedFiles("files").forEach(file -> {
                FileUtil.streamToFile(file.getContent(), "upload/" + file.getFilename());
            });
            ctx.html("Deine Datei wurde erfolgreich hochgeladen!");
        });
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