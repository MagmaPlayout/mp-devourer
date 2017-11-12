package ingestserver;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import libconfig.ConfigurationManager;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

/**
 * Given a directory. Walks it, analyzing each file and processing it if it's a
 * valid file format using a FileProcessor.
 *
 * @author cyberpunx
 */
public class DirectoryCrawler {
    private final FileProcessor fp;
    private final String playoutApiBaseUrl;
    private final String adminApiBaseUrl;
    private final Logger logger;
   
    public DirectoryCrawler(Logger logger) {
        this.fp = new FileProcessor(logger);
        this.logger = logger;

        ConfigurationManager cfg = ConfigurationManager.getInstance();
        this.playoutApiBaseUrl = cfg.getPlayoutAPIRestBaseUrl();
        this.adminApiBaseUrl = cfg.getAdminAPIRestBaseUrl();
    }

    /**
     * Walks the directory tree and analyzes each file. Processing all the video
     * files found.
     * @param directory
     * @param processedFiles HashMap that will be populated with a key (piece id) and a value (it's corresponding Clip)
     * @return
     */
    public HashMap<Integer, Clip> analyzeDir(String directory, HashMap<Integer, Clip> processedFiles) {
        File dir = new File(directory);
        File[] files = dir.listFiles();

        if(files == null || files.length == 0){
            logger.log(Level.INFO, "No files to analyze in the specified directory!");
        }
        else {
            logger.log(Level.INFO, "ANALIZING: " + directory + "\n\n");
            for (File aFile : files) {
                if (fp.isSupportedFileType(aFile.toPath())) {
                    //System.out.println(aFile.getName() + " - " + aFile.length());
                    try {
                        if(isItOkToProcess(aFile)){
                            processedFiles = processFile(aFile, processedFiles);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "An error ocurred while using the REST API. Data can't be uploaded to the database. Aborting.");
                        logger.log(Level.SEVERE, e.getMessage());
//                        System.exit(1);
                    }
                }
            }
        }

        return processedFiles;
    }
    
    /**
     * Obtains the provider of the file and checks if it's current (ok to process
     * or if it's an old file (should be ignored)
     * 
     * @param file
     * @return 
     */
    private boolean isItOkToProcess(File file){
        boolean process = false;
        String curProviderPath = file.getParentFile().getName();
        String curName = file.getName();

        // Default provider is when the media is on the root input dir without a provider folder
        if(curProviderPath.equals(FileProcessor.DEFAULT_PROVIDER)){
            File[] inputFiles = new File(ConfigurationManager.getInstance().getDevourerInputDir()).listFiles();
            process = isItOkToProcessThisFileFromProvider(inputFiles, curName);
        }
        // Every other case has a provider folder
        else {
            File[] inputFiles = new File(ConfigurationManager.getInstance().getDevourerInputDir()+"/"+curProviderPath).listFiles();
            process = isItOkToProcessThisFileFromProvider(inputFiles, curName);
        }
        
        return process;
    }
    
    /**
     * Checks if the specified curFileName exists in the input directory.
     * If it does, then it's OK to process, if it doesn't it means that it's 
     * an old file, and returns false to be ignored.
     * 
     * @param providerFiles
     * @param curFileName
     * @return 
     */
    private boolean isItOkToProcessThisFileFromProvider(File[] providerFiles, String curFileName){
        if(providerFiles == null){
            return false;
        }
        
        boolean process = false;
        for(File inFile:providerFiles){
            if(!inFile.isDirectory() && inFile.getName().equals(curFileName)){
                process = true;
                break;
            }
        }
        
        return process;
    }
    

    public void transcodeDirectory(String directory) {
        String dirPath = directory;
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if(files == null){
            logger.log(Level.INFO, "No files to transcode in the specified directory!");
            return;
        }

        if (files.length == 0) {
            logger.log(Level.INFO, "The directory is empty");
        } else {
            logger.log(Level.INFO, "TRANSCODING DIRECTORY: " + dirPath + "\n\n");
            for (File aFile : files) {
                if (fp.isSupportedFileType(aFile.toPath())) {
                    //System.out.println(aFile.getName() + " - " + aFile.length());
                    try {
                        //transcode files
                        fp.transcode(aFile.getAbsolutePath());
                    } catch (Exception e) {
                        //TODO: HANDLE
                    }
                }
            }
        }

    }

    private HashMap<Integer, Clip> processFile(File file, HashMap<Integer, Clip> processedFiles) throws IOException {
        logger.log(Level.INFO, "Processing file: " + file.getAbsolutePath());

        String[] cmdOut;
        String duration;
        String framerate;
        String frames;
        String resolution;
        String width;
        String height;
        String mlt;
        String supplier;

        //duration in seconds
        cmdOut = fp.execFfprobe("-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            logger.log(Level.SEVERE, "ERROR getting Duration: ");
            logger.log(Level.SEVERE, cmdOut[1]);
        }
        String duration2 = cmdOut[0];
        duration = fp.secondsToDuration(cmdOut[0]);

        //framerate
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=avg_frame_rate -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            logger.log(Level.SEVERE, "ERROR getting Framerate: ");
            logger.log(Level.SEVERE, cmdOut[1]);
        }
        framerate = fp.getFPS(cmdOut[0]);

