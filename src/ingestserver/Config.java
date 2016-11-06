package ingestserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Reads the configuration file
 *
 * @author cyberpunx
 */
public class Config {

    private String homeDir = System.getProperty("user.home");
    private String propFileName = homeDir + "/.magma-playout/ingestserver.properties";
    private InputStream inputStream;
    private String redisHost;
    private String redisPort;
    private String mediaDirectory;
    private String ffmpeg;
    private String ffprobe;

    /**
     *
     * @throws IOException
     */
    public Config() throws IOException {

        try {
            Properties prop = new Properties();
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);

            //inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            Date time = new Date(System.currentTimeMillis());

            // get the property value and save it
            this.redisHost = prop.getProperty("redis_host");
            this.redisPort = prop.getProperty("redis_port");
            this.mediaDirectory = prop.getProperty("media_directory");
            this.ffmpeg = prop.getProperty("ffmpeg_path");
            this.ffprobe = prop.getProperty("ffprobe_path");
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
    }

    public void setProperty(String key, String value) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(propFileName);
        FileInputStream in = new FileInputStream(propFileName);
        Properties props = new Properties();

        props.load(in);
        in.close();

        if (key.equalsIgnoreCase("redis_host")) {
            this.redisHost = value;
        }
        if (key.equalsIgnoreCase("redis_port")) {
            this.redisPort = value;
        }
        if (key.equalsIgnoreCase("media_directory")) {
            this.mediaDirectory = value;
        }
        if (key.equalsIgnoreCase("ffmpeg_path")) {
            this.ffmpeg = value;
        }
        if (key.equalsIgnoreCase("ffprobe_path")) {
            this.ffprobe = value;
        }

        props.setProperty("redis_host", this.redisHost);
        props.setProperty("redis_port", this.redisPort);
        props.setProperty("media_directory", this.mediaDirectory);
        props.setProperty("ffmpeg_path", this.ffmpeg);
        props.setProperty("ffprobe_path", this.ffprobe);

        props.store(out, null);
        out.close();
    }

    public String getFfprobe() {
        return ffprobe;
    }

    public String getFfmpeg() {
        return ffmpeg;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public String getMediaDirectory() {
        return mediaDirectory;
    }

}
