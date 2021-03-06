package de.bytephil.utils;

import de.bytephil.app.App;
import de.bytephil.enums.MessageType;
import io.javalin.websocket.WsConnectContext;
import org.eclipse.jetty.websocket.api.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class Console {

    public static void printout(String message, MessageType type) {
        System.out.println("[" + getTime() + "] " + type + " - " + message);
    }
    public static void empty() {
        System.out.println("");
    }
    private static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void input() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input;
        try {
            input = reader.readLine();

            if (input.contains("load")) {
                if (input.equalsIgnoreCase("load")) {
                    printout("Use \"load [Name to mp4 File] \"", MessageType.ERROR);
                } else {
                    if (!input.contains(".mp4")) {
                        printout("The File has to be a \".mp4\" File!", MessageType.ERROR);
                    } else {
                        try {
                            String fileName = input.replace(" ", "").replace("load", "");
                            printout("Trying to load File \"" + fileName + "\"...", MessageType.INFO);
                            ByteBuffer buf = ByteBuffer.wrap(Converter.convert("Files/" + fileName, fileName, false));
                            int clients = App.getInstance().sessionHashMap.size();
                            Console.printout("Sending loaded Video to all " + clients + " connected Clients!", MessageType.INFO);
                            App.getInstance().currentPlaying = fileName;

                            for (int i = 0; i < clients; i++) {
                                String sessionid = App.getInstance().sessions.get(i);
                                WsConnectContext session = App.getInstance().sessionctx.get(sessionid);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                                session.send(buf);
                                //session.send("Aktuelle Zeit: " + ZonedDateTime.now(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                            }
                            for (int i = 0; i < App.getInstance().infoctx.size(); i++) {
                                App.getInstance().infoctx.get(i).send("Currently theres playing \"" + fileName.replace(".mp4", "") + "\"");
                            }
                        } catch (Exception e1) {
                            printout(e1.getMessage(), MessageType.ERROR);
                            Console.input();
                        }
                    }
                }
            }
            switch (Objects.requireNonNull(input)) {
                case "exit":
                case "stop": {
                    App.shutdown();
                }
                case "help": {
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                    System.out.println(" Stop Program ?? exit");
                    System.out.println(" Show help ?? help");
                    System.out.println(" Show all connected Clients ?? list");
                    System.out.println(" Show all sendet WebSockets ?? show");
                    Console.input();
                }
                case "list": {
                    Console.printout("All Connected Clients", MessageType.INFO);
                    for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                        String sessionid = App.getInstance().sessions.get(i);
                        Session session = App.getInstance().sessionHashMap.get(sessionid);
                        Console.printout(sessionid + " | IP: " + session.getRemoteAddress(), MessageType.INFO);
                    }
                    Console.input();
                }
                case "show": {
                    if (App.getInstance().showProcesses) {
                        Console.printout("The WebSocket processes are no longer shown!", MessageType.INFO);
                        App.getInstance().showProcesses = false;
                    } else {
                        Console.printout("The WebSocket processes are shown now!", MessageType.INFO);
                        App.getInstance().showProcesses = true;
                    }
                    Console.input();
                }
                Console.printout("Command not found! Use \"help\" for Help!", MessageType.ERROR);
                Console.input();
            }
            Console.input();
        } catch (Exception e1) {
            Console.printout("Reader Error: " + e1.getMessage(), MessageType.ERROR);
        }
    }
}
