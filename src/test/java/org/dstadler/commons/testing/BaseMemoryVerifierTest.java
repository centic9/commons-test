package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class BaseMemoryVerifierTest {
    @Test
    public void testBase() {
        BaseMemoryVerifier verifier = new BaseMemoryVerifier() {
        };

        // should not cause an error if no allocation was registered
        verifier.tearDownBase();
    }

    @Test
    public void testFailure() {
        FailingBaseMemoryVerifier verifier = new FailingBaseMemoryVerifier();

        // an object that is still registered when the tearDown runs
        Object obj = new ArrayList<>();
        verifier.register(obj);

        // should not cause an error if no allocation was registered
		assertThrows(AssertionError.class,
				verifier::tearDownBase);
    }

    private static class FailingBaseMemoryVerifier extends BaseMemoryVerifier {
        public void register(Object obj) {
            // not necessary here as we only test the functionality
            verifier.setHeapDump(false);

            verifier.addObject(obj);
        }
    }
}
