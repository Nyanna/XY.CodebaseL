package net.xy.codebase.collection;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import net.xy.codebase.exec.ThreadUtils;

public class ArrayQueueTest {

	@Test
	public void testAddNRemove() {
		final ArrayQueue<Object> aq = new ArrayQueueUnbounded<Object>(Object.class, 1024 * 10);
		final Object obj = new Object();
		Assert.assertTrue(aq.add(obj));
		Assert.assertEquals(1, aq.size());
		Assert.assertEquals(obj, aq.peek());
		Assert.assertEquals(obj, aq.take());
		Assert.assertNull(aq.take());
	}

	@Test
	public void testMaxLimit() {
		final ArrayQueue<Object> aq = new ArrayQueueUnbounded<Object>(Object.class, 10);
		final Object obj = new Object();
		for (int i = 0; i < 10; i++)
			Assert.assertTrue(aq.add(obj));
		Assert.assertFalse(aq.add(obj));
		Assert.assertEquals(10, aq.size());
	}

	@Test
	public void testGrowth() {
		final int old = Array.MIN_GROWTH;
		Array.MIN_GROWTH = 2;
		final ArrayQueue<Integer> aq = new ArrayQueueUnbounded<Integer>(Integer.class, 2000);
		int count = 0;
		int cnt = 0;
		for (int i = 1000; i > 0; i--) {
			Integer got = 0;
			if (Math.random() > 0.4d)
				aq.add(count++);
			else if ((got = aq.take()) != null && got != cnt++)
				throw new RuntimeException("Lost element [" + got + "]");
		}
		Array.MIN_GROWTH = old;
	}

	@Test
	public void testNormalOperationSim() throws Exception {
		// float brun = 0;
		// ThreadUtils.sleep(5000);
		// for (int h = 1; h < 3; h++)
		// for (int i = 1; i < 10; i++)
		// for (int j = 1; j < 10; j++)
		// for (int k = 2; k < 10; k++)
		{
			final int amount = 100000;
			final int size = amount / 10;
			// final int size = amount / k;
			final ArrayQueue<Object> aq = new ArrayQueue<Object>(Object.class, size);
			startSim(aq, 3, 3, amount);
			// final float trun = startSim(aq, i, j, 10000000);
			// if (trun > brun) {
			// brun = trun;
			// System.out.println("Better, Amount: " + amount + " Producers: " +
			// i + " Consumers: " + j + " "
			// + " Size: " + size + " (1/" + k + ")");
			// }
		}
	}

	public static void main(final String[] args) throws Exception {
		new ArrayQueueTest().testNormalOperationSim();
	}

	@Test
	public void testNormalOperationFixed() throws Exception {
		final ArrayQueue<Object> aq = new ArrayQueue<Object>(Object.class, 1024 * 10);
		testNormalOperation(aq);
	}

	@Test
	public void testNormalOperationGrowth() throws Exception {
		final ArrayQueue<Object> aq = new ArrayQueueUnbounded<Object>(Object.class, 1024 * 10);
		testNormalOperation(aq);
	}

	private void testNormalOperation(final ArrayQueue<Object> aq) throws Exception {
		final long seed = System.currentTimeMillis();
		System.out.print("Seed: " + seed + " ");
		final Random rnd = new Random(seed);
		final int producers = rnd.nextInt(10) + 1;
		final int consumers = rnd.nextInt(10) + 1;
		final int amount = (100 + rnd.nextInt(1000)) * producers;
		System.out.println("Amount: " + amount + " Producers: " + producers + " Consumers: " + consumers + " ");
		startSim(aq, producers, consumers, amount);
	}

	private float startSim(final ArrayQueue<Object> aq, final int producers, final int consumers, final long amount)
			throws InterruptedException {
		final Object elem = new Object();
		final AtomicInteger proc = new AtomicInteger();

		final CountDownLatch ths = new CountDownLatch(consumers);
		final CountDownLatch thp = new CountDownLatch(producers);
		for (int i = 0; i < consumers; i++) {
			new Thread("Consumer") {
				@Override
				public void run() {
					for (;;) {
						final Object res = aq.take();
						if (res != null)
							proc.incrementAndGet();
						else
							Thread.yield();
						if (proc.get() >= amount)
							break;
					}
					ths.countDown();
				};
			}.start();
			ThreadUtils.sleep(new Random().nextInt(10));
		}
		for (int i = 0; i < producers; i++) {
			new Thread("Producer") {
				@Override
				public void run() {
					for (long j = amount / producers + 1; j > 0;)
						if (aq.add(elem))
							j--;
						else
							ThreadUtils.sleep(1);
					thp.countDown();
				};
			}.start();
			ThreadUtils.sleep(new Random().nextInt(10));
		}

		final long starts = System.currentTimeMillis();
		final long start = System.nanoTime();
		// for (;;) {
		// ths.await(1, TimeUnit.SECONDS);
		// final long time = System.nanoTime() - start;
		// final long times = System.currentTimeMillis() - starts;
		// // System.out.println("Avr/Per: " + (double) time / proc.get() + "
		// // Elem/ms:" + (float) proc.get() / times
		// // + " (" + proc.get() + ")");
		//
		// if (proc.get() >= amount)
		// break;
		// }
		ths.await();
		thp.await();
		final long time = System.nanoTime() - start;
		final long times = System.currentTimeMillis() - starts;
		System.out.print("Amount: " + amount + " Producers: " + producers + " Consumers: " + consumers + " ");
		final float elemss = (float) proc.get() / times;
		System.out.println("Avr/Per: " + (double) time / proc.get() + " Elem/ms:" + elemss + " (" + proc.get() + ")");
		return elemss;
	}
}
