package ingestserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import libconfig.ConfigurationManager;

/**
 * Provide functions to process video files by FFmpeg/FFprobe. FFmpeg and
 * FFprobe paths are read from configuration file.
 *
 * @author cyberpunx
 */
public class FileProcessor {
    public static final String DEFAULT_PROVIDER = "Default";
    private final String ffprobe;
    private final String ffmpeg;
    private final String inDir;
    private final String outDir;
    private final String thumbDirectory;
    private final String meltPath;
    private final String fps;
    private final String ffmpegArgs;
    private final Logger logger;

    /**
     * Reads FFmpeg and FFprobe paths from properties file.
     */
    public FileProcessor(Logger logger) {
        ConfigurationManager cfg = ConfigurationManager.getInstance();
        String mltFwPath = cfg.getMltFrameworkPath();
        this.logger = logger;
        this.ffmpeg = mltFwPath+"/ffmpeg";
        this.ffprobe = mltFwPath+"/ffprobe";
        this.meltPath = mltFwPath+"/melt";
        this.inDir = cfg.getDevourerInputDir();
        this.outDir = cfg.getDevourerOutputDir();
        this.thumbDirectory = cfg.getDevourerThumbDir();
        this.fps = Integer.toString(cfg.getMediasFPS());
        this.ffmpegArgs = cfg.getDevourerFfmpegArgs();
    }

    /**
     * Just adds PT[input]S to a String given representing seconds.
     *
     * @param seconds: Just a String in seconds
     * @return a String in seconds followin the ISO 8601 format.
     */
    public String secondsToDuration(String seconds) {
        return "PT" + seconds + "S";
    }

    /**
     * Takes the Ffprobe Framerate as a rational number and gives the integer
     * result, rounded up.
     *
     * @param fpsInput Framerate as given by Ffprobe in x/x format.
     * @return Framerate result, rounded up.
     */
    public String getFPS(String fpsInput) {
        String[] parts = fpsInput.split("/");
        int part1 = Integer.parseInt(parts[0]);
        int part2 = Integer.parseInt(parts[1]);
        int fpsint = (int) Math.ceil((double) part1 / part2); //rounds up
        
        return Integer.toString(fpsint);
    }
    
    public String createMltFile(String clip, String frameLen, String fps) throws IOException{
        clip = clip.replace("\"", "");
        String xmlPath;
        xmlPath = clip.substring(0, clip.lastIndexOf('.'))+".mlt";
        String cmdString = meltPath+" "+clip+" out="+frameLen+" length="+frameLen+" -consumer xml:"+xmlPath+" frame_rate_num="+fps;
        execCmd(cmdString);        
        return clip.substring(0, clip.lastIndexOf('.'))+".mlt";
    }

    /**
     * Given a video file, creates a directory and, using Ffmpeg, fills that
     * directory with thumbnails from the video.
     *
     * @param file video file
     * @param duration integer duration in seconds of video
     * @param thumbCount number of generated thumbnails
     * @return an array with all thumbnails absolute paths
     * @throws IOException
     */
    public List<String> generateThumbnails(File file, String duration, String thumbCount) throws IOException {
        //create /thumbnail directory if not exists
        File thumbDirPath = new File(this.thumbDirectory);
        if (thumbDirPath.mkdir()) {
            logger.log(Level.INFO, "Successfully created new file: %s%n", thumbDirPath.getAbsolutePath());
        } else if(!thumbDirPath.isDirectory()){
            logger.log(Level.SEVERE, "Failed to create new file: %s%n", thumbDirPath.getAbsolutePath());
        }
        
        

        //get filename without extension
        String fname = file.getName();
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);
        }
        File thumbDir = new File(this.thumbDirectory + "/" + fname);
        thumbDir.mkdir();

        duration = duration.split("\\.")[0];
        execFfmpeg("-i " + file.getAbsolutePath() + " -vf fps=" + thumbCount + "/" + duration + " " + thumbDir + "/" + fname + "_%03d.png ");

        String dirPath = thumbDir.getAbsolutePath();
        List<String> thumbArray = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files.length == 0) {
            logger.log(Level.WARNING, "The directory is empty");
        } else {
            for (File aFile : files) {
                if (aFile.toString().toLowerCase().endsWith(".png")) {
                    thumbArray.add(aFile.getAbsolutePath());
                }
            }
        }

        //thumbArray.forEach(System.out::println);
        return thumbArray;
    }

    public void transcode(String inputString) {
        File input = new File(inputString);
        String supplier = input.getParentFile().getName();
             
        String[] cmdOut;
        String outputString;
                
        // If supplier is input folder. Output supplier = Default (DEFAULT_PROVIDER)
        File inDir = new File(this.inDir);
        if(supplier.equals(inDir.getName())){
            logger.log(Level.WARNING, "supplier is input!  " +supplier+"="+inDir.getName());
            new File(this.outDir+"/"+DEFAULT_PROVIDER).mkdirs(); // Creates output/supplier folder
            outputString = this.outDir + "/"+DEFAULT_PROVIDER+"/" +input.getName();
        }else{
            new File(this.outDir+"/"+supplier).mkdirs(); // Creates output/supplier folder
            outputString = this.outDir + "/" + supplier+ "/" +input.getName();
        }        
        File output = new File(outputString);
        
        logger.log(Level.INFO, "Input file: "+inputString +", Output file: "+output);
        if (output.exists()) {
            logger.log(Level.INFO, "Output file already exists. No transcoding needed");
        } else {
            try {
                logger.log(Level.INFO, "Transcoding input file.");
                cmdOut = execFfmpeg("-i " + inputString +" -r " + this.fps + " " +this.ffmpegArgs + " " + outputString);
                
                if(false){ // Activate this for debugging the ffmpeg command
                    // stdout
                    if (!"".equals(cmdOut[1])) {
                        logger.log(Level.INFO, cmdOut[1]);
                    }

                    // stderr
                    if (!"".equals(cmdOut[0])) {
                        logger.log(Level.INFO, cmdOut[0]);
                    }
                }
            } catch (IOException ex) {
                //TODO: HADLE
                Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Executes a FFprobe commands and returns the output.
     *
     * @param cmd command to be executed by ffprobe.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execFfprobe(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(ffprobe + " " + cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    /**
     * Executes a terminal command and return the output.
     *
     * @param cmd command to be executed.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execCmd(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    /**
     * Executes a FFmpeg commands and returns the output.
     *
     * @param cmd command to be executed.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execFfmpeg(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(ffmpeg + " " + cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    /**
     * Given a file, returns if it is a valid format to be processed by
     * FileProcessor.
     *
     * @param file to be analyzed as a valid format.
     * @return True if File is a valid forma (video). False otherwise.
     */
    public boolean isSupportedFileType(Path file) {
        return (
            file.toString().toLowerCase().endsWith(".mp4")  || 
            file.toString().toLowerCase().endsWith(".webm") ||
            file.toString().toLowerCase().endsWith(".mkv")  ||
            file.toString().toLowerCase().endsWith(".avi")  ||
            file.toString().toLowerCase().endsWith(".flv")  ||
            file.toString().toLowerCase().endsWith(".mpeg") ||
            file.toString().toLowerCase().endsWith(".gif")  ||
            file.toString().toLowerCase().endsWith(".m4v")  ||
            file.toString().toLowerCase().endsWith(".wmv")  ||
            file.toString().toLowerCase().endsWith(".mov")
        );
    }

}
