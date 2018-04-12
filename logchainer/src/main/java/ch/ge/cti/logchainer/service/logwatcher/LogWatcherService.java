package ch.ge.cti.logchainer.service.logwatcher;

import ch.ge.cti.logchainer.generate.LogChainerConf;

public interface LogWatcherService {
    /**
     * Create a list where all clients are registered after being instantiated
     * as Client objects. Initialize their WatchKey.
     * 
     * @param clientConfList
     */
    void initializeFileWatcherByClient(LogChainerConf clientConfList);

    /**
     * Infinity loop checking for updates in the directoy and then does the file
     * treatment by calling all necessary methods correctly.
     */
    void processEvents();
}
