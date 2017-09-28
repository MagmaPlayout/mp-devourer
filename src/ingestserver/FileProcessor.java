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

    private String ffprobe;
    private final String ffmpeg;
    private String mediaDirectory;
    private String inDir;
    private String outDir;
    private String thumbDirectory;
    private String meltPath;
    private String fps;

    /**
     * Reads FFmpeg and FFprobe paths from properties file.
     */
    public FileProcessor() {    
   
        ConfigurationManager cfg = ConfigurationManager.getInstance();
        String mltFwPath = cfg.getMltFrameworkPath();
        this.ffmpeg = mltFwPath+"/ffmpeg";
        this.ffprobe = mltFwPath+"/ffprobe";
        this.meltPath = mltFwPath+"/melt";
        this.mediaDirectory = cfg.getDevourerOutputDir();
        this.inDir = cfg.getDevourerInputDir();
        this.outDir = cfg.getDevourerOutputDir();
        this.thumbDirectory = cfg.getDevourerThumbDir() + "/thumbnails";
        this.fps = Integer.toString(cfg.getMediasFPS());
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
        String fps = Integer.toString(fpsint);
        return fps;
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
    public List<String> generateThumbnailGIF(File file, String duration, String thumbCount) throws IOException {

        //create /thumbnail directory if not exists
        File thumbDirPath = new File(this.thumbDirectory);
        if (thumbDirPath.exists()) {
            System.out.println("thumbnail dir already exists");
        } else {
            System.out.println("thumbnail dir not exists, creating");
            if (thumbDirPath.mkdir()) {
                System.out.printf("Successfully created new file: %s%n", thumbDirPath.getAbsolutePath());
            } else {
                System.out.printf("Failed to create new file: %s%n", thumbDirPath.getAbsolutePath());
            }
        }

        String[] cmdOut;
        duration = duration.split("\\.")[0];
        List<String> thumbArray = new ArrayList<>();

        //get filename without extension
        String fname = file.getName();
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);
        }
        File thumbDir = new File(this.thumbDirectory + "/" + fname);
        thumbDir.mkdir();

        cmdOut = execFfmpeg("-i " + file.getAbsolutePath() + " -vf fps=" + thumbCount + "/" + duration + " " + thumbDir + "/" + fname + "_%03d.png ");

        String dirPath = thumbDir.getAbsolutePath();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files.length == 0) {
            System.out.println("The directory is empty");
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

    public void transcodeLoselessH264(String inputString) {
        File input = new File(inputString);
        String provider = input.getParentFile().getName();
             
        String[] cmdOut;
        String outputString;
                
        // If provider is input folder. Output provider = Default
        File inDir = new File(this.inDir);
        if(provider.equals(inDir.getName())){ 
            System.out.println("provider is input!  " +provider+"="+inDir.getName());
            new File(this.outDir+"/Default").mkdirs(); // Creates output/provider folder   
            outputString = this.outDir + "/Default/" +input.getName();
        }else{
            new File(this.outDir+"/"+provider).mkdirs(); // Creates output/provider folder   
            outputString = this.outDir + "/" + provider+ "/" +input.getName();
        }        
        File output = new File(outputString);
        
        System.out.println("Input file: "+inputString);
        System.out.println("Output file: "+output);

        if (output.exists()) {
            System.out.println("FILE OUTPUT ALREADY EXISTS: " + outputString + " -- NO TRANSCODE DONE");
        } else {
            try {
                System.out.println("TRANSCODING: " + inputString);
                cmdOut = execFfmpeg("-i " + inputString + " -f avi -r "+this.fps+" -c:v libx264 -qp 0  " + outputString);
                if (!"".equals(cmdOut[1])) {
                    System.out.println(cmdOut[1]);
                }
                if (!"".equals(cmdOut[1])) {
                    System.out.println(cmdOut[0]);
                }

            } catch (IOException ex) {
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
    public boolean isCorrectFileType(Path file) {
        boolean flag = false;
        if (file.toString().toLowerCase().endsWith(".mp4")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".webm")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mkv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".avi")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".flv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mpeg")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".gif")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".m4v")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".wmv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mov")) {
            flag = true;
        }
//        } else if (file.toString().toLowerCase().endsWith(".txt")) {
//            flag = true;
//        }
        return flag;
    }

}
