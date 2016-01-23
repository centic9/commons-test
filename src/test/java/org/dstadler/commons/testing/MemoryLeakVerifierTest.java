package org.dstadler.commons.testing;

import org.junit.Test;

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

		verifier.addObject(obj);

		try {
			verifier.assertGarbageCollected(3);
		} catch (AssertionError e) {
			TestHelpers.assertContains(e, "Object should not exist");
		}
	}
}
