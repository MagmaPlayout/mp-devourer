package ingestserver;

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
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DirectoryCrawler dc = new DirectoryCrawler();
        dc.transcodeDirectory(cfg.getDevourerInputDir());
        dc.analyze(cfg.getDevourerOutputDir());
        logger.log(Level.INFO, "Done!");
    }
}
