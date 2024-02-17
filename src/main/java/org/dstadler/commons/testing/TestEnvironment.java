package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.FileUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static final String RUNTIME_DATA_PATH = "build/testdata/" + UUID.randomUUID();
    public static final String ABS_RUNTIME_DATA_PATH =
            new File(RUNTIME_DATA_PATH).getAbsolutePath();

    /**
     * Create (not yet existing) or clear (yet existing) runtime data directory.
     *
     * @throws IOException If removing files and directories fails or the directory
     * 		cannot be created.
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
     * @throws IOException If removing files and directories fails.
     */
    public static void clearRuntimeData() throws IOException {
        // Little sanity check:
        assertTrue(
				ABS_RUNTIME_DATA_PATH.endsWith(RUNTIME_DATA_PATH.replace("/", File.separator).replace("\\", File.separator)),
				"Had: " + ABS_RUNTIME_DATA_PATH);
        assertTrue(ABS_RUNTIME_DATA_PATH.contains(File.separator + "build" + File.separator),
				"Had: " + ABS_RUNTIME_DATA_PATH);

        File directory = new File(ABS_RUNTIME_DATA_PATH);
		deleteDirectory(directory);

        assertFalse(directory.exists());
    }

    /**
     * <em>Create</em> a new temporary directory for the given test class.
     *
     * @param testClass the test class to create the temporary directory for
     * @return the temporary directory for testing
     * @throws IOException If the directory cannot be created.
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
	 * See {@link File#createTempFile(String, String)}
	 *
     * @param  prefix     The prefix string to be used in generating the file's
     *                    name; must be at least three characters long
     *
     * @param  suffix     The suffix string to be used in generating the file's
     *                    name; may be <code>null</code>, in which case the
     *                    suffix <code>".tmp"</code> will be used
     *
     * @return  An abstract pathname denoting a newly-created empty file
     *
     * @throws  IllegalArgumentException
     *          If the <code>prefix</code> argument contains fewer than three
     *          characters
     *
     * @throws  IOException  If a file could not be created
     *
     * @throws  SecurityException
     *          If a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          method does not allow a file to be created
	 */
	public static File createTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix, new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
	}

	/**
	 * Safely delete the given directory recursively with a possible retry
	 * to work around issues on some Windows systems.
	 *
	 * @param directory The directory to remove
     * @throws IOException If removing files and directories fails.
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
			} catch (IOException e1) {
				String[] list = directory.list();
				Objects.requireNonNull(list, "Directory " + directory + " not found or not accessible");
				StringBuilder builder = new StringBuilder("Had leftover files/directories: ").append(Arrays.toString(list)).append("\n");
				for(String file : list) {
					File subFile = new File(directory, file);
					if(subFile.isDirectory()) {
						builder.append("Had leftover sub-files/directories: ").append(Arrays.toString(subFile.list())).append("\n");
					}
				}
				throw new IOException(builder.toString(), e1);
			}
		}
	}
}
