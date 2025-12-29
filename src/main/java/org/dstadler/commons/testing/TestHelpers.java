package org.dstadler.commons.testing;

import static java.lang.Integer.signum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Assumptions;

/**
 * Copied from project test.util to use it in independent projects from here.
 *
 * @author dominik.stadler
 *
 */
@SuppressWarnings("EqualsWithItself")
public class TestHelpers {
	/**
	 * Verify that the provided object implements the basic requirements of an "equals()" method correctly, i.e. equal
	 * objects are equal, non-equal objects are not equal.
	 *
	 * Additionally some additional checks are performed, e.g.: - Reflexive: Objects should be equal to themselves -
	 * Symmetric: Equal objects should be equal in both directions - Transitive: Equal objects should have the same
	 * hashCode() - Handle null in equals gracefully - Handle foreign object types in equals gracefully
	 *
	 *
	 * @param obj
	 *            An object of the type to test, should override equals()
	 * @param equal
	 *            An object of the type, verified to be equal to "obj"
	 * @param notequal
	 *            An object of the type which should be different from "obj" and "equal"
	 */
	@SuppressWarnings({"EqualsWithItself", "ObjectEqualsNull", "SimplifiableJUnitAssertion", "ConstantConditions"})
	public static void EqualsTest(final Object obj, final Object equal, final Object notequal) {
		// none of the three should be null
		assertNotNull(obj, "Object in EqualsTest should not be null!");
		assertNotNull(equal, "Equals-object in EqualsTest should not be null!");
		assertNotNull(notequal, "Non-equal-object in EqualsTest should not be null!");

		// make sure different objects are passed in
		assertFalse(obj == equal, "Object and equals-object in EqualsTest should not be identical");	// NOPMD
		assertFalse(obj == notequal, "Object and non-equals-object in EqualsTest should not be identical");	// NOPMD

		// make sure correct objects are passed
		assertTrue(obj.getClass().equals(equal.getClass()), "Classes of objects in EqualsTest should be equal!");	// NOPMD
		assertTrue(obj.getClass().equals(	// NOPMD
				notequal.getClass()), "Classes of objects in EqualsTest should be equal!");

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertTrue(obj.equals(equal), "Object and equal-object should be equal in EqualsTest!");	// NOPMD
		assertFalse(obj.equals(notequal), "Object and non-equal-object should not be equal in EqualsTest!");	// NOPMD

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertTrue(obj.equals(obj), "Reflexive: object should be equal to itself in EqualsTest!");	// NOPMD
		assertTrue(equal.equals(equal), "Reflexive: equal-object should be equal to itself in EqualsTest!");	// NOPMD
		assertTrue(notequal	// NOPMD
				.equals(notequal), "Reflexive: non-equal-object should be equal to itself in EqualsTest!");

		// not equals to null
		assertFalse(obj.equals(null), "Object should not be equal to null in EqualsTest!");	// NOPMD - null-equals() is intended here
		assertFalse(equal.equals(null), "Equal-object should not be equal to null in EqualsTest!");	// NOPMD - null-equals() is intended here
		assertFalse(notequal.equals(null), "Non-equal-object should not be equal to null in EqualsTest!");	// NOPMD - null-equals() is intended here

		// not equals to a different type of object
		assertFalse(obj
						.equals("TestString"), "Object should not be equal to an arbitrary string in EqualsTest!");	// NOPMD
		assertFalse("TestString"
						.equals(obj), "Object should not be equal to an arbitrary string in EqualsTest!");	// NOPMD

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertTrue(obj.equals(equal), "Symmetric: Object should be equal to equal-object in EqualsTest");	// NOPMD
		assertTrue(equal.equals(obj), "Symmetric: Equals-object should be equal to object in EqualsTest!");	// NOPMD
		assertFalse(obj	// NOPMD
				.equals(notequal), "Symmetric: Object should NOT be equal to non-equal-object in EqualsTest");
		assertFalse(notequal	// NOPMD
				.equals(obj), "Symmetric: Non-equals-object should NOT be equal to object in EqualsTest!");

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		// hashCode: equal objects should have equal hash code
		assertTrue(obj.hashCode() == equal.hashCode(),
				"Transitive: Equal objects should have equal hash-code in EqualsTest!");
        //noinspection ExpressionComparedToItself
        assertTrue(obj.hashCode() == obj	// NOPMD
				.hashCode(), "Transitive: Equal objects should have equal hash-code in EqualsTest!");
        //noinspection ExpressionComparedToItself
		assertTrue(equal.hashCode() == equal.hashCode(),
				"Transitive: Equal objects should have equal hash-code in EqualsTest!");
        //noinspection ExpressionComparedToItself
		assertTrue(notequal.hashCode() == notequal.hashCode(),
				"Transitive: Equal objects should have equal hash-code in EqualsTest!");
	}

