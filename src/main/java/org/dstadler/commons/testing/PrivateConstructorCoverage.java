package org.dstadler.commons.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * @author dominik.stadler
 *
 */
public class PrivateConstructorCoverage {
	/**
	 * Helper method for removing coverage-reports for classes with only static
	 * methods
	 *
	 * see for related EMMA ticket
	 * http://sourceforge.net/tracker/index.php?func=
	 * detail&amp;aid=1173251&amp;group_id=108932&amp;atid=651900
	 *
	 * add this to the test case for any class that has only static methods
	 * where coverage reports the default constructor as not covered
	 *
	 * Template:
	 *
	 * <code>

	// helper method to get coverage of the unused constructor
	{@literal @}Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(yourclass.class);
	}

	 </code>
	 *
	 * @param <T> The type of class to cover.
	 * @param targetClass The class to cover.
	 * @return The constructed instance if needed for further testing.
	 *
	 * @throws Exception If invoking the default constructor fails for any reason.
	 */
	public static <T> T executePrivateConstructor(final Class<T> targetClass) throws Exception {
        if(Modifier.isAbstract(targetClass.getModifiers())) {
        	throw new IllegalArgumentException("Cannot run the private constructor for abstract classes.");
        }

		// get the default constructor
		final Constructor<T> c = targetClass.getDeclaredConstructor();

		// make it callable from the outside
		c.setAccessible(true);

		// call it
		return c.newInstance((Object[]) null);
	}
}
