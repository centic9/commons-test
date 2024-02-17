package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class PrivateConstructorCoverageTest {

	@Test
	public void testExecutePrivateConstructor() throws Exception {
		// run this on itself to cover it!
		assertNotNull(PrivateConstructorCoverage.executePrivateConstructor(PrivateConstructorCoverage.class));

		// run it with an abstract class to check for exception
		try {
			PrivateConstructorCoverage.executePrivateConstructor(MyAbstract.class);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Cannot run the private constructor for abstract classes");
		}
	}

	private abstract static class MyAbstract {

	}
}
