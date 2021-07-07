package org.dstadler.commons.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.util.ThreadDump;

/**
 * Helper class to test with many threads.
 *
 * Sample usage is as follows:
 *
 * <pre>
  private static final int NUMBER_OF_THREADS = 10;
  private static final int NUMBER_OF_TESTS = 100;

 {@literal @}Test
  public void testMultipleThreads() throws Throwable {
  ThreadTestHelper helper =
      new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

  helper.executeTest(new ThreadTestHelper.TestRunnable() {
 {@literal @}Override
  public void doEnd(int threadNum) throws Exception {
    // do stuff at the end ...
  }

 {@literal @}Override
  public void run(int threadNum, int itNum) throws Exception {
    // do the actual threaded work ...
  }
 });
 }
 </pre>
 */
public class ThreadTestHelper {
	private static final Logger log = LoggerFactory.make();

	private final int threadCount;
	private final int testsPerThread;

	private volatile Throwable exception = null;
	private final int[] executions;

	/**
	 * Initialize the class with the number of tests that should be executed
	 *
	 * @param threadCount
	 *        The number of threads to start running in parallel.
	 * @param testsPerThread
	 *        The number of single test-executions that are done in each
	 *        thread
	 */
	public ThreadTestHelper(int threadCount, int testsPerThread) {
		this.threadCount = threadCount;
		this.testsPerThread = testsPerThread;

		// Initialize array to allow to summarize afterwards
		executions = new int[threadCount];
	}

	public void executeTest(TestRunnable run) throws Throwable {
		log.info("Starting thread test");

		List<Thread> threads = new LinkedList<>();

		// start all threads
		for (int i = 0; i < threadCount; i++) {
			Thread t = startThread(i, run);
			threads.add(t);
		}

		// wait for all threads
		for (int i = 0; i < threadCount; i++) {
			threads.get(i).join();
		}

		// report exceptions if there were any
		if (exception != null) {
			throw new Exception("Caught an exception in one of the threads", exception);
		}

		// make sure the resulting number of executions is correct
		for (int i = 0; i < threadCount; i++) {
			// check if enough items were performed
			assertEquals("Thread " + i
					+ " did not execute all iterations", testsPerThread,
					executions[i]);
		}
	}

