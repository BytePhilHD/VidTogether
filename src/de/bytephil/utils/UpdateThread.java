package de.bytephil.utils;

import de.bytephil.app.App;
import de.bytephil.enums.MessageType;
import io.javalin.websocket.WsConnectContext;

import java.time.format.DateTimeFormatter;

public class UpdateThread {

    public static Thread thread = new Thread() {
        @Override
        public void run() {
            int u = 1;
            while (thread.isAlive()) {
                if (u==11) u=1; else u++;

                for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                    String sessionid = App.getInstance().sessions.get(i);
                    WsConnectContext session = App.getInstance().sessionctx.get(sessionid);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    //ByteBuffer buf = ByteBuffer.wrap(cachedImages.get(u));
                   // session.send(buf);
                    if (App.getInstance().showProcesses) {
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
}
