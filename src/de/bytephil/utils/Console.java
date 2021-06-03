package de.bytephil.utils;

import de.bytephil.app.App;
import jline.console.ConsoleReader;
import org.bukkit.command.CommandException;
import org.eclipse.jetty.websocket.api.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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

        String input = null;
        try {
            input = reader.readLine();
            switch (Objects.requireNonNull(input)) {
                case "exit":
                case "stop": {
                    App.shutdown();
                }
                case "help": {
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                    System.out.println(" Stop Program » exit");
                    System.out.println(" Show help » help");
                    System.out.println(" Show all connected Clients » list");
                    System.out.println(" Show all sendet WebSockets » show");
                    Console.input();
                }
                case "list": {
                    Console.printout("All Connected Clients", MessageType.INFO);
                    for (int i = 0; i < App.getInstance().sessionHashMap.size(); i++) {
                        String sessionid = App.getInstance().sessions1.get(i);
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