	/**
	 * Helper method to verify some basic assumptions about the compareTo() method.
	 *
	 * This can be used to verify basic assertions on an implementation of Comparable.
	 *
	 * @param <T> The type of object to test compareTo() on
	 * @param obj
	 *            The object to use for compareTo()
	 * @param equal
	 *            An object which is equal to "obj", but not the same object!
	 * @param notequal
	 *            An object which is not equal to "obj"
	 * @param notEqualIsLess True if the notequal object should be less than obj.
	 */
	@SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion"})
	public static <T extends Comparable<T>> void CompareToTest(final T obj, final T equal,
															   final T notequal, boolean notEqualIsLess) {
		// none of the three should be null
		assertNotNull(obj, "Object in CompareToTest should not be null!");
		assertNotNull(equal, "Equals-object in CompareToTest should not be null!");	// NOPMD
		assertNotNull(notequal, "Non-equal-object in CompareToTest should not be null!");	// NOPMD

		// make sure different objects are passed in
		assertFalse(obj == equal, "Object and equals-object in CompareToTest should not be identical");	// NOPMD
		assertFalse(obj == notequal, "Object and non-equals-object in CompareToTest should not be identical");	// NOPMD

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertEquals(0, obj.compareTo(equal), "Object and equal-object should compare in CompareToTest!");
		assertFalse(0 == obj	// NOPMD
				.compareTo(notequal), "Object and non-equal-object should not compare in CompareToTest!");
		assertFalse(0 == equal	// NOPMD
				.compareTo(notequal), "Equal-object and non-equal-object should not compare in CompareToTest!");

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertEquals(0, obj.compareTo(obj), "Reflexive: object should be equal to itself in CompareToTest!");
		assertEquals(0, equal
				.compareTo(equal), "Reflexive: equal-object should be equal to itself in CompareToTest!");
		assertEquals(0, notequal
				.compareTo(notequal), "Reflexive: non-equal-object should be equal to itself in CompareToTest!");

		// not equals to null
		assertTrue(0 != obj.compareTo(null), "Object should not be equal to null in CompareToTest!");
		assertTrue(0 != equal.compareTo(null), "Equal-object should not be equal to null in CompareToTest!");
		assertTrue(0 != notequal.compareTo(null), "Non-equal-object should not be equal to null in CompareToTest!");

		// not equals to a different type of object
		/* cannot happen due to Generics
		assertFalse("Object should not be equal to an arbitrary string in CompareToTest!" , 0 ==
				obj.compareTo("TestString"));*/

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertEquals(0, obj	// NOPMD
				.compareTo(equal), "Symmetric: Object should be equal to equal-object in CompareToTest");
		assertEquals(0, equal	// NOPMD
				.compareTo(obj), "Symmetric: Equals-object should be equal to object in CompareToTest!");
		assertFalse(0 == obj	// NOPMD
				.compareTo(notequal), "Symmetric: Object should NOT be equal to non-equal-object in CompareToTest");
		assertFalse(0 == notequal.compareTo(obj),
				"Symmetric: Non-equals-object should NOT be equal to object in CompareToTest!");
		assertEquals(signum(obj.compareTo(notequal)), (-1)*signum(notequal.compareTo(obj)),
				"Symmetric: Comparing object and non-equal-object in both directions should lead to the same result.");

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		assertEquals(signum(obj.compareTo(notequal)), signum(equal.compareTo(notequal)),
				"Congruence: Comparing object and non-equal-object should have the same result as comparing the equal object and the non-equal-object");

		if(notEqualIsLess) {
			assertTrue(obj.compareTo(notequal) > 0,
					"Item 'notequal' should be less than item 'equal' in CompareToTest, but compare was: " + obj.compareTo(notequal));
		} else {
			assertTrue(obj.compareTo(notequal) < 0,
					"Item 'notequal' should be higher than item 'equal' in CompareToTest, but compare was: " + obj.compareTo(notequal));
		}

		// ensure equals() and hashCode() are implemented as well here
		assertTrue(obj.equals(equal),
				"Findbugs: Comparable objects should implement equals() the same way as compareTo().");
        assertFalse(obj.equals(notequal),
				"Findbugs: Comparable objects should implement equals() the same way as compareTo().");
        EqualsTest(obj, equal, notequal);
        assertEquals(obj.hashCode(), equal.hashCode(),
				"Findbugs: Comparable objects should implement hashCode() the same way as compareTo().");
        HashCodeTest(obj, equal);
	}

