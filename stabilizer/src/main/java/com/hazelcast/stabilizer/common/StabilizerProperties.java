package com.hazelcast.stabilizer.common;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.stabilizer.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.hazelcast.stabilizer.Utils.fileAsText;
import static com.hazelcast.stabilizer.Utils.getStablizerHome;
import static com.hazelcast.stabilizer.Utils.newFile;
import static java.lang.String.format;

/**
 * StabilizerProperties will always load the properties in the stabilizer_home/conf/stabilizer.properties
 * as defaults. If a stabilizer.properties is available in the working dir of if an explicit stabilizer.properties
 * is configured, it will override the properties from the default.
 */
public class StabilizerProperties {
    private final static ILogger log = Logger.getLogger(StabilizerProperties.class);

    private final Properties properties = new Properties();

    public StabilizerProperties() {
        File defaultPropsFile = newFile(getStablizerHome(), "conf", "stabilizer.properties");
        log.finest("Loading default stabilizer.properties from: " + defaultPropsFile.getAbsolutePath());
        load(defaultPropsFile);
    }

    /**
     * Initialized the StabilizerProperties
     *
     * @param file the file to load the properties from. If the file is null, then first the stabilizer.properties
     *             in the working dir is checked.
     */
    public void init(File file) {
        if (file == null) {
            //if no file is explicitly given, we look in the working directory
            File fallbackPropsFile = new File("stabilizer.properties");
            if (fallbackPropsFile.exists()) {
                file = fallbackPropsFile;
            } else {
                log.warning(format("%s is not found, relying on defaults", fallbackPropsFile));
            }
        }

        if (file != null) {
            log.info(format("Loading stabilizer.properties: %s", file.getAbsolutePath()));
            load(file);
        }
    }

    private void load(File file) {
        if (!file.exists()) {
            Utils.exitWithError(log, "Could not find stabilizer.properties file:  " + file.getAbsolutePath());
            return;
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                Utils.closeQuietly(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String name) {
        String value = (String) properties.get(name);

        if ("CLOUD_IDENTITY".equals(name)) {
            value = load("CLOUD_IDENTITY",value);
        }else if("CLOUD_CREDENTIAL".equals(name)){
            value = load("CLOUD_CREDENTIAL",value);
        }

        return value;
    }

    public String get(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        value = (String) properties.get(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private String load(String property, String value) {
        File file = newFile(value);
        if (!file.exists()) {
            Utils.exitWithError(log, format("Can't find %s file %s", property, value));
        }

        if (log.isFinestEnabled()) {
            log.finest("Loading " + property + " from file: " + file.getAbsolutePath());
        }
        return fileAsText(file).trim();
    }
}
