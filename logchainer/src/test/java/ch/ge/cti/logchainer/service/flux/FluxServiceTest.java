package ch.ge.cti.logchainer.service.flux;

import static ch.ge.cti.logchainer.constant.LogChainerConstant.SEPARATOR_DEFAULT;
import static ch.ge.cti.logchainer.constant.LogChainerConstant.STAMP_POSITION_DEFAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.beans.Client;
import ch.ge.cti.logchainer.beans.WatchedFile;
import ch.ge.cti.logchainer.generate.ClientConf;
import ch.ge.cti.logchainer.service.file.FileServiceImpl;
import ch.ge.cti.logchainer.service.folder.FolderServiceImpl;
import ch.ge.cti.logchainer.service.helper.FileHelper;
import ch.ge.cti.logchainer.service.logwatcher.LogWatcherServiceImpl;

public class FluxServiceTest {
    private final FluxServiceImpl fluxService = new FluxServiceImpl();
    private final String testFilename = "fluxTest_stampTest.txt";
    private final String testFilenameChangeSeparator = "fluxTest-stampTest.txt";
    private final String testFilenameChangeStampPosition = "stampTest_fluxTest.txt";
    private final String testFilenameChangeSeparatorAndStampPosition = "stampTest-fluxTest.txt";
    private ArrayList<WatchedFile> watchedFiles = new ArrayList<>();
    private Map<String, ArrayList<WatchedFile>> mapFluxFiles;

    @BeforeClass
    public void setUp() {
	watchedFiles.add(new WatchedFile("file1"));
	watchedFiles.add(new WatchedFile("file2"));
	watchedFiles.add(new WatchedFile("file3"));
	watchedFiles.add(new WatchedFile("file4"));

	mapFluxFiles = new HashMap<>();
    }

    @Test(description = "testing the method getting the flux name")
    public void testGetFluxName() {
	String fluxname = fluxService.getFluxName(testFilename, SEPARATOR_DEFAULT, STAMP_POSITION_DEFAULT);
	assertEquals(fluxname, "fluxTest");

	String fluxnameChangeSeparator = fluxService.getFluxName(testFilenameChangeSeparator, "-",
		STAMP_POSITION_DEFAULT);
	assertEquals(fluxnameChangeSeparator, "fluxTest");

	String fluxnameChangeStampPosition = fluxService.getFluxName(testFilenameChangeStampPosition, SEPARATOR_DEFAULT,
		"before");
	assertEquals(fluxnameChangeStampPosition, "fluxTest");

	String fluxnameChangeSeparatorAndStampPosition = fluxService
		.getFluxName(testFilenameChangeSeparatorAndStampPosition, "-", "before");
	assertEquals(fluxnameChangeSeparatorAndStampPosition, "fluxTest");
    }

    @Test(description = "testing the method getting the stamp used to sort files")
    public void testGetSortingStamp() {
	String stamp = fluxService.getSortingStamp(testFilename, SEPARATOR_DEFAULT, STAMP_POSITION_DEFAULT);
	assertEquals(stamp, "stampTest");

	String stampChangeSeparator = fluxService.getSortingStamp(testFilenameChangeSeparator, "-",
		STAMP_POSITION_DEFAULT);
	assertEquals(stampChangeSeparator, "stampTest");

	String stampChangeStampPosition = fluxService.getSortingStamp(testFilenameChangeStampPosition,
		SEPARATOR_DEFAULT, "before");
	assertEquals(stampChangeStampPosition, "stampTest");

	String stampChangeSeparatorAndStampPosition = fluxService
		.getSortingStamp(testFilenameChangeSeparatorAndStampPosition, "-", "before");
	assertEquals(stampChangeSeparatorAndStampPosition, "stampTest");
    }

    @Test(description = "testing when the is ready to be treated")
    public void testIsFluxReadyToBeTreated() {
	watchedFiles.stream().forEach(file -> file.setReadyToBeProcessed(true));
	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.entrySet().stream().forEach(flux -> assertTrue(fluxService.isFluxReadyToBeTreated(flux)));
	mapFluxFiles.clear();

	ArrayList<WatchedFile> nonReadyWatchedFiles = new ArrayList<>();
	nonReadyWatchedFiles.add(new WatchedFile("file1"));
	nonReadyWatchedFiles.add(new WatchedFile("file2"));
	nonReadyWatchedFiles.get(1).setReadyToBeProcessed(true);
	mapFluxFiles.put("fluxTest2", nonReadyWatchedFiles);
	mapFluxFiles.entrySet().stream().forEach(flux -> assertFalse(fluxService.isFluxReadyToBeTreated(flux)));
    }

    @Test(description = "testing the treatment of the flux")
    public void testFluxTreatment() {
	LogWatcherServiceImpl watcherService = mock(LogWatcherServiceImpl.class);
	fluxService.watcherService = watcherService;
	FileServiceImpl fileService = mock(FileServiceImpl.class);
	fluxService.fileService = fileService;
	FileHelper fileHelper = mock(FileHelper.class);
	fluxService.fileHelper = fileHelper;

	Client client = mock(Client.class);

	List<String> refList = new ArrayList<>();
	refList.add("fluxTest1");
	refList.add("fluxTest2");
	List<String> fluxList = new ArrayList<>();

	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.put("fluxTest2", watchedFiles);

	when(watcherService.treatmentAfterDetectionOfEvent(any(Client.class), anyString(), any())).thenReturn(true);
	doNothing().when(fileService).sortFiles(anyString(), anyString(), anyString(), any());
	when(fileHelper.getSeparator(client)).thenReturn(null);
	when(fileHelper.getSorter(client)).thenReturn(null);

	mapFluxFiles.entrySet().stream().forEach(fluxname -> fluxService.fluxTreatment(client, fluxList, fluxname));

	assertEquals(fluxList.size(), refList.size());
	fluxList.stream().forEach(flux -> assertTrue(refList.contains(flux)));
    }

    @Test(description = "testing the process of a corrupted file")
    public void testCorruptedFluxProcess() {
	LogWatcherServiceImpl watcherService = mock(LogWatcherServiceImpl.class);
	fluxService.watcherService = watcherService;
	FileServiceImpl fileService = mock(FileServiceImpl.class);
	fluxService.fileService = fileService;
	FileHelper fileHelper = mock(FileHelper.class);
	fluxService.fileHelper = fileHelper;
	FolderServiceImpl mover = mock(FolderServiceImpl.class);
	fluxService.mover = mover;

	ClientConf clientConf = new ClientConf();
	clientConf.setInputDir(null);
	clientConf.setCorruptedFilesDir(null);

	Client client = new Client(clientConf);

	List<String> refList = new ArrayList<>();
	refList.add("fluxTest1");
	refList.add("fluxTest2");
	List<String> fluxList = new ArrayList<>();

	mapFluxFiles.put("fluxTest1", watchedFiles);
	mapFluxFiles.put("fluxTest2", watchedFiles);

	when(mover.moveFileInDirWithNoSameNameFile(anyString(), anyString(), anyString())).thenReturn(null);

	mapFluxFiles.entrySet().stream()
		.forEach(fluxname -> fluxService.corruptedFluxProcess(client, fluxList, fluxname));

	assertEquals(fluxList.size(), refList.size());
	fluxList.stream().forEach(flux -> assertTrue(refList.contains(flux)));
    }
}