        //frames
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=nb_frames -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            logger.log(Level.SEVERE, "ERROR getting Frames: ");
            logger.log(Level.SEVERE, cmdOut[1]);
        }
        frames = cmdOut[0];

        //resolution
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=width -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            logger.log(Level.SEVERE, "ERROR getting Resolution: ");
            logger.log(Level.SEVERE, cmdOut[1]);
        }
        width = cmdOut[0];
        cmdOut = fp.execFfprobe("-v error -select_streams v:0 -show_entries stream=height -of default=noprint_wrappers=1:nokey=1 " + file.getAbsolutePath());
        if (!"".equals(cmdOut[1])) {
            logger.log(Level.SEVERE, "ERROR getting Resolution: ");
            logger.log(Level.SEVERE, cmdOut[1]);
        }
        height = cmdOut[0];
        resolution = width + "x" + height;
        logger.log(Level.INFO, "Duration: " + duration + ", Framerate: " + framerate + ", Frames: " + frames + ", Resolution: " + resolution);

        
        //Thumbnails
        List<String> thumbArray = fp.generateThumbnails(file, duration2, "2");
        
        //Generate .mlt
        mlt = fp.createMltFile(file.getAbsolutePath(),frames,framerate);
        logger.log(Level.INFO, mlt);
        
        //get Supplier
        supplier = file.getParentFile().getName();

        Clip clip = new Clip();
        clip.setName(file.getName());
        clip.setPath(file.getAbsolutePath());

        clip.setThumbnails(thumbArray);
        clip.setDuration(duration);
        clip.setFps(framerate);
        clip.setFrames(frames);
        clip.setDescription("descripcion generica");
        clip.setResolution(resolution);
        clip.setSupplier(supplier);
        logger.log(Level.INFO, "clip = " + clip.toString());
        logger.log(Level.INFO, "Send Network Request to playout-api/medias:\n");

        //POSTing in Resty
        Gson gson = new Gson();
        String jsonClip = gson.toJson(clip);
        Map<String, String> clipMap = gson.fromJson(jsonClip, Map.class);
        Resty resty = new Resty(Resty.Option.timeout(4000));
        JSONObject jsonObject = new JSONObject(clipMap);
        
        String mediaId="";
        String mediaName="---";
        try {
            try {
                mediaName = jsonObject.getString("name");
            } catch (JSONException e) {}
            
            mediaId = (String)resty.json(playoutApiBaseUrl+"medias", Resty.content(jsonObject)).get("id");
            logger.log(Level.INFO, "mediaid:" +mediaId);
        } catch (ConnectException e) {
            logger.log(Level.SEVERE, "An error occurred while using medias API. Quiting...\n{0}", e.getMessage());
//            System.exit(1);
        } catch (IOException e) {
            // Hubo algún problema con el post a medias
            logger.log(Level.SEVERE, "A media with the same name \"{0}\" already exists! Quiting...", mediaName);
//            System.exit(1);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred while using medias API.\nUnhandlede exception.\n Quiting...\n{0}", ex.getMessage());
//            System.exit(1);
        }
        
        // Le pongo el mediaId al clip y lo mando a la api de pieces
        clip.setMediaId(mediaId);
        clip.setPath(mlt);
        //TODO: copypasteado
        jsonClip = gson.toJson(clip);
        clipMap = gson.fromJson(jsonClip, Map.class);
        jsonObject = new JSONObject(clipMap);     
        logger.log(Level.INFO, "clip = " + clip.toString());
        logger.log(Level.INFO, "Send Network Request to playout-api/thumbnails:\n");
        JSONResource insertedThumbResult = resty.json(playoutApiBaseUrl+"thumbnails", Resty.content(jsonObject));
        
        logger.log(Level.INFO, "Send Network Request to playout-api/pieces:\n");
        JSONResource insertedResult = resty.json(playoutApiBaseUrl+"pieces", Resty.content(jsonObject));

        try {
            int pieceId = Integer.parseInt(insertedResult.toObject().get("id").toString()); // get's piece id from result
            processedFiles.put(pieceId, clip); // Put's a new entry into the processedFiles so that later, someone changes the .mlt file to include the pieceId

        } catch (JSONException ex) {
            // TODO: HANDLE
            logger.log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex){
            // TODO: HANDLE
            logger.log(Level.SEVERE, null, ex);
        }
        
        // Agarro el supplier del clip y pregunto si existe antes de insertarlo
        logger.log(Level.INFO, "clip = " + clip.toString());
        logger.log(Level.INFO, "Send Network Request to admin-api/supplier/name/"+supplier);
        String supplierId=".";
        JSONResource supplierResource;
        try{
            //JSONArray supplierResource = resty.json(adminApiBaseUrl+"supplier/name/"+supplier).array();
            supplierResource = resty.json(adminApiBaseUrl+"supplier/name/"+supplier);
            if(supplierResource.object().length() == 0  ){
                logger.log(Level.INFO, "JSON SUPPLIER IS NULL, INSERT NEW ONE");
                supplierId = (String)resty.json(adminApiBaseUrl+"supplier", Resty.content(jsonObject)).get("id");
            }else{
                logger.log(Level.INFO, "JSON SUPPLIER IS NOT NULL, GET EXISTENT ID"); 
                supplierId = (String) supplierResource.get("id");
            }
            
        } catch (IOException e) {
            // Hubo algún problema con el post a supplier
            logger.log(Level.SEVERE, "Error getting a Supplier with name the name \"{0}\"! Quiting...", supplier);
//            System.exit(1);
        }catch (Exception e) {
            logger.log(Level.SEVERE, "An error occured while using ADMIN API. Quiting...\n{0}", e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
        }
        
        // Agarro el supplier id, se lo cargo al clip y lo mando a rawMedias
        clip.setSupplierId(supplierId);
        jsonClip = gson.toJson(clip);
        clipMap = gson.fromJson(jsonClip, Map.class);
        jsonObject = new JSONObject(clipMap);        
        
        logger.log(Level.INFO, "clip = " + clip.toString());
        logger.log(Level.INFO, "Send Network Request to playout-api/rawMedia:\n");
        // Envio a la Admin Api para RawMedia
        resty.json(adminApiBaseUrl+"rawMedia", Resty.content(jsonObject));

        return processedFiles;
    }
}
