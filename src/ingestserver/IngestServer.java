package ingestserver;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import libconfig.ConfigurationManager;

/**
 *
 * @author ragnarok
 */
public class IngestServer {
    private ConfigurationManager cfg;
    private Logger logger;

    public static void main(String[] args) {
        IngestServer is = new IngestServer();
        is.run();
    }

    private void run(){
        // Config
        logger = Logger.getLogger(IngestServer.class.getName());
        this.cfg = ConfigurationManager.getInstance();
        cfg.init(logger);
        cfg.printConfig(logger);
        
        if(!checkConfig(cfg, logger)){
            System.exit(1);
        }
        
        DirectoryCrawler dc = new DirectoryCrawler(logger);
        transcodeInputMedias(dc);// Transcode all input medias first
        processOutputFolder(dc); // Process transcoded files
        cleanInputFolder();      // Moves all the files from input folder to processed folder.
        
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
        if(!inputPath.exists() && !inputPath.isDirectory()) {
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
    
    private void transcodeInputMedias(DirectoryCrawler dc){
        File[] inputFolders = new File(cfg.getDevourerInputDir()).listFiles();
        
        for (File file : inputFolders) { //Transcode Providers
            if (file.isDirectory()) {
                dc.transcodeDirectory(file.getAbsolutePath());
            } 
        }
        dc.transcodeDirectory(cfg.getDevourerInputDir()); //Transcode "Default" supplier (input folder)
    }
    
    /**
     * Operates over the transcodedFiles to create mlt's, thumbnails and add them to the DB.
     * 
     * @param dc
     * @return 
     */
    private void processOutputFolder(DirectoryCrawler dc){
        HashMap<Integer, Clip> processedFiles = new HashMap<>(); // This map will be loaded with all the processedFiles
        
        File[] outputFolders = new File(cfg.getDevourerOutputDir()).listFiles();
        for (File file : outputFolders) {
            if (file.isDirectory()) {
                processedFiles = dc.analyzeDir(file.getAbsolutePath(), processedFiles);
            } 
        }
        
        // Modifies each .mlt file to insert the piece ID as "title" attribute
        updateMltIds(processedFiles);
    }
    
    private void updateMltIds(HashMap<Integer, Clip> processedFiles){
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
    }
    
    /**
     * Moves files from inputFolder to "processed" folder
     */
    private void cleanInputFolder(){
        logger.log(Level.INFO, "Moving processed files to \"processed\" folder...");
        
        File processedFolder = new File(cfg.getDevourerInputDir()+"/../processed");
        File inputDir = new File(cfg.getDevourerInputDir());
        processedFolder.mkdir();
        
        File[] inputFiles = inputDir.listFiles();
        
        for(File file:inputFiles){
            file.renameTo(new File(processedFolder, file.getName()));
        }
        
        logger.log(Level.INFO, "Done moving files.");
    }
}
