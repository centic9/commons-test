package org.dstadler.commons.testing;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

		try {
			verifier.assertGarbageCollected(3);
			fail("Should report a memory leak here");
		} catch (AssertionError e) {
			TestHelpers.assertContains(e, "Object should not exist");
		}
	}

	@Test
	public void testWithMemoryLeakAndHeapDump() {
		Object obj = new Object();

		MemoryLeakVerifier verifier = new MemoryLeakVerifier();
		verifier.setHeapDump(true);

		verifier.addObject(obj);

		try {
			verifier.assertGarbageCollected(3);
			fail("Should report a memory leak here");
		} catch (AssertionError e) {
			TestHelpers.assertContains(e, "Object should not exist");

			final File heapDumpFile = new File(MemoryLeakVerifier.HEAP_DUMP_FILE_NAME);

			assertTrue("HeapDumpFile was not found at " + heapDumpFile.getAbsolutePath(), heapDumpFile.exists());
			assertTrue("HeapDumpFile at " + heapDumpFile.getAbsolutePath() + " could not be deleted", heapDumpFile.delete());
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
