package app;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;

import java.util.Collections;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(70);

        app.get("/", App::renderHelloPage);

        app.post("/upload-example", ctx -> {
            ctx.uploadedFiles("files").forEach(file -> {
                FileUtil.streamToFile(file.getContent(), "upload/" + file.getFilename());
            });
            ctx.html("Deine Datei wurde erfolgreich hochgeladen!");
        });
    }

    private static void renderHelloPage(Context ctx) {
            HelloPage page = new HelloPage();
            page.userName = "admin";
            page.userpassword = "admin234";
            ctx.render("startpage.jte", Collections.singletonMap("page", page));
    }
}