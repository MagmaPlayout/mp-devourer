package ingestserver;

import java.util.ArrayList;
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
    private String frameRate;
    private String frameCount;
    private String id;
    private String description;
    private String resolution;
    private String mediaId;
    private String supplier;
    private String supplierId;

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public long getId() {
        return Long.getLong(id);
    }

    public void setId(long id) {
        this.id = String.valueOf(id);
    }

    @Override
    public String toString() {
        return "Clip{" + "id=" + id + "name=" + name + ", path=" + path + ", thumbnails=" + thumbnails + ", duration=" + duration + ", fps=" + frameRate + ", frames=" + frameCount + ", id=" + mediaId + ", supplier=" + supplier + ", supplierId=" + supplierId +'}';
    }

    /**
     * Initializes the clip with null data
     */
    public Clip() {

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
        List<Thumbnail> thumbList = new ArrayList<>();
        for (final String thumbstring : thumbnails) {
            Thumbnail thumb = new Thumbnail();
            thumb.setPath(thumbstring);
            thumbList.add(thumb);
        }
        List<Thumbnail> newList = new ArrayList<>(thumbList);
        //Gson gson = new Gson();
        //String thumbString = gson.toJson(thumbList);
        this.thumbnails = newList;
    }

    public List<Thumbnail> getThumbnails() {
        return thumbnails;
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
        return frameRate;
    }

    /**
     *
     * @param fps the framerate
     */
    public void setFps(String fps) {
        this.frameRate = fps;
    }

    /**
     *
     * @return total number of frameCount in video
     */
    public String getFrames() {
        return frameCount;
    }

    /**
     *
     * @param frames the total number of frameCount in video
     */
    public void setFrames(String frames) {
        this.frameCount = frames;
    }

    void setDescription(String desc) {
        this.description = desc;
    }

    String getDescription() {
        return this.description;
    }

    void setResolution(String reso) {
        this.resolution = reso;
    }

    String getResolution() {
        return this.resolution;
    }

    void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }
    
    String getSupplierId() {
        return this.supplierId;
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
