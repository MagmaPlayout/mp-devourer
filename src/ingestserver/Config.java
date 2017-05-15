package ingestserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Reads the configuration file. Creates config file if needed.
 *
 * @author cyberpunx
 */
public class Config {

    private String homeDir = System.getProperty("user.home");
    private String dir = homeDir + "/.magma-playout";
    private String propFileName = homeDir + "/.magma-playout/ingestserver.properties";
    private String thumbnailDir;
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
        boolean success = false;
        try {
            File directory = new File(dir);
            if (directory.exists()) {
                System.out.println("Magma playout directory already exists ...");
            } else {
                System.out.println("Directory not exists, creating now");
                success = directory.mkdir();
                if (success) {
                    System.out.printf("Successfully created new directory : %s%n", dir);
                } else {
                    System.out.printf("Failed to create new directory: %s%n", dir);
                }
            }

            File f = new File(propFileName);
            if (f.exists()) {
                System.out.println("Config file already exists");

            } else {
                System.out.println("No such file exists, creating now");
                success = f.createNewFile();
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(propFileName))) {
                    writer.write("redis_host=localhost\n");
                    writer.write("redis_port=6379\n");
                    writer.write("media_directory=" + homeDir + "/Videos\n");
                    writer.write("ffmpeg_path=\n");
                    writer.write("ffprobe_path=\n");
                    writer.write("thumbnail_dir= INSERT HERE YOUR /.../mp-installer/magma-playout/gui/mp-ui-playout/src/assets/img HERE\n");
                }
                if (success) {
                    System.out.printf("Successfully created new file: %s%n", f);
                } else {
                    System.out.printf("Failed to create new file: %s%n", f);
                }
            }

            Properties prop = new Properties();
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);

            // get the property value and save it
            this.redisHost = prop.getProperty("redis_host");
            this.redisPort = prop.getProperty("redis_port");
            this.mediaDirectory = prop.getProperty("media_directory");
            this.ffmpeg = prop.getProperty("ffmpeg_path");
            this.ffprobe = prop.getProperty("ffprobe_path");
            this.thumbnailDir = prop.getProperty("thumbnail_dir");
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

    public String getThumbnailDir() {
        return thumbnailDir;
    }

}