	/**
	 * Helper method to verify some basic assumptions about implementations of the Comparator interface.
	 *
	 * This can be used.
	 *
	 * @param <T> The type of Comparator to test
	 * @param comparator
	 *            The implementation of the Comparator.
	 * @param obj
	 *            The object to use.
	 * @param equal
	 *            An object which is equal to "obj", but not the same object!
	 * @param notequal
	 *            An object which is not equal to "obj"
	 * @param notEqualIsLess True if the notequal object should be less than obj.
	 */
	@SuppressWarnings("SimplifiableJUnitAssertion")
	public static <T> void ComparatorTest(final Comparator<T> comparator, final T obj, final T equal,
										  final T notequal, boolean notEqualIsLess) {
		// none of the three should be null
		assertNotNull(obj, "Object in ComparatorTest should not be null!");
		assertNotNull(equal, "Equals-object in ComparatorTest should not be null!");	// NOPMD
		assertNotNull(notequal, "Non-equal-object in ComparatorTest should not be null!");	// NOPMD

		// make sure different objects are passed in
		assertFalse(obj == equal, "Object and equals-object in ComparatorTest should not be identical");	// NOPMD
		assertFalse(obj == notequal, "Object and non-equals-object in ComparatorTest should not be identical");	// NOPMD

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertEquals(0, comparator.compare(obj, equal), "Object and equal-object should compare in ComparatorTest!");
		assertFalse(0 == comparator.compare(obj	// NOPMD
				, notequal), "Object and non-equal-object should not compare in ComparatorTest!");

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertEquals(0, comparator.compare(obj, obj),
				"Reflexive: object should be equal to itself in ComparatorTest!");
		assertEquals(0, comparator.compare(equal
				, equal), "Reflexive: equal-object should be equal to itself in ComparatorTest!");
		assertEquals(0, comparator.compare(notequal
				, notequal), "Reflexive: non-equal-object should be equal to itself in ComparatorTest!");

		// not equals to null, not checked currently as most Comparators expect non-null input at all times
		/*assertTrue("Object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(obj, null));
		assertTrue("Equal-object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(equal, null));
		assertTrue("Non-equal-object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(notequal, null));*/

		// not equals to a different type of object
		/* Cannot happen due to Generics
		assertFalse("Object should not be equal to an arbitrary string in ComparatorTest!" , 0 ==
			obj, "TestString"));
		 */

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertEquals(0, comparator.compare(obj	// NOPMD
				, equal), "Symmetric: Object should be equal to equal-object in ComparatorTest");
		assertEquals(0, comparator.compare(equal	// NOPMD
				, obj), "Symmetric: Equals-object should be equal to object in ComparatorTest!");
		assertFalse(0 == comparator.compare(obj	// NOPMD
				, notequal), "Symmetric: Object should NOT be equal to non-equal-object in ComparatorTest");
		assertFalse(0 == comparator.compare(notequal, obj),
				"Symmetric: Non-equals-object should NOT be equal to object in ComparatorTest!");
		int signumObjEqual = signum(comparator.compare(obj, notequal));
		assertEquals(signumObjEqual, (-1)*signum(comparator.compare(notequal, obj)),
				"Symmetric: Comparing object and non-equal-object in both directions should lead to the same result.");

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		assertEquals(signumObjEqual, signum(comparator.compare(equal, notequal)),
				"Congruence: Comparing object and non-equal-object should have the same result as comparing the equal object and the non-equal-object");

		if(notEqualIsLess) {
			assertTrue(comparator.compare(notequal, obj) < 0,
					"Item 'notequal' should be less than item 'equal' in ComparatorTest, but compare was: " + comparator.compare(notequal, obj));
		} else {
			assertTrue(comparator.compare(notequal, obj) > 0,
					"Item 'notequal' should be higher than item 'equal' in ComparatorTest, but compare was: " + comparator.compare(notequal, obj));
		}

		// additionally test with null
		assertEquals(0, comparator.compare(null, null), "compare(null,null) should have 0 as compare-result");
		assertTrue(comparator.compare(obj, null) != 0, "compare(obj,null) should not have 0 as compare-result");
		assertTrue(comparator.compare(null, obj) != 0, "compare(null,obj) should not have 0 as compare-result");
	}

