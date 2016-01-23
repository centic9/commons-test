package org.dstadler.commons.testing;

import org.junit.Test;

public class MemoryLeakVerifierTest {
	@Test
	public void testNoMemoryLeak() {
		Object obj = new Object();

		MemoryLeakVerifier verifier = new MemoryLeakVerifier(obj);

		// unset local reference
		obj = null;

		verifier.assertGarbageCollected("obj");
	}

	@Test
	public void testWithMemoryLeak() {
		Object obj = new Object();

		MemoryLeakVerifier verifier = new MemoryLeakVerifier(obj);

		// keep local reference
		//obj = null;

		try {
			verifier.assertGarbageCollected("obj", 3);
		} catch (AssertionError e) {
			TestHelpers.assertContains(e, "obj: object should not exist");
		}
	}
}
