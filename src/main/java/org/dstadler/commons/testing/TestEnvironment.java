package org.dstadler.commons.testing;

import org.apache.commons.io.FileUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test environment utilities, e.g. runtime data directory (also known as temporary directory).
 */
public class TestEnvironment {
	private final static Logger log = LoggerFactory.make();

    /**
     * Directory for input/configuration data required by tests.
     */
    public static final String TEST_DATA_PATH = "testsrc/data";
    public static final String ABS_TEST_DATA_PATH = new File(TEST_DATA_PATH).getAbsolutePath();

    /**
     * Directory for runtime data temporarily required by tests (aka temp directory).
     */
    public static final String RUNTIME_DATA_PATH = "build/testdata/" + UUID.randomUUID().toString();
    public static final String ABS_RUNTIME_DATA_PATH =
            new File(RUNTIME_DATA_PATH).getAbsolutePath();

    /**
     * Create (not yet existing) or clear (yet existing) runtime data directory.
     *
     * @throws IOException
     */
    public static void createOrClearRuntimeData() throws IOException {
        File dir = new File(ABS_RUNTIME_DATA_PATH);
        if (dir.exists()) {
            clearRuntimeData();
        }
        if(!dir.mkdirs()) {
        	// this sometimes happens, but dir.exists() returns false, let's retry after a bit of sleeping
        	log.info("Waiting for 3 seconds to see if we can then create directory " + dir + ": " + dir.exists() + ": " + dir.isDirectory() + ": " + dir.isFile() + ": " + dir.lastModified());
        	try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}

            if(!dir.mkdirs()) {
            	throw new IOException("Could not create directory " + dir + ": " + dir.exists() + ": " + dir.isDirectory() + ": " + dir.isFile() + ": " + dir.lastModified());
            }
        }
    }

    /**
     * Clear runtime data directory.
     *
     * Must be called in a test case's teardown() method if the test case makes use of the runtime
     * data directory.
     *
     * @throws IOException
     */
    public static void clearRuntimeData() throws IOException {
        // Little sanity check:
        assertTrue("Had: " + ABS_RUNTIME_DATA_PATH,
        		ABS_RUNTIME_DATA_PATH.endsWith(RUNTIME_DATA_PATH.replace("/", File.separator).replace("\\", File.separator)));
        assertTrue("Had: " + ABS_RUNTIME_DATA_PATH,
        		ABS_RUNTIME_DATA_PATH.contains(File.separator + "build" + File.separator));

        File directory = new File(ABS_RUNTIME_DATA_PATH);
		deleteDirectory(directory);

        assertFalse(directory.exists());
    }

    /**
     * <em>Create</em> a new temporary directory for the given test class.
     *
     * @param testClass the test class to create the temporary directory for
     * @return the temporary directory for testing
     */
    public static File createTestDirectory(Class<?> testClass) throws IOException {
        File directory = getTestDirectory(testClass);
        if(!directory.mkdirs()) {
            throw new IOException("Could not create directory " + directory);
        }
        return directory;
    }

    /**
     * Get a new temporary directory for the given test class.
     *
     * @param testClass the test class to get the temporary directory for
     * @return the temporary directory for testing
     */
    public static File getTestDirectory(Class<?> testClass) {
        return new File(TestEnvironment.ABS_RUNTIME_DATA_PATH, testClass.getCanonicalName());
    }

	/**
	 * Calls File.createTempFile(), but ensures that the resulting file is located in the
	 * local temporary directory for the tests which ensures proper cleanup after test runs.
	 *
	 * @see {@link File#createTempFile(String, String)}
	 */
	public static File createTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix, new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
	}

	/**
	 * Safely delete the given directory recursively with a possible retry
	 * to work around issues on some Windows systems.
	 *
	 * @param directory The directory to remove
	 * @throws IOException
     */
	public static void deleteDirectory(File directory) throws IOException {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			// Somehow sometimes the directories are not yet deletable as windows still has locks on them
			// try once more after a short sleep
			log.log(Level.WARNING, "Could not delete directory " + directory + ", trying once more after some time: " + e);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e2) {
				throw new IllegalStateException(e2);
			}

			try {
				FileUtils.deleteDirectory(directory);
			} catch (@SuppressWarnings("unused") IOException e1) {
				String[] list = directory.list();
				StringBuilder builder = new StringBuilder("Had leftover files/directories: ").append(Arrays.toString(list)).append("\n");
				for(String file : list) {
					File subFile = new File(directory, file);
					if(subFile.isDirectory()) {
						builder.append("Had leftover sub-files/directories: ").append(Arrays.toString(subFile.list())).append("\n");
					}
				}
				throw new IOException(builder.toString(), e);
			}
		}
	}
}
