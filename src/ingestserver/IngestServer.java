/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import java.io.File;
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
            // Creates config file if not exists
            Config config = null;
            try {
                config = new Config();
            } catch (IOException ex) {
                Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Creates input/output folders
            String inputPath = config.getMediaDirectory() + "/input";
            String outputPath = config.getMediaDirectory() + "/output";
            File inputFolder = new File(inputPath);
            File outputFolder = new File(outputPath);
            boolean success = false;
            if (inputFolder.exists()) {
                System.out.println("Input directory already exists ...");
            } else {
                System.out.println("Input directory not exists, creating now");
                success = inputFolder.mkdir();
                if (success) {
                    System.out.printf("Successfully created new directory : %s%n", inputPath);
                } else {
                    System.out.printf("Failed to create new directory: %s%n", inputPath);
                }
            }
            success = false;
            if (outputFolder.exists()) {
                System.out.println("Output directory already exists ...");
            } else {
                System.out.println("Output directory not exists, creating now");
                success = outputFolder.mkdir();
                if (success) {
                    System.out.printf("Successfully created new directory : %s%n", outputPath);
                } else {
                    System.out.printf("Failed to create new directory: %s%n", outputPath);
                }
            }

            // Exec gui
            LanternaTerminalGUI tgui = new LanternaTerminalGUI();
            tgui.executeGUI();

        } catch (IOException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
