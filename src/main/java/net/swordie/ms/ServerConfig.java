package net.swordie.ms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    public static final String HEAP_DUMP_DIR = ServerConstants.LOG_DIR + "/heapdump";

    private static final Properties prop = new Properties();

    static {
        var file = new File(ServerConstants.RESOURCES_DIR + "/config.properties");
        if (file.exists()) {
            try (var is = new FileInputStream(file)) {
                prop.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(prop.getProperty(key, String.valueOf(defaultValue)));
    }

    public static long getLong(String key, long defaultValue) {
        return Long.parseLong(prop.getProperty(key, String.valueOf(defaultValue)));
    }

    public static double getDouble(String key, double defaultValue) {
        return Double.parseDouble(prop.getProperty(key, String.valueOf(defaultValue)));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(prop.getProperty(key, String.valueOf(defaultValue)));
    }
}
