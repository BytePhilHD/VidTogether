package de.bytephil.utils;

import de.bytephil.app.App;
import de.bytephil.enums.MessageType;
import io.javalin.websocket.WsConnectContext;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class UpdateThread {

    public static float time = 0;

    public static Thread thread = new Thread() {
        @Override
        public void run() {
            int u = 1;
            while (thread.isAlive()) {
                time = 0;
                ArrayList<WsConnectContext> wsCMD = App.getInstance().wsCMDctx;

                for (int i = 0; i < wsCMD.size(); i++) {
                    wsCMD.get(i).send("thread");   // Sending Request for information about current Video Time
                }
                try {
                    thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
