/*
 * <Log Chainer>
 *
 * Copyright (C) 2018 R�publique et Canton de Gen�ve
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.ge.cti.logchainer.monitoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.stereotype.Component;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Component
public class Metrics {
    /**
     * Counts the number of corrupted files put in the corrupted files directory
     * and registers it for monitoring.
     */
    public void registerTotalNbCorruptedFiles() {
	SimpleMeterRegistry registry = new SimpleMeterRegistry();
	Counter counterTotalNbCorruptedFiles = Counter.builder("corruptedFilesNb").register(new SimpleMeterRegistry());

	LogWatcherServiceImpl.getClients().stream()
		.forEach(clientFromList -> getFilesAlreadyInDirectory(clientFromList.getConf().getCorruptedFilesDir())
			.stream().forEach(corrFile -> counterTotalNbCorruptedFiles.increment()));

	String nbCorruptedFiles = String.valueOf(counterTotalNbCorruptedFiles.count());

	Counter.builder("corruptedFilesNb")
		.description("counts the number of files that have been transfered to the corruptedFiles directory")
		.baseUnit("number of files").tags("corrupted.files.detected.number", nbCorruptedFiles)
		.register(registry);

	Controller.composite.add(registry);
    }

    /**
     * Registers informations about all the detected corrupted files for
     * monitoring.
     */
    public void registerAllCorruptedFiles() {
	Map<String, ArrayList<Tag>> clientWithTags = new HashMap<>();
	Map<String, Counter> clientWithCounter = new HashMap<>();

	LogWatcherServiceImpl.getClients().stream().forEach(new Consumer<Client>() {
	    @Override
	    public void accept(Client client) {
		clientWithTags.put(client.getConf().getClientId(), new ArrayList<>());

		Counter counterForClient = Counter.builder("clientCounter").register(new SimpleMeterRegistry());
		clientWithCounter.put(client.getConf().getClientId(), counterForClient);

		getFilesAlreadyInDirectory(client.getConf().getCorruptedFilesDir()).stream()
			.forEach(new Consumer<File>() {
			    @Override
			    public void accept(File corruptedFile) {
				Tag tag = new ImmutableTag(corruptedFile.getName(),
					String.valueOf(corruptedFile.length()));
				clientWithTags.get(client.getConf().getClientId()).add(tag);
				clientWithCounter.get(client.getConf().getClientId()).increment();
			    }
			});
	    }
	});

	clientWithTags.keySet()
		.forEach(clientName -> Counter.builder(clientName).baseUnit("bytes (file size)")
			.tags(clientWithTags.get(clientName))
			.description(String.valueOf(clientWithCounter.get(clientName).count())
				+ " files were put in the corrupted files directory for this client")
			.register(Controller.composite));
    }

    /**
     * Registers informations about the corrupted files of a specified client
     * for monitoring.
     * 
     * @param clientId
     */
    public void registerCorruptedFiles(String clientId) {
	Collection<File> corruptedFilesForClient = new ArrayList<>();

	LogWatcherServiceImpl.getClients().stream().forEach(new Consumer<Client>() {
	    @Override
	    public void accept(Client client) {
		if (client.getConf().getClientId().equals(clientId)) {
		    Collection<File> corruptedFiles = getFilesAlreadyInDirectory(
			    client.getConf().getCorruptedFilesDir());
		    corruptedFilesForClient.addAll(corruptedFiles);
		    Counter.builder(clientId).description("number of corrupted files for this client")
			    .baseUnit("number of files")
			    .tags("corrupted.files.number", String.valueOf(corruptedFiles.size()))
			    .register(Controller.composite);
		}
	    }
	});

	corruptedFilesForClient.stream()
		.forEach(corruptedFile -> Counter.builder(corruptedFile.getName())
			.description("this file has been put in the corrupted files directory").baseUnit("bytes")
			.tags("file.size", String.valueOf(corruptedFile.length())).register(Controller.composite));
    }

    /**
     * Get the files present in the specified directory.
     * 
     * @param workingDir
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<File> getFilesAlreadyInDirectory(String workingDir) {
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		return file.getName().equals("readme") ? false : true;
	    }
	}, null);

    }

}
