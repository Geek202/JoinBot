package me.geek.tom.joinbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public final String verificationChannel;
    public final String verifiedRole;
    public final String joinLeaveChannel;
    public final String joinCommand;
    public final String joinMessage;
    public final String leaveMessage;

    private Config(String verificationChannel, String verifiedRole, String joinLeaveChannel, String joinCommand, String joinMessage, String leaveMessage) {
        this.verificationChannel = verificationChannel;
        this.verifiedRole = verifiedRole;
        this.joinLeaveChannel = joinLeaveChannel;
        this.joinCommand = joinCommand;
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
    }

    private boolean valid() {
        return verificationChannel != null
                && verifiedRole != null
                && joinLeaveChannel != null
                && joinCommand != null
                && joinMessage != null
                && leaveMessage != null;
    }
    
    public static Config readConfig(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);
        Config config = new Config(prop.getProperty("verificationChannel"), prop.getProperty("verifiedRole"),
                prop.getProperty("joinLeaveChannel"), prop.getProperty("joinCommand"),
                prop.getProperty("joinMessage"), prop.getProperty("leaveMessage"));
        return config.valid() ? config : null;
    }
}
