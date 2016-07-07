package net.xy.codebase;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Strings {
	public static final DecimalFormat FloatFormat = new DecimalFormat("#.#####",
			DecimalFormatSymbols.getInstance(Locale.US));

	public static String fformat(final float number) {
		return FloatFormat.format(number);
	}

	public static String combine(final String s1, final String s2, final String s3, final String s4, final String s5,
			final String s6, final String s7, final String s8) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
		return res.toString();
	}

	public static String combine(final String s1, final String s2, final String s3, final String s4, final String s5,
			final String s6, final String s7) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7);
		return res.toString();
	}

	public static String combine(final String s1, final String s2, final String s3, final String s4, final String s5,
			final String s6) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6);
		return res.toString();
	}

	public static String combine(final String s1, final String s2, final String s3, final String s4, final String s5) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3).append(s4).append(s5);
		return res.toString();
	}

	public static String combine(final String s1, final String s2, final String s3, final String s4) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3).append(s4);
		return res.toString();
	}

	public static String combine(final String s1, final String s2, final String s3) {
		final StringBuilder res = new StringBuilder();
		res.append(s1).append(s2).append(s3);
		return res.toString();
	}
}
