package org.dstadler.commons.testing;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple utility class that can verify that objects have been successfully garbage collected.
 *
 * Usage is something like
 *
 * 	private final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

	{@literal}After
	public void tearDown() {
		verifier.assertGarbageCollected();
	}

	{@literal}Test
	public void someTest() {
		...
		verifier.addObject(object);
	}

 *
 * This will verify at the end of the test if the object is actually removed by the
 * garbage collector or if it lingers in memory for some reason.
 *
 * By default, a heap dump will be written to the file 'MemoryLeakVerifier.hprof' in the
 * current directory. This can be disable via setHeapDump(false)
 *
 * Idea taken from http://stackoverflow.com/a/7410460/411846
 */
public class MemoryLeakVerifier {
	private static final int MAX_GC_ITERATIONS = 50;
	private static final int GC_SLEEP_TIME     = 100;

	protected static final String HEAP_DUMP_FILE_NAME = "MemoryLeakVerifier.hprof";

	private final List<WeakReference<Object>> references = new ArrayList<>();
	private boolean dumpHeap = true;

	public MemoryLeakVerifier() {
	}

	public void setHeapDump(boolean dumpHeap) {
		this.dumpHeap = dumpHeap;
	}

	public void addObject(Object object) {
		references.add(new WeakReference<>(object));
	}

	/**
	 * Attempts to perform a full garbage collection so that all weak references will be removed. Usually only
	 * a single GC is required, but there have been situations where some unused memory is not cleared up on the
	 * first pass. This method performs a full garbage collection and then validates that the weak reference
	 * now has been cleared. If it hasn't then the thread will sleep for 100 milliseconds and then retry up to
	 * 50 more times. If after this the object still has not been collected then the assertion will fail.
	 *
	 * Based upon the method described in: http://www.javaworld.com/javaworld/javatips/jw-javatip130.html
	 */
	public void assertGarbageCollected() {
		assertGarbageCollected(MAX_GC_ITERATIONS);
	}

	/**
	 * Used only for testing the class itself where we would like to fail faster than 5 seconds
	 * @param maxIterations The number of times a GC will be invoked until a possible memory leak is reported
	 */
	void assertGarbageCollected(int maxIterations) {
		try {
			for(WeakReference<Object> ref : references) {
				assertGarbageCollected(ref, maxIterations, dumpHeap);
			}
		} catch (InterruptedException e) {
			// just ensure that we quickly return when the thread is interrupted
		}
	}

	private static void assertGarbageCollected(WeakReference<Object> ref, int maxIterations, boolean dumpHeap) throws InterruptedException {
		// exit early if the ref is already collected from before
		if(ref.get() == null) {
			return;
		}

	    Runtime runtime = Runtime.getRuntime();
	    for (int i = 0; i < maxIterations; i++) {
	        runtime.runFinalization();
	        runtime.gc();
	        if (ref.get() == null) {
				return;
			}

	        // Pause for a while and then go back around the loop to try again...
			//EventQueue.invokeAndWait(Procedure.NoOp); // Wait for the AWT event queue to have completed processing
			Thread.sleep(GC_SLEEP_TIME);
	    }

		if(dumpHeap && ref.get() != null) {
			// dumping heap
			try {
				HeapDump.dumpHeap(HEAP_DUMP_FILE_NAME, true);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

	    assertNull("Object should not exist after " + MAX_GC_ITERATIONS + " collections, but still had: " + ref.get() + (dumpHeap ? ", a heapdump was written to " + HEAP_DUMP_FILE_NAME : ""),
	    		ref.get());
	}
}
