package net.xy.codebase.asserts;

/**
 * assertion utility methods
 *
 * @author User
 *
 */
public class Assert {
	/**
	 * checks whether value is true and if not throws an AssertionError.
	 *
	 * @param val
	 */
	public static void True(final boolean val) throws AssertionError {
		True(val, null);
	}

	/**
	 * checks whether value is true and if not throws an AssertionError with an
	 * optional error message.
	 *
	 * @param val
	 */
	public static void True(final boolean val, final String message) throws AssertionError {
		if (!val)
			throw new AssertionError(message != null ? message : "Assertion is false", null);
	}
}
