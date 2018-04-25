package ch.ge.cti.logchainer.service.folder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.exception.BusinessException;
import ch.ge.cti.logchainer.service.folder.FolderService;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;

public class FolderServiceTest {
    private final String testResourcesDirPath = "src/test/resources";
    private final FolderService mover = new FolderServiceImpl();

    @Test(description = "testing the move of files")
    public void testMovingFile() throws Exception {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"), noData.getBytes());

	mover.moveFileInDirWithNoSameNameFile("testMovingFile1.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.moveFileInDirWithNoSameNameFile("testMovingFile2.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesMoved = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");

	assertEquals(
		existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt")),
		true);
	assertEquals(
		existingFilesMoved.contains(new File(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt")),
		true);

	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	try {
	    mover.moveFileInDirWithNoSameNameFile("testMovingFile1.txt", testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), FileAlreadyExistsException.class);
	}

	try {
	    mover.moveFileInDirWithNoSameNameFile("nonExistingFile", testResourcesDirPath,
		    testResourcesDirPath + "/testMovingToFolder");
	} catch (BusinessException e) {
	    assertEquals(e.getCause().getClass(), NoSuchFileException.class);
	}

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"));
    }

    @Test(description = "testing the copy of files")
    public void testCopyingFile() throws IOException {
	String noData = "";
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"), noData.getBytes());
	Files.write(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"), noData.getBytes());

	mover.copyFileToDirByReplacingExisting("testMovingFile1.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	mover.copyFileToDirByReplacingExisting("testMovingFile2.txt", testResourcesDirPath,
		testResourcesDirPath + "/testMovingToFolder");

	Collection<File> existingFilesCopied = getPreviousFiles(testResourcesDirPath + "/testMovingToFolder");
	assertTrue(existingFilesCopied
		.contains(new File(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt")));
	assertTrue(existingFilesCopied
		.contains(new File(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt")));

	Collection<File> existingFilesStaying = getPreviousFiles(testResourcesDirPath);
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/testMovingFile1.txt")));
	assertTrue(existingFilesStaying.contains(new File(testResourcesDirPath + "/testMovingFile2.txt")));

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingToFolder/testMovingFile2.txt"));

	Files.delete(Paths.get(testResourcesDirPath + "/testMovingFile1.txt"));
	Files.delete(Paths.get(testResourcesDirPath + "/testMovingFile2.txt"));
    }

    @SuppressWarnings("unchecked")
    private Collection<File> getPreviousFiles(String workingDir) {
	// filtering the files to only keep the same as given flux one (should
	// be unique)
	return FileUtils.listFiles(new File(workingDir), new IOFileFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return accept(new File(name));
	    }

	    @Override
	    public boolean accept(File file) {
		return true;
	    }
	}, null);
    }
}