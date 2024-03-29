package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.Proxy.Type;
import java.util.logging.Level;

import org.dstadler.commons.http.NanoHTTPD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHelpersTest {

    @Test
    public void testEqualsTest() {
        //noinspection StringOperationCanBeSimplified
        TestHelpers.EqualsTest(new String("str"), "str", "str2");
    }

    @Test
    public void testCompareToTest() {
        TestHelpers.CompareToTest(new MyString("str"), new MyString("str"), new MyString("str2"), false);
        TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str2"), true);
        TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str4"), false);
	}

	@Test
	public void testCompareToTestFailures() {
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(null, new MyString("str2"), new MyString("str4"), false),
				"obj and equ not equal");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(new MyString("str3"), null, new MyString("str4"), false),
				"obj and equ not equal");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), null, false),
				"obj and equ not equal");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(new MyString("str3"), new MyString("str2"), new MyString("str4"), false),
				"obj and equ not equal");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str3"), false),
				"obj and notEqu are equal");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str4"), true),
				"notEqu is not less");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest("str3", "str3", "str4", false),
				"obj and equ are the same object");
		assertThrows(AssertionError.class,
				() -> TestHelpers.CompareToTest("str3", "str2", "str3", false),
				"obj and equ are the same object");
    }

    @Test
    public void testComparatorTest() {
        //noinspection StringOperationCanBeSimplified
        TestHelpers.ComparatorTest((o1, o2) -> {
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
		}, new String("str"), "str", "str2", false);

        //noinspection StringOperationCanBeSimplified
        TestHelpers.ComparatorTest((o1, o2) -> {
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
		}, new String("str3"), "str3", "str2", true);
    }

    @Test
    public void testToStringTest() {
        TestHelpers.ToStringTest("str1");
        TestHelpers.ToStringTest("abc");
    }

    @Test
    public void testCloneTest() throws Exception {
        TestHelpers.CloneTest(new CloneableImplementation());
    }

    @Test
    public void testHashCodeTest() {
        //noinspection StringOperationCanBeSimplified
        TestHelpers.HashCodeTest(new String("str"), "str");
        //noinspection StringOperationCanBeSimplified
        TestHelpers.HashCodeTest(new String("other"), "other");
    }

    @Test
    public void testEnumTest() throws Exception {
        TestHelpers.EnumTest(Type.DIRECT, Type.class, "DIRECT");
        TestHelpers.EnumTest(Type.HTTP, Type.class, "HTTP");
    }

    @Test
    public void testAssertContainsThrowableStringArray() {
        TestHelpers.assertContains(new Throwable("some"), "some");
        TestHelpers.assertContains(new Throwable("other"), "other");
    }

    @Test
    public void testAssertContainsStringThrowableStringArray() {
        TestHelpers.assertContains("message", new Throwable("some"), "some");
        TestHelpers.assertContains("other message", new Throwable("some"), "some");
    }

    @Test
    public void testAssertContainsStringStringArray() {
        TestHelpers.assertContains("some", "some");
        TestHelpers.assertContains("other", "other");
    }

    @Test
    public void testAssertContainsMsg() {
        TestHelpers.assertContainsMsg("message", "some", "some");
        TestHelpers.assertContainsMsg("other message", "other", "other");
    }

    @Test
    public void testAssertNotContains() {
        TestHelpers.assertNotContains("some", "some1");
        TestHelpers.assertNotContains("other", "other1");
    }

    @Test
    public void testAssertNotContainsMsg() {
        TestHelpers.assertNotContainsMsg("message", "some", "message");
        TestHelpers.assertNotContainsMsg("other message", "other", "some other message");
    }

    @Test
    public void testRunTestWithDifferentLogLevel() {
        TestHelpers.runTestWithDifferentLogLevel(this::testAssertNotContains, TestHelpers.class.getName(), Level.WARNING);
    }

    @Test
    public void testAssertURLWorks() throws IOException {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "msg")) {
            TestHelpers.assertURLWorks("http://localhost:" + server.getPort(), 1000);
        }
    }

    @Test
    public void testAssumeCanShowDialogs() {
        // just call the method, it may ignore the test-method
        TestHelpers.assumeCanShowDialogs();
    }

    public static final class CloneableImplementation implements Cloneable {
        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Object clone() {
            return new CloneableImplementation();
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
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

        MyString(String str) {
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

        @SuppressWarnings("RedundantIfStatement")
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

    @Test
    public void testCreateTempDirectoryWithSuffix() throws IOException {
        File dir = TestHelpers.createTempDirectory("abc", ".test");
        Assertions.assertNotNull(dir);
        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(dir.isDirectory());

        Assertions.assertTrue(dir.delete());
        Assertions.assertTrue(dir.getName().endsWith(".test"));
        Assertions.assertTrue(dir.getName().startsWith("abc"));
    }

    @Test
    public void testCreateTempDirectoryNoSuffix() throws IOException {
        File dir = TestHelpers.createTempDirectory("abc", "");
        Assertions.assertNotNull(dir);
        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(dir.isDirectory());

        Assertions.assertTrue(dir.delete());
        Assertions.assertTrue(dir.getName().startsWith("abc"));
    }

    @Test
    public void testCreateTempDirectoryNullSuffix() throws IOException {
        File dir = TestHelpers.createTempDirectory("def", null);
        Assertions.assertNotNull(dir);
        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(dir.isDirectory());

        Assertions.assertTrue(dir.delete());
        Assertions.assertTrue(dir.getName().endsWith(".tmp"));
        Assertions.assertTrue(dir.getName().startsWith("def"));
    }
}
