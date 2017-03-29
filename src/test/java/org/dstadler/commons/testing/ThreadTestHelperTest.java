package org.dstadler.commons.testing;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

@SuppressWarnings("Convert2Lambda")		// should still compile with Java 7
public class ThreadTestHelperTest {

	@Test
	public void testExecuteTestTestRunnable() throws Throwable {
        ThreadTestHelper helper =
            new ThreadTestHelper(2, 2);

        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger endCount = new AtomicInteger();
        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadNum) throws Exception {
                endCount.incrementAndGet();
            }

            @Override
            public void run(int threadNum, int iter) throws Exception {
                count.incrementAndGet();
            }
        });

        assertEquals(2, endCount.get());
        assertEquals(4, count.get());
	}

	@Test
	public void testWaitForThreadToFinish() throws Exception {
		ThreadTestHelper.waitForThreadToFinish("some non-existing name");
		ThreadTestHelper.waitForThreadToFinish("another");
	}

	@Test
	public void testWaitForThreadToFinishSubstring() throws Exception {
		ThreadTestHelper.waitForThreadToFinishSubstring("some non-existing name");
	}

	@Test
	public void testExecuteTestTestCallable() throws Throwable {
        List<String> list = ThreadTestHelper.executeTest(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return "str";
			}
		}, 23);

        assertEquals(23, list.size());

        list = ThreadTestHelper.executeTest(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return "str";
			}
		}, 1);

        assertEquals(1, list.size());
	}
}
