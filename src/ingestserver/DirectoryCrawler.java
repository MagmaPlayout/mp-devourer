/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.transform.Affine;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Given a directory. Walks it, analyzing each file and processing it if it's a
 * valid file format using a FileProcessor.
 *
 * DirectoryCrawler runs-and-dies, if you want to constantly analyze a
 * directory, use DirectoryPoller class.
 *
 * @author cyberpunx
 */
public class DirectoryCrawler {

    private String DirectoryAbsolutePath;
    private FileProcessor fp;

    /**
     * Sets the directory to be processed
     *
     * @param absolutePath Aboslute path to the target directory
     */
    public DirectoryCrawler(String absolutePath) {
        this.DirectoryAbsolutePath = absolutePath;
        fp = new FileProcessor();
    }

    /**
     * Walks the directory tree and analyzes each file. Processing all the video
     * files found.
     */
    public void analyze() {
        // Setup Redis and deletes all previous keys. We start from 0 each time.
        RedisManager redis = new RedisManager();
        redis.resetRedisKeys();

        String dirPath = this.DirectoryAbsolutePath;
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if (files.length == 0) {
            System.out.println("The directory is empty");
        } else {
            System.out.println("ANALIZING: " + this.DirectoryAbsolutePath + "\n\n");
            for (File aFile : files) {
                if (fp.isCorrectFileType(aFile.toPath())) {
                    //System.out.println(aFile.getName() + " - " + aFile.length());
                    try {
                        //process files
                        processFiles(aFile);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private void processFiles(File file) throws IOException {

        System.out.println("Processing file: " + file.getAbsolutePath());
        System.out.println("--------------------\n");

        String[] cmdOut;
        String duration = null;
        String framerate;
        String frames;
        List<String> thumbArray = new ArrayList<>();

        //duration in seconds
        cmdOut = fp.execFfprobe("-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            System.out.println("ERROR getting Duration: ");
            System.out.println(cmdOut[1]);
        }
        String duration2 = cmdOut[0];
        duration = fp.secondsToDuration(cmdOut[0]);
        System.out.println("Duration: " + duration);

        //framerate
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=avg_frame_rate -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            System.out.println("ERROR getting Framerate: ");
            System.out.println(cmdOut[1]);
        }
        framerate = fp.getFPS(cmdOut[0]);
        System.out.println("Framerate: " + framerate);

        //frames
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=nb_frames -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            System.out.println("ERROR getting Frames: ");
            System.out.println(cmdOut[1]);
        }
        frames = cmdOut[0];
        System.out.println("Frames: " + frames);

        //Thumbnails
        thumbArray = fp.generateThumbnailGIF(file, duration2, "2");
        thumbArray.forEach(System.out::println);
        ;
        System.out.println("\nRedis Insert:\n");
        //Creates Clip Object
        Clip clip = new Clip();
        clip.setName(file.getName());
        clip.setPath(file.getAbsolutePath());
        clip.setThumbnails(thumbArray);
        clip.setDuration(duration);
        clip.setFps(framerate);
        clip.setFrames(frames);
        //System.out.println("clip = " + clip.toString());

        // push clips in redis
        //Jedis redis = new jedis();
        //redis.rpush("cliplist", json); // old list cliplist
        RedisManager redis = new RedisManager();
        redis.addClip(clip);
        System.out.println("--------------------\n");

    }

}
