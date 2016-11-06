/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ragnarok
 */
public class IngestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Config config = null;
            try {
                config = new Config();
            } catch (IOException ex) {
                Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Output file logger
            String homeDir = System.getProperty("user.home");
            String outFileName = homeDir + "/.magma-playout/ingestOutput.txt";
            PrintStream out = new PrintStream(new FileOutputStream(outFileName));
            System.setOut(out);

            // Exec gui
            TerminalGUI tgui = new TerminalGUI();
            tgui.executeGUI();

//            DirectoryCrawler dc;
//            dc = new DirectoryCrawler(config.getMediaDirectory());
//            dc.analyze();

            /* TODO IMPLEMENTAR DIRECTORY POLLER */
//        DirectoryPoller dp;
//        dp = new DirectoryPoller("/home/ragnarok/Documents/PROYECTO_FINAL/IngestServer/IngestServer/media");
//        dp.start();
//
//        int i = 10;
//        while (i > 0) {
//            System.out.println("Remaining: " + i + " seconds");
//            try {
//                i--;
//                Thread.sleep(1000L);    // 1000L = 1000ms = 1 second
//            } catch (InterruptedException e) {
//                //I don't think you need to do anything for your particular problem
//            }
//        }
//
//        dp.stopPolling();
//
//        i = 10;
//        while (i > 0) {
//            System.out.println("Remaining: " + i + " seconds");
//            try {
//                i--;
//                Thread.sleep(1000L);    // 1000L = 1000ms = 1 second
//            } catch (InterruptedException e) {
//                //I don't think you need to do anything for your particular problem
//            }
//        }
        } catch (IOException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