	/**
	 * This methods executes the passed {@link Callable}. The number of executions depends
	 * on the given runs number.
	 *
	 * @param <T> The return-type for the {@link Callable} testable.
	 * @param testable test {@link Callable} to execute
	 * @param runs defines how many times the passed {@link Callable} is executed
	 * @return the results of the the execution of the passed {@link Callable}
	 * @throws Throwable if an exception happened during the execution of the {@link Callable}
	 */
	public static <T> List<T> executeTest(final Callable<T> testable, int runs) throws Throwable {
		final CyclicBarrier barrier = new CyclicBarrier(runs);
		ExecutorService executor = Executors.newCachedThreadPool(
				new BasicThreadFactory.Builder()
						.uncaughtExceptionHandler((t, e) -> log.log(Level.SEVERE, "An uncaught exception happened in Thread " + t.getName(), e))
						.namingPattern(ThreadTestHelper.class.getSimpleName() + "-Thread-%d")
						.build());
		try {
			List<Callable<T>> tasks = new ArrayList<>(runs);
			for (int i = 0; i < runs; i++) {
				tasks.add(() -> {
					barrier.await(); // causes more contention
					return testable.call();
				});
			}

			List<Future<T>> futures = executor.invokeAll(tasks);
			List<T> results = new ArrayList<>(futures.size());
			for (Future<T> future : futures) {
				results.add(future.get());
			}
			return results;
		} catch (ExecutionException e) {
			throw e.getCause();
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * This method is executed to start one thread. The thread will execute the
	 * provided runnable a number of times.
	 *
	 * @param threadNum
	 *        The number of this thread
	 * @param run
	 *        The Runnable object that is used to perform the actual test
	 *        operation
	 *
	 * @return The thread that was started.
	 *
	 */
	private Thread startThread(final int threadNum, final TestRunnable run) {
		log.info("Starting thread number: " + threadNum);

		Thread t1 = new Thread(() -> {
			try {
				for (int itNum = 0; itNum < testsPerThread && exception == null; itNum++) {
					// log.info("Executing iteration " + itNum +
					// " in thread" +
					// Thread.currentThread().getName());

					// call the actual test code
					run.run(threadNum, itNum);

					executions[threadNum]++;
				}

				// do end-work here, we don't do this in a finally as we log
				// Exception
				// then anyway
				run.doEnd(threadNum);
			} catch (Throwable e) {
				// log.log(Level.SEVERE, "Caught unexpected Throwable", e);
				exception = e;
			}
		}, "ThreadTestHelper-Thread " + threadNum + ": " + run.getClass().getName());

		t1.start();

		return t1;
	}

	public interface TestRunnable {

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's <code>run</code> method to be called in that separately
		 * executing
		 * thread.
		 *
		 * The general contract of the method <code>run</code> is that it may take any action whatsoever.
		 *
		 * @param threadNum
		 *        The number of the thread executing this run()
		 * @param itNum
		 *        The count of how many times this thread executed the
		 *        method
		 * @throws Exception Thrown on any failure during running the test
		 *
		 * @see java.lang.Thread#run()
		 */
		void run(int threadNum, int itNum) throws Exception;

		/**
		 * Perform any action that should be done at the end.
		 *
		 * This method should throw an Exception if any check fails at this
		 * point.
		 *
		 * @param threadNum
		 *        The number of the thread executing this doEnd()
		 * @throws Exception Thrown on any failure during running the test
		 */
		@SuppressWarnings("RedundantThrows")
		default void doEnd(int threadNum) throws Exception {
			// default empty implementation as this is often not needed
		}
	}

	/**
	 * Wait for all threads with the specified name to finish, i.e. to not appear in the
	 * list of running threads any more.
	 *
	 * @param name The exact name of the Thread to wait for.
	 *
	 * @throws InterruptedException Thrown by joining threads with the given name
	 */
	public static void waitForThreadToFinish(final String name) throws InterruptedException {
		int count = Thread.currentThread().getThreadGroup().activeCount();

		Thread[] threads = new Thread[count];
		Thread.currentThread().getThreadGroup().enumerate(threads);

		for (Thread t : threads) {
			if (t != null && name.equals(t.getName())) {
				t.join();
			}
		}
	}

	/**
	 * Wait for threads whose name contains the specified string to finish, i.e. to not appear in the
	 * list of running threads any more.
	 *
	 * @param contains The string which is matched against thread-names via thread.getName().contains(name)
	 *
	 * @throws InterruptedException Thrown by joining threads with the given name
	 */
	public static void waitForThreadToFinishSubstring(final String contains) throws InterruptedException {
		waitForThreadToFinishSubstring(contains, 0);
	}

	/**
	 * Wait some time for threads whose name contains the specified string to finish, i.e. to not appear in the
	 * list of running threads any more.
	 *
	 * @param contains The string which is matched against thread-names via thread.getName().contains(name)
	 * @param timeout The number of milliseconds to wait for the thread to finish
	 *
	 * @throws InterruptedException Thrown by joining threads with the given name
	 */
	public static void waitForThreadToFinishSubstring(final String contains, final long timeout) throws InterruptedException {
		int count = Thread.currentThread().getThreadGroup().activeCount();

		Thread[] threads = new Thread[count];
		Thread.currentThread().getThreadGroup().enumerate(threads);

		for (Thread t : threads) {
			if (t != null && t.getName().contains(contains) && !t.getName().startsWith("SUITE-")) {
				t.join(timeout);
			}
		}
	}

	/**
	 * Check and fail if a thread which contains the given string is currently running.
	 *
	 * This is usually combined with waitForThreadToFinishSubstring(contains, timeout);
	 *
	 * @param error The error message to include in the failure.
	 * @param contains The string to check for in thread-names.
	 */
	public static void assertNoThreadLeft(final String error, final String contains) {
		int count = Thread.currentThread().getThreadGroup().activeCount();

		Thread[] threads = new Thread[count];
		Thread.currentThread().getThreadGroup().enumerate(threads);

		for (Thread t : threads) {
			if (t != null && t.getName().contains(contains) && !t.getName().startsWith("SUITE-")) {
				log.info("ThreadDump: " + new ThreadDump(true, true));
				fail(error + t);
			}
		}
	}
}
