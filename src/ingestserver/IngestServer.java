package ingestserver;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import libconfig.ConfigurationManager;

//TODO: replace al System.out with logger.log
/**
 *
 * @author ragnarok
 */
public class IngestServer {

    public static void main(String[] args) {
        IngestServer is = new IngestServer();
        is.run();
    }

    private void run(){
        // Config
        Logger logger = Logger.getLogger(IngestServer.class.getName());
        ConfigurationManager cfg = ConfigurationManager.getInstance();
        cfg.init(logger);
        cfg.printConfig(logger);
        
        // CHECKS CONFIGURATION FILE
        
        // checks FFMPEG
        File ffmpegPath = new File(cfg.getMltFrameworkPath()+"/ffmpeg");
        if(ffmpegPath.exists() && !ffmpegPath.isDirectory()) { 
            logger.log(Level.INFO, "FFMPEG: Config OK");
        }else{
            logger.log(Level.INFO, "FFMPEG: Config ERROR");
        }
        // checks FFPROBE
        File ffprobePath = new File(cfg.getMltFrameworkPath()+"/ffprobe");
        if(ffprobePath.exists() && !ffprobePath.isDirectory()) { 
            logger.log(Level.INFO, "FFPROBE: Config OK");
        }else{
            logger.log(Level.INFO, "FFPROBE: Config ERROR");
        }
        
        // checks INPUT DIR
        File inputPath = new File(cfg.getDevourerInputDir());
        if(inputPath.exists()) { 
            logger.log(Level.INFO, "INPUT DIR: Config OK");
        }else{
            logger.log(Level.INFO, "INPUT DIR: Config ERROR");
        }
        
        // checks OUTPUT DIR
        File outputPath = new File(cfg.getDevourerOutputDir());
        if(outputPath.exists()) { 
            logger.log(Level.INFO, "OUTPUT DIR: Config OK");
        }else{
            logger.log(Level.INFO, "OUTPUT DIR: Config ERROR");
        }
        
        // checks THUMBS DIR
        File thumbsPath = new File(cfg.getDevourerThumbDir());
        if(thumbsPath.exists()) { 
            logger.log(Level.INFO, "THUMBS DIR: Config OK");
        }else{
            logger.log(Level.INFO, "THUMBS DIR: Config ERROR");
        }
        // END CONFIG FILE CHECK
        
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        // TRANSCODE FIRST
        File[] inputFolders = new File(cfg.getDevourerInputDir()).listFiles();
        DirectoryCrawler dc = new DirectoryCrawler();
        for (File file : inputFolders) {
            if (file.isDirectory()) {
                dc.transcodeDirectory(file.getAbsolutePath());
            } 
        }
        
        // ANALYZE TRANSCODED FILES
        File[] ouputFolders = new File(cfg.getDevourerOutputDir()).listFiles();
        for (File file : ouputFolders) {
            if (file.isDirectory()) {
                dc.analyze(file.getAbsolutePath());
            } 
        }       
        
        logger.log(Level.INFO, "Done!");
    }
}
