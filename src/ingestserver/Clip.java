package ingestserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Clip class used as a data structure containing relevant data.
 *
 * @author cyberpunx
 */
public class Clip {

    private String name;
    private String path;
    private List<Thumbnail> thumbnails;
    private String duration;
    private String fps;
    private String frames;

    @Override
    public String toString() {
        return "Clip{" + "name=" + name + ", path=" + path + ", thumbnails=" + thumbnails + ", duration=" + duration + ", fps=" + fps + ", frames=" + frames + '}';
    }

    /**
     * Initializes the clip with null data
     */
    public Clip() {
        this.thumbnails = new ArrayList<>();
    }

    /**
     *
     * @return name of the video file.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name sets name the video file.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return absolute path to video file
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @param path the absolute path to video file
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Given a String array where each position is a absolute path to a
     * thumbnail. Generates and set a Thumbnail class array.
     *
     * @param thumbnails array of strins. Each string is a Path to a thumbnail.
     */
    public void setThumbnails(List<String> thumbnails) {
        for (final String thumbstring : thumbnails) {
            Thumbnail thumb = new Thumbnail();
            thumb.setPath(thumbstring);
            this.thumbnails.add(thumb);
        }
    }

    /**
     *
     * @return duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     *
     * @param duration the duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     *
     * @return framerate
     */
    public String getFps() {
        return fps;
    }

    /**
     *
     * @param fps the framerate
     */
    public void setFps(String fps) {
        this.fps = fps;
    }

    /**
     *
     * @return total number of frames in video
     */
    public String getFrames() {
        return frames;
    }

    /**
     *
     * @param frames the total number of frames in video
     */
    public void setFrames(String frames) {
        this.frames = frames;
    }

}

class Thumbnail {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
