package org.dstadler.commons.testing;

import java.io.File;
import java.io.IOException;
import java.net.Proxy.Type;
import java.util.Comparator;
import java.util.logging.Level;

import org.dstadler.commons.http.NanoHTTPD;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Convert2Lambda")        // should still compile/run with Java 7
public class TestHelpersTest {

    @Test
    public void testEqualsTest() {
        //noinspection RedundantStringConstructorCall
        TestHelpers.EqualsTest(new String("str"), "str", "str2");
    }

    @Test
    public void testCompareToTest() {
        TestHelpers.CompareToTest(new MyString("str"), new MyString("str"), new MyString("str2"), false);
        TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str2"), true);
        TestHelpers.CompareToTest(new MyString("str3"), new MyString("str3"), new MyString("str4"), false);
    }

    @Test
    public void testComparatorTest() {
        //noinspection RedundantStringConstructorCall
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

        //noinspection RedundantStringConstructorCall
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
        TestHelpers.ToStringTest("abc");
    }

    @Test
    public void testCloneTest() throws Exception {
        TestHelpers.CloneTest(new CloneableImplementation());
    }

    @Test
    public void testHashCodeTest() {
        //noinspection RedundantStringConstructorCall
        TestHelpers.HashCodeTest(new String("str"), "str");
        //noinspection RedundantStringConstructorCall
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

    public final class CloneableImplementation implements Cloneable {
        @Override
        public Object clone() throws CloneNotSupportedException {
            return new CloneableImplementation();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        assertTrue(dir.delete());
        assertTrue(dir.getName().endsWith(".test"));
        assertTrue(dir.getName().startsWith("abc"));
    }

    @Test
    public void testCreateTempDirectoryNoSuffix() throws IOException {
        File dir = TestHelpers.createTempDirectory("abc", "");
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        assertTrue(dir.delete());
        assertTrue(dir.getName().startsWith("abc"));
    }

    @Test
    public void testCreateTempDirectoryNullSuffix() throws IOException {
        File dir = TestHelpers.createTempDirectory("def", null);
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        assertTrue(dir.delete());
        assertTrue(dir.getName().endsWith(".tmp"));
        assertTrue(dir.getName().startsWith("def"));
    }
}
