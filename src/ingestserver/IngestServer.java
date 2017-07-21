package ingestserver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: replace al System.out with logger.log
/**
 *
 * @author ragnarok
 */
public class IngestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IngestServer is = new IngestServer();

        if(args.length < 1){
            System.out.println("Not enough arguments!");
            System.out.println("args: <media dir> [transcode]");
            System.out.println("transcode is an optional parameter. If present transcode is on, if missing no transcoding is applied");
            System.exit(0);
        }
        String mediaDir = args[0];
        boolean transcode = (args.length >1);   // Second argument can be whatever but if exists it means that transcoding needs to happen

        is.run(mediaDir, transcode);
    }

    private void run(String mediaDir, boolean transcode){
        DirectoryCrawler dc = new DirectoryCrawler(mediaDir);
        
        // Creates config file if not exists
        Config config = null;
        try {
            config = new Config();
        } catch (IOException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        // Creates input/output folders
        String inputPath = mediaDir + "/input";
        String outputPath = mediaDir + "/output";
        File inputFolder = new File(inputPath);
        File outputFolder = new File(outputPath);
        boolean success;
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

        if(transcode){
            dc.transcodeInputDirectory();
        } else {
            dc.analyze();
        }

        System.out.println("DONE!");
    }
}
