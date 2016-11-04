package net.xy.codebase.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Full period LCG implementation with random input parameters relating to input
 * amount and period. Uses random start offset and XOR shift to scramble output
 * further.
 *
 * @author Xyan
 *
 */
public class FindFullPeriodLCG {
	private static final Random RND = new Random();

	// private WAddress res;

	private final short[][] mat;
	// private final ICriteriaChecker crit;
	private int count;

	private final int size;
	private int fac;
	private int inc;
	private final int mask;
	@SuppressWarnings("unused")
	private final int off;
	private long pos;

	public FindFullPeriodLCG(
			final short[][] mat/* , final ICriteriaChecker crit */) {
		this.mat = mat;
		// this.crit = crit;
		size = count = mat.length;

		off = RND.nextInt(count);
		mask = RND.nextInt(count);

		// get factor
		int cMul = getPrimeFactorMultiply(size);
		if (size % 4 == 0)
			cMul = getCommonMultiply(cMul, 4);
		do
			fac = cMul * RND.nextInt((Integer.MAX_VALUE - 1) / cMul) + 1;
		while (fac <= 0);

		// get increment
		final BigInteger bi = BigInteger.valueOf(size);
		do
			inc = RND.nextInt(Integer.MAX_VALUE);
		while (bi.gcd(BigInteger.valueOf(inc)).intValue() != 1);
	}

	private int next() {
		// LCG
		pos = (fac * pos + inc) % size;
		return (int) ((pos ^ mask) >= size ? pos : pos ^ mask);
	}

	/**
	 * gets common multiply basefactor for 20 = 2² * 5 it gets 2 * 5
	 *
	 * @param number
	 * @return
	 */
	private int getPrimeFactorMultiply(final int number) {
		int n = number;
		int cMul = 0;
		int lastFac = 0;

		for (int i = 2; i <= n; i++)
			while (n % i == 0) {
				n /= i;
				if (lastFac != i) {
					lastFac = i;
					if (cMul == 0)
						cMul = i;
					else
						cMul *= i;
				}
			}
		return cMul;
	}

	/**
	 * gets common multiply by prime factor multiplycation
	 *
	 * @param number
	 * @param number2
	 * @return
	 */
	private int getCommonMultiply(final int number, final int number2) {
		int n1 = number;
		int n2 = number2;
		int kgv = 0;

		for (int i = 2; i <= n1 || i <= n2; i++) {
			int an1 = 0;
			while (n1 % i == 0) {
				n1 /= i;
				an1++;
			}
			int an2 = 0;
			while (n2 % i == 0) {
				n2 /= i;
				an2++;
			}
			final int mul = Math.max(an1, an2);
			if (mul > 0)
				if (kgv == 0)
					kgv = i * mul;
				else
					kgv *= i * mul;
		}
		return kgv;
	}

	/**
	 * gets a list of all prime factors
	 *
	 * @param number
	 * @return
	 */
	public static List<Integer> primeFactors(final int number) {
		int n = number;
		final List<Integer> factors = new ArrayList<Integer>();
		for (int i = 2; i <= n; i++)
			while (n % i == 0) {
				factors.add(i);
				n /= i;
			}
		return factors;
	}

	public static void main(final String[] args) {
		final int sum = new Random().nextInt(300);// 40;
		System.out.println("Sum: " + sum + ", " + primeFactors(sum));
		final FindFullPeriodLCG rnd = new FindFullPeriodLCG(new short[sum][]);
		int i = 0;
		for (; i < rnd.mat.length; i++)
			if (i > 0 && i % 20 == 0)
				System.out.println(rnd.next());
			else
				System.out.print(rnd.next() + ",");
	}
}