	/**
	 * Run some general tests on the toString method. This static method is used in tests for classes that overwrite
	 * toString().
	 *
	 * @param obj
	 *            The object to test toString(). This should be an object of a type that overwrites toString()
	 *
	 */
	@SuppressWarnings("SimplifiableJUnitAssertion")
	public static void ToStringTest(final Object obj) {
		// toString should not return null
		assertNotNull(obj.toString(), "A derived toString() should not return null!");

		// toString should not return an empty string
		//noinspection StringEqualsEmptyString
		assertFalse(obj.toString().equals(""), "A derived toString() should not return an empty string!");

		// check that calling it multiple times leads to the same value
		String value = obj.toString();
		for (int i = 0; i < 10; i++) {
			assertEquals(value, obj.toString(),
					"toString() is expected to result in the same result across repeated calls!");
		}
	}

	/**
	 * Run some generic tests on the derived clone-method.
	 *
	 * We need to do this via reflection as the clone()-method in Object is protected and the Cloneable interface does
	 * not include a public "clone()".
	 *
	 * @param obj
	 *            The object to test clone for.
	 * @throws Exception Any exception thrown by invoking clone() on obj via reflection.
	 */
	@SuppressWarnings("SimplifiableJUnitAssertion")
	public static void CloneTest(final Cloneable obj) throws Exception {
		final Method m = obj.getClass().getMethod("clone");
		assertNotNull(m, "Need to find a method called 'clone' in object of type '" + obj.getClass().getName()
				+ "' in CloneTest!");
		// assertTrue("Method 'clone' on object of type '" +
		// obj.getClass().getName() + "' needs to be accessible in
		// CloneTest!",
		// m.isAccessible());

		// clone should return a different object, not the same again
		assertTrue(obj != m.invoke(obj,	// NOPMD
				new Object[] {}), "clone() should not return the object itself in CloneTest!");

		// should return the same type of object
		assertTrue(m	// NOPMD
				.invoke(obj).getClass() == obj.getClass(),
				"clone() should return the same type of object (i.e. the same class) in CloneTest!");

		// cloned objects should be equal to the original object
		assertTrue(m
				.invoke(obj).equals(obj), "clone() should return an object that is equal() to the original object in CloneTest!");
	}

	/**
	 * Checks certain assumption that are made for the hashCode() method
	 *
	 * @param obj
	 *            An Object that override the hasCode() method.
	 * @param equ An Object which should return the same hashCode() as obj.
	 */
	@SuppressWarnings("SimplifiableJUnitAssertion")
	public static void HashCodeTest(final Object obj, final Object equ) {
		assertFalse(obj == equ,
				"HashCodeTest expects two distinct objects with equal hashCode, but the same object is provided twice!");

		// The same object returns the same hashCode always
		final int hash = obj.hashCode();
		assertEquals(hash, obj
						.hashCode(), "hashCode() on object returned different hash after some iterations!");
		assertEquals(hash, obj
						.hashCode(), "hashCode() on object returned different hash after some iterations!");
		assertEquals(hash, obj
						.hashCode(), "hashCode() on object returned different hash after some iterations!");
		assertEquals(hash, obj
						.hashCode(), "hashCode() on object returned different hash after some iterations!");
		assertEquals(hash, obj
						.hashCode(), "hashCode() on object returned different hash after some iterations!");

		// equal objects must have the same hashCode
		// the other way around is not required,
		// different objects can have the same hashCode!!
		assertEquals(obj, equ,
				"Equal Assert failed, but input to HashCodeTest should be two equal objects! Check if the class implements equals() as well to fulfill this contract");
		assertEquals(obj.hashCode(), equ
				.hashCode(), "Equal objects should have equal hashCode() by Java contract!");
	}

