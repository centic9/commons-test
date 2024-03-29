package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.File;

public class MemoryLeakVerifierTest {
	@Test
	public void testNoMemoryLeak() {
		MemoryLeakVerifier verifier = new MemoryLeakVerifier();
		verifier.addObject(new Object());
		verifier.addObject(new Object());

		verifier.assertGarbageCollected();
	}

	@Test
	public void testWithMemoryLeak() {
		Object obj = new Object();

		MemoryLeakVerifier verifier = new MemoryLeakVerifier();
		verifier.setHeapDump(false);

		verifier.addObject(obj);

		TestHelpers.assertContains(
				assertThrows(AssertionError.class,
						() -> verifier.assertGarbageCollected(3)),
				"Object should not exist");
	}

	@Test
	public void testWithMemoryLeakAndHeapDump() {
		final File heapDumpFile = new File(MemoryLeakVerifier.HEAP_DUMP_FILE_NAME);
		assertTrue(!heapDumpFile.exists() || heapDumpFile.delete());

		Object obj = new Object();

		MemoryLeakVerifier verifier = new MemoryLeakVerifier();
		verifier.setHeapDump(true);

		verifier.addObject(obj);

		expectMemoryLeaks(heapDumpFile, verifier);

		// in some other tests we got an IllegalStateException because writing
		// the heap-dump a 2nd time failed because the file already exists, but here
		// we seem to be able to overwrite it
		expectMemoryLeaks(heapDumpFile, verifier);
	}

	private void expectMemoryLeaks(File heapDumpFile, MemoryLeakVerifier verifier) {
		try {
			verifier.assertGarbageCollected(3);
			fail("Should report a memory leak here");
		} catch (AssertionError e) {
			TestHelpers.assertContains(e, "Object should not exist");

			assertTrue(heapDumpFile.exists(), "HeapDumpFile was not found at " + heapDumpFile.getAbsolutePath());
			assertTrue(heapDumpFile.delete(),
					"HeapDumpFile at " + heapDumpFile.getAbsolutePath() + " could not be deleted");
		}
	}

	@Test
	public void testNoMemoryLeakManyObjects() {
		MemoryLeakVerifier verifier = new MemoryLeakVerifier();
		for(int i = 0;i < 5000;i++) {
			verifier.addObject(new Object());
		}

		verifier.assertGarbageCollected();
	}
}
