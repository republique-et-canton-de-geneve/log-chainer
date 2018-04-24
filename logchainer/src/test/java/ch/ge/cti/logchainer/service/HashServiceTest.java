package ch.ge.cti.logchainer.service;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ge.cti.logchainer.service.hash.HashService;

public class HashServiceTest {
    private final String pathTestFile = "src/test/resources/testHashCode";

    @Autowired
    private HashService hasher;

    @Test(description = "hash method test")
    public void testHashCode() throws IOException {
	byte[] refArray = new byte[] {14, -39, -87, 75, 76, -6, -127, -30, 51, 126, -4, 21, 5, 102, -98, 25, 100, -62,
		91, -19, 117, 1, 50, 118, -89, 57, -10, 11, -8, -49, 15, -18 };

	try (InputStream fileToTest = new FileInputStream(new File(pathTestFile))) {
	    hasher.getNullHash();
	    assertEquals(hasher.getLogHashCode(fileToTest), refArray);
	}
    }

    @Test(description = "null hash method test")
    public void testNullHash() {
	byte[] nullHash = new byte[]{};
	
	assertEquals(hasher.getNullHash(), nullHash);
    }
}