	/**
	 * Verifies certain assumptions on an Enum class.
	 *
	 * @param <T> The type of enum to test.
	 * @param enumtype The enum-class to test.
	 * @param enumclass The class-object for the enum-class to test.
	 * @param element A String-element that is expected to match one entry in the enum via Enum.valueOf() calls.
	 *
	 * @throws NoSuchMethodException If the provided class does not have a static method "values"
	 * @throws InvocationTargetException If invoking the static method "values" causes an exception
	 * @throws IllegalAccessException If invoking the static method "values" causes an exception
	 */
	public static <T extends Enum<T>> void EnumTest(Enum<T> enumtype, Class<T> enumclass, String element)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// check valueOf()
		assertEquals(enumtype, Enum.valueOf(enumclass, element));

		// check values()
		Method m = enumclass.getMethod("values", (Class<?>[]) null);
		Object obj = m.invoke(enumtype, (Object[]) null);
		assertNotNull(obj);
		assertInstanceOf(Object[].class, obj);

		// check existing valeOf()
		obj = Enum.valueOf(enumclass, element);
		assertNotNull(obj);
		// Findbugs: useless check: assertTrue(obj instanceof Enum<?>);

		// check non-existing valueOf
		TestHelpers.assertContains(
			assertThrows(IllegalArgumentException.class,
				() -> Enum.valueOf(enumclass, "nonexistingenumelement"),
					"Should catch exception IllegalArgumentException when calling Enum.valueOf() with incorrect enum-value!"),
				"No enum");
	}

	/**
	 * Small helper to verify that a Throwable contains the specified sub-string as part of it's message.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param throwable The Exception to look at.
	 * @param searches A list of one or more search-terms, all of them need to be found in the exception-text.
	 */
	public static void assertContains(final Throwable throwable, final String... searches) {
		assertContains("", throwable, searches);
	}

	/**
	 * Small helper to verify that a Throwable contains the specified sub-string as part of it's message.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param msg A descriptive message which provides more information about a potential assertion failure.
	 * @param throwable The Exception to look at.
	 * @param searches A list of one or more search-terms, all of them need to be found in the exception-text.
	 */
	public static void assertContains(final String msg, final Throwable throwable, final String... searches) {
		assertNotNull(throwable, "Cannot verify message contents of a Throwable when it is null.");
        assertTrue(searches.length > 0, "Specify at least one search-term to be searched in the string");

		String str = throwable.getMessage();
		if(str == null) {
			throw new IllegalArgumentException("Throwable of type " + throwable.getClass() + " contains a null-string as message, cannot assertContains", throwable);
		}
		for(String search : searches) {
			assertTrue(str.contains(search),
					msg + ". Expected to find string '" + search + "', but was not contained in provided string '" + str + "'\n" + ExceptionUtils.getStackTrace(throwable));
		}
	}

	/**
	 * Small helper to verify that a string contains a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str A string of text on which the assertions are applied.
	 * @param searches A list of one or more search-terms, all of them need to be found in the text.
	 */
	public static void assertContains(final String str, final String... searches) {
		assertNotNull(str, "Cannot assertContains on a null-string");
		assertTrue(searches.length > 0, "Specify at least one search-term to be searched in the string");

		for(String search : searches) {
			assertTrue(str.contains(search),
					"Expected to find string '" + search + "', but was not contained in provided string '" + str + "'");
		}
	}

	/**
	 * Small helper to verify that a string contains a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param msg A descriptive message which provides more information about a potential assertion failure.
	 * @param str A string of text on which the assertions are applied.
	 * @param searches A list of one or more search-terms, none of them should be found in the text.
	 */
	public static void assertContainsMsg(final String msg, final String str, final String... searches) {
		assertNotNull(str, "Cannot assertContains on a null-string");
        assertTrue(searches.length > 0, "Specify at least one search-term to be searched in the string");

		for(String search : searches) {
			assertTrue(str.contains(search),
					msg + ". Expected to find string '" + search + "', but was not contained in provided string '" + str + "'");
		}
	}

	/**
	 * Small helper to verify that a string does not contain a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str A string of text on which the assertions are applied.
	 * @param searches A list of one or more search-terms, none of them should be found in the text.
	 */
	public static void assertNotContains(final String str, final String... searches) {
		assertNotNull(str, "Cannot assertNotContains on a null-string");
        assertTrue(searches.length > 0, "Specify at least one search-term to be searched in the string");

		for(String search : searches) {
			assertFalse(str.contains(search),
					"Expected to NOT find '" + search + "' but was contained in string '" + str + "'");
		}
	}


	/**
	 * Small helper to verify that a string does not contain a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param msg A descriptive message which provides more information about a potential assertion failure.
	 * @param str A string of text on which the assertions are applied.
	 * @param searches A list of one or more search-terms, none of them should be found in the text.
	 */
	public static void assertNotContainsMsg(final String msg, final String str, final String... searches) {
		assertNotNull(str, "Cannot assertNotContains on a null-string");
        assertTrue(searches.length > 0, "Specify at least one search-term to be searched in the string");

		for(String search : searches) {
			assertFalse(str.contains(search),
					msg + ". Expected to NOT find '" + search + "' but was contained in string '" + str + "'");
		}
	}

	/**
	 * Allows to run code with different loglevel to cover cases where logging is only done with debug-log-level.
	 *
	 * Ensures that the log-level is reset back to the original value after the test is run, irrespective if it failed or not.
	 *
	 *  @param test A Runnable that executes the test-code
	 *  @param className The name that is used for the logger that should be adjusted
	 *  @param levels The actual log-levels that should be used, e.g. Level.FINE
	 */
	public static void runTestWithDifferentLogLevel(final Runnable test, final String className, final Level... levels) {
		Logger localLogger = Logger.getLogger(className);
		Level origLevel = localLogger.getLevel();
		for(Level level : levels) {
			localLogger.setLevel(level);
			try {
				test.run();
			} finally {
				localLogger.setLevel(origLevel);
			}
		}
	}

	/**
	 * Verify that the given URL is actually existing and results in a HTTP 200 return code
	 * if requested.
	 *
	 * This is used to stop tests early if required internet access is not available.
	 *
	 * @param urlString The URL that should be verified.
	 * @param timeout The timeout for network calls in milliseconds.
	 * @throws IOException If an error occurs while contacting the given URL.
	 */
	public static void assertURLWorks(String urlString, int timeout) throws IOException {
		URL url = URI.create(urlString).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
	        conn.setDoOutput(false);
	        conn.setDoInput(true);
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);

	        /* if connecting is not possible this will throw a connection refused exception */
	        conn.connect();

	        assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode(),
					"Expect URL '" + urlString + "' to be available and return HTTP 200");
        } finally {
        	conn.disconnect();
        }
	}

	/**
	 * Helper method which uses assume to not run tests if the current JVM runs in headless mode.
	 */
	public static void assumeCanShowDialogs() {
		assertNotNull(GraphicsEnvironment.getLocalGraphicsEnvironment());
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Can not run some tests when tests are executed in headless mode");
	}

	/**
	 * Creates a temporary directory which is guaranteed to be unique (via File.createTempFile)
	 * and ensures that the directory exists.
	 *
	 * Note: The caller needs to ensure that the directory is removed again after use else it
	 * 		will be left on the disk.
	 *
	 * @param  prefix     The prefix string to be used in generating the directory's
	 *                    name; must be at least three characters long
	 *
	 * @param  suffix     The suffix string to be used in generating the directory's
	 *                    name; may be <code>null</code>, in which case the
	 *                    suffix <code>".tmp"</code> will be used
	 * @return A File pointing to the newly created directory.
	 * @throws IOException If creating the temporary file fails.
	 * @throws AssertionError If creating the directory fails.
	 */
	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		final File dir = File.createTempFile(prefix, suffix);

		assertTrue(dir.delete());
		assertTrue(dir.mkdir());
		assertTrue(dir.exists());

		return dir;
	}
}
