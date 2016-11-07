/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            // Exec gui
            LanternaTerminalGUI tgui = new LanternaTerminalGUI();
            tgui.executeGUI();

            /* TODO IMPLEMENTAR DIRECTORY POLLER */
//        DirectoryPoller dp;
//        dp = new DirectoryPoller(config.getMediaDirectory());
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
