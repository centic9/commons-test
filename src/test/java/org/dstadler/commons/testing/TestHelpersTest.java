package org.dstadler.commons.testing;

import java.io.IOException;
import java.net.Proxy.Type;
import java.util.Comparator;
import java.util.logging.Level;

import org.dstadler.commons.http.NanoHTTPD;
import org.junit.Test;

public class TestHelpersTest {

	@Test
	public void testEqualsTest() {
		TestHelpers.EqualsTest(new String("str"), "str", "str2");
	}

	@Test
	public void testCompareToTest() {
		TestHelpers.CompareToTest(new MyString("str"), new MyString("str"), new MyString("str2"), false);
		TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str2"), true);
	}

	@Test
	public void testComparatorTest() {
		TestHelpers.ComparatorTest(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if(o1 == null && o2 == null) {
					return 0;
				}
				if(o1 == null) {
					return -1;
				}
				if(o2 == null) {
					return 1;
				}

				return o1.compareTo(o2);
			}

		}, new String("str"), "str", "str2", false);

		TestHelpers.ComparatorTest(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if(o1 == null && o2 == null) {
					return 0;
				}
				if(o1 == null) {
					return -1;
				}
				if(o2 == null) {
					return 1;
				}

				return o1.compareTo(o2);
			}

		}, new String("str3"), "str3", "str2", true);
	}

	@Test
	public void testToStringTest() {
		TestHelpers.ToStringTest("str1");
	}

	@Test
	public void testCloneTest() throws Exception {
		TestHelpers.CloneTest(new CloneableImplementation());
	}

	@Test
	public void testHashCodeTest() {
		TestHelpers.HashCodeTest(new String("str"), "str");
	}

	@Test
	public void testEnumTest() throws Exception {
		TestHelpers.EnumTest(Type.DIRECT, Type.class, "DIRECT");
	}

	@Test
	public void testAssertContainsThrowableStringArray() {
		TestHelpers.assertContains(new Throwable("some"), "some");
	}

	@Test
	public void testAssertContainsStringThrowableStringArray() {
		TestHelpers.assertContains("message", new Throwable("some"), "some");
	}

	@Test
	public void testAssertContainsStringStringArray() {
		TestHelpers.assertContains("some", "some");
	}

	@Test
	public void testAssertContainsMsg() {
		TestHelpers.assertContainsMsg("message", "some", "some");
	}

	@Test
	public void testAssertNotContains() {
		TestHelpers.assertNotContains("some", "some1");
	}

	@Test
	public void testAssertNotContainsMsg() {
		TestHelpers.assertNotContainsMsg("message", "some", "message");
	}

	@Test
	public void testRunTestWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				testAssertNotContains();
			}
		}, TestHelpers.class.getName(), Level.WARNING);
	}

	@Test
	public void testAssertURLWorks() throws IOException {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "msg")) {
			TestHelpers.assertURLWorks("http://localhost:" + server.getPort(), 1000);
		}
	}

	public final class CloneableImplementation implements Cloneable {
		@Override
		public Object clone() throws CloneNotSupportedException {
			return new CloneableImplementation();
		}

		@Override
		public boolean equals(Object obj) {
			return true;
		}

		@Override
		public int hashCode() {
			return 1;
		}
	}

	private final class MyString implements Comparable<MyString> {
		private final String str;

		public MyString(String str) {
			super();
			this.str = str;
		}

		@Override
		public int compareTo(MyString o) {
			if(o == null || o.str == null) {
				return 1;
			}

			return str.compareTo(o.str);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((str == null) ? 0 : str.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyString other = (MyString) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}

		private TestHelpersTest getOuterType() {
			return TestHelpersTest.this;
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(TestHelpers.class);
	}
}
