package de.bytephil.utils;

import org.eclipse.jetty.websocket.api.Session;

import java.text.SimpleDateFormat;

public class User {

    private Session session;
    private String sessionid;
    private SimpleDateFormat connectTime;

    public User(Session session, String sessionID, SimpleDateFormat connectTime) {
        this.session = session;
        this.connectTime = connectTime;
        this.session = session;
    }
}
