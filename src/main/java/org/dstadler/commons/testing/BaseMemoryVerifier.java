package org.dstadler.commons.testing;

import org.junit.jupiter.api.AfterEach;

/**
 * Base class for tests which want to check for memory leaks at the end of execution.
 *
 * Simply add any object that you want to check for leaking via
 *
 *      verifier.addObject(obj);
 */
public abstract class BaseMemoryVerifier {
    protected static final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

    @AfterEach
    public void tearDownBase() {
        verifier.assertGarbageCollected();
    }
}
