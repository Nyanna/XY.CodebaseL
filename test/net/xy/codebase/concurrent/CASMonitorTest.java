package net.xy.codebase.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class CASMonitorTest {
	@Test
	public void testWaitNCallAll() throws InterruptedException {
		final CountDownLatch cl1 = new CountDownLatch(5);
		final CountDownLatch cl2 = new CountDownLatch(5);
		final Monitor cm = new Monitor();
		for (int i = 0; i < 5; i++)
			new Thread() {
				@Override
				public void run() {
					cl1.countDown();
					cm.await(cm.getState());
					cl2.countDown();
				};
			}.start();
		cl1.await();
		Assert.assertEquals(0, cl1.getCount());
		Assert.assertEquals(5, cl2.getCount());
		cm.call();
		cl2.await();
		Assert.assertEquals(0, cl2.getCount());
	}

	@Test
	public void testModcount() throws InterruptedException {
		final CountDownLatch cl1 = new CountDownLatch(5);
		final Monitor cm = new Monitor();
		Assert.assertEquals(0, cm.getState());
		for (int i = 0; i < 5; i++)
			new Thread() {
				@Override
				public void run() {
					for (int i = 0; i < 5; i++)
						cm.call();
					cl1.countDown();
				};
			}.start();
		cl1.await();
		Assert.assertEquals(25, cm.getState());
	}

	@Test
	public void testReturnByState() throws InterruptedException {
		final Monitor cm = new Monitor();
		final int state = cm.getState();
		cm.call();
		cm.await(state);
		// should run through
	}

	@Test
	public void testReturnTimeOut() throws InterruptedException {
		final Monitor cm = new Monitor();
		final long start = System.currentTimeMillis();
		final int state = cm.getState();
		cm.await(state, TimeUnit.MILLISECONDS.toNanos(5));
		Assert.assertTrue(System.currentTimeMillis() >= start + 5);
	}

	@Test
	public void testWaitCycles() throws InterruptedException {
		final AtomicInteger count = new AtomicInteger();
		final Monitor cm = new Monitor();
		final Semaphore sp = new Semaphore(0);

		for (int i = 0; i < 5; i++)
			new Thread() {
				@Override
				public void run() {
					for (;;) {
						final int state = cm.getState();
						sp.release();
						cm.await(state);
						count.incrementAndGet();
					}
				};
			}.start();
		for (int i = 0; i < 100; i++) {
			sp.acquire(5);
			cm.call();
		}
		sp.acquire(5);
		Assert.assertEquals(500, count.get());
	}
}
