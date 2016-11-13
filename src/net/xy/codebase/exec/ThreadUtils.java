package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);
	private static final long MSInNS = TimeUnit.MILLISECONDS.toNanos(1);
	private static final long BACKOFF_LIMIT = TimeUnit.SECONDS.toNanos(1);

	public static void sleep(final int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (final InterruptedException e) {
			LOG.info("Thread interupted", e);
		}
	}

	public static void sleep(final long nanos) {
		try {
			Thread.sleep(nanos / MSInNS, (int) (nanos % MSInNS));
		} catch (final InterruptedException e) {
			LOG.info("Thread interupted", e);
		}
	}

	public static void assertThread(final String name) {
		assert((ThreadExtended) Thread.currentThread()).getThreadName().equals(name);
	}

	public static void yield() {
		Thread.yield();
	}

	public static int yieldCAS(final int iteration) {
		// use Binary Exponential Backoff
		if (iteration > 0) {
			final int slots = (int) Math.pow(2, iteration);
			final long backoff = randInt(System.nanoTime(), slots);
			if (backoff > 0) //
			{
				final long time = backoff * 1000;
				sleep(time < BACKOFF_LIMIT ? time : BACKOFF_LIMIT);
				return iteration + 1;
			}
		}
		Thread.yield();
		return iteration + 1;

	}

	public static int randInt(final long gseed, final int n) {
		final long multiplier = 0x5deece66dL;
		long seed = (gseed ^ multiplier) & (1L << 48) - 1;

		if ((n & -n) == n) {
			seed = seed * multiplier + 0xbL & (1L << 48) - 1;
			final int next = (int) (seed >>> 48 - 31);
			return (int) (n * (long) next >> 31);
		}
		int bits, val;
		do {
			seed = seed * multiplier + 0xbL & (1L << 48) - 1;
			final int next = (int) (seed >>> 48 - 31);
			bits = next;
			val = bits % n;
		} while (bits - val + n - 1 < 0);
		return val;
	}
}
