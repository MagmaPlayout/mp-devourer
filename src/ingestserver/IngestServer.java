package ingestserver;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
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
        
        if(!checkConfig(cfg, logger)){
            System.exit(1);
        }

//TODO: no procesar archivos ya procesados. El input se puede borrar y traer medias nuevos, pero el output puede tener cosas viejas procesadas previamente
        
        // TRANSCODE FIRST
        File[] inputFolders = new File(cfg.getDevourerInputDir()).listFiles();
        DirectoryCrawler dc = new DirectoryCrawler();
        for (File file : inputFolders) { //Transcode Providers
            if (file.isDirectory()) {
                dc.transcodeDirectory(file.getAbsolutePath());
            } 
        }
        dc.transcodeDirectory(cfg.getDevourerInputDir()); //Transcode "Default" supplier (input folder)
        
        // ANALYZE TRANSCODED FILES
        HashMap<Integer, Clip> processedFiles = new HashMap<>();
        File[] outputFolders = new File(cfg.getDevourerOutputDir()).listFiles();
        for (File file : outputFolders) {
            if (file.isDirectory()) {
                dc.analyze(file.getAbsolutePath(), processedFiles);
            } 
        }

        // Modifies each .mlt file to insert the piece ID as "title" attribute
        try {
            
            MltProcessor mltProc = new MltProcessor();
            mltProc.processMlts(processedFiles);

        } catch (ParserConfigurationException ex) {
            //TODO: HANDLE
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            //TODO: HANDLE
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        logger.log(Level.INFO, "Done!");
    }

    /**
     * Checks configuration file for mandatory entries.
     * 
     * @param cfg
     * @param logger
     * @return
     */
    private boolean checkConfig(ConfigurationManager cfg, Logger logger){
        boolean ok = true;

        // checks FFMPEG
        File ffmpegPath = new File(cfg.getMltFrameworkPath()+"/ffmpeg");
        if(!ffmpegPath.exists() || ffmpegPath.isDirectory()) {
            logger.log(Level.SEVERE, "FFMPEG: Config ERROR");
            ok = false;
        }

        // checks FFPROBE
        File ffprobePath = new File(cfg.getMltFrameworkPath()+"/ffprobe");
        if(!ffprobePath.exists() || ffprobePath.isDirectory()) {
            logger.log(Level.SEVERE, "FFPROBE: Config ERROR");
            ok = false;
        }

        // checks INPUT DIR
        File inputPath = new File(cfg.getDevourerInputDir());
        if(!inputPath.exists()) {
            logger.log(Level.SEVERE, "INPUT DIR: Config ERROR");
            ok = false;
        }

        // checks OUTPUT DIR
        File outputPath = new File(cfg.getDevourerOutputDir());
        if(!outputPath.exists()) {
            logger.log(Level.SEVERE, "OUTPUT DIR: Config ERROR");
            ok = false;
        }

        // checks THUMBS DIR
        File thumbsPath = new File(cfg.getDevourerThumbDir());
        if(!thumbsPath.exists()) {
            logger.log(Level.SEVERE, "THUMBS DIR: Config ERROR");
            ok = false;
        }

        return ok;
    }
}
