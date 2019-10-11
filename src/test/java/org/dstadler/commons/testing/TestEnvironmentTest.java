package org.dstadler.commons.testing;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestEnvironmentTest {
    @After
    public void tearDown() throws IOException {
        TestEnvironment.clearRuntimeData();
    }

    @Test
    public void testPaths() {
        assertNotNull(TestEnvironment.ABS_RUNTIME_DATA_PATH);
        assertNotNull(TestEnvironment.ABS_TEST_DATA_PATH);
    }

    @Test
    public void testCreateOrClearRuntimeData() throws Exception {
        File dir = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH);
        if(dir.exists()) {
            TestEnvironment.deleteDirectory(dir);
        }
        assertFalse(dir.exists());

        TestEnvironment.createOrClearRuntimeData();
        assertTrue(dir.exists());

        File file = new File(dir, "testfile");
        FileUtils.writeStringToFile(file, "filedata", "UTF-8");
        assertTrue(file.exists());

        TestEnvironment.createOrClearRuntimeData();
        assertTrue(dir.exists());
        assertFalse(file.exists());
    }

    @Test
    public void testClearRuntimeData() throws Exception {
        TestEnvironment.createOrClearRuntimeData();

        File dir = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH);
        assertTrue(dir.exists());

        File file = new File(dir, "testfile");
        FileUtils.writeStringToFile(file, "filedata", "UTF-8");
        assertTrue(file.exists());

        TestEnvironment.clearRuntimeData();
        assertFalse(file.exists());
        assertFalse(dir.exists());
    }

    @Test
    public void testCreateTestDirectory() throws Exception {
        File directory = TestEnvironment.createTestDirectory(TestEnvironment.class);
        assertNotNull(directory);
        assertTrue(directory.exists());
        assertTrue(directory.isDirectory());

        // create a file in place of the directory
        assertTrue(directory.delete());
        FileUtils.writeStringToFile(directory, "somedata", "UTF-8");

        try {
            TestEnvironment.createTestDirectory(TestEnvironment.class);
            fail("Should catch exception here");
        } catch (IOException e) {
            TestHelpers.assertContains(e, directory.toString());
        }
    }

    @Test
    public void testGetTestDirectory() {
        File directory = TestEnvironment.getTestDirectory(TestEnvironment.class);
        assertNotNull(directory);
        assertFalse(directory.exists());
    }

    @Test
    public void testCreateTempFile() throws Exception {
        TestEnvironment.createOrClearRuntimeData();

        File file = TestEnvironment.createTempFile("some", "post");
        assertNotNull(file);
        assertTrue(file.exists());
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        File directory = TestEnvironment.createTestDirectory(TestEnvironment.class);
        assertNotNull(directory);
        assertTrue(directory.exists());
        assertTrue(directory.isDirectory());

        TestEnvironment.deleteDirectory(directory);
    }
}