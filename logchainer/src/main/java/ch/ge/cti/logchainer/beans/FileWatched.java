package ch.ge.cti.logchainer.beans;

import java.nio.file.WatchEvent;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean grouping all watched file related attributes.
 * 
 * @author FANICHETL
 *
 */
public class FileWatched {
    private final static int CONVERT_HOUR_TO_SECONDS = 3600;
    private final static int CONVERT_MINUTE_TO_SECONDS = 60;

    private final String filename;
    private final int arrivingTime;
    private boolean readyToBeTreated = false;
    private WatchEvent.Kind<?> kind;
    private boolean registered;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileWatched.class.getName());

    public FileWatched(String filename) {
	LOG.debug("client infos instantiated");
	this.filename = filename;
	this.arrivingTime = LocalDateTime.now().getHour() * CONVERT_HOUR_TO_SECONDS
		+ LocalDateTime.now().getMinute() * CONVERT_MINUTE_TO_SECONDS + LocalDateTime.now().getSecond();
    }

    public String getFilename() {
	return filename;
    }

    public int getArrivingTime() {
	return arrivingTime;
    }

    public boolean isReadyToBeTreated() {
	return readyToBeTreated;
    }

    public void setReadyToBeTreated(boolean readyToBeTreated) {
	this.readyToBeTreated = readyToBeTreated;
    }

    public WatchEvent.Kind<?> getKind() {
	return kind;
    }

    public void setKind(WatchEvent.Kind<?> kind) {
	this.kind = kind;
    }

    public boolean isRegistered() {
	return registered;
    }

    public void setRegistered(boolean registered) {
	this.registered = registered;
    }
}
