package app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HelloPage {
    public String userName;
    public String userpassword;
    public String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}