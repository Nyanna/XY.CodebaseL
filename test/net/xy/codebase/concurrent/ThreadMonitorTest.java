package net.xy.codebase.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ThreadMonitorTest {
	private ThreadMonitor tm;

	@Before
	public void setup() {
		tm = new ThreadMonitor();
	}

	@Test
	public void testNormalOperation() throws Exception {
		final CountDownLatch cdl1 = new CountDownLatch(3);
		final CountDownLatch cdl2 = new CountDownLatch(3);
		final ReentrantLock lock = new ReentrantLock();
		lock.lock();

		for (int i = 0; i < 3; i++)
			new Thread() {
				@Override
				public void run() {
					tm.enter();
					cdl1.countDown();
					lock.lock();
					lock.unlock();
					tm.leave();
					cdl2.countDown();
				};
			}.start();

		cdl1.await();
		Assert.assertEquals(3, tm.count());
		lock.unlock();
		cdl2.await();
		Assert.assertEquals(0, tm.count());
	}

	@Test
	public void testLeave() throws Exception {
		final CountDownLatch cdl1 = new CountDownLatch(3);
		final CountDownLatch cdl2 = new CountDownLatch(1);
		final ReentrantLock lock1 = new ReentrantLock();
		final ReentrantLock lock2 = new ReentrantLock();
		lock1.lock();
		lock2.lock();

		for (int i = 0; i < 2; i++)
			new Thread() {
				@Override
				public void run() {
					tm.enter();
					cdl1.countDown();
					lock1.lock();
					lock1.unlock();
					tm.leave();
				};
			}.start();
		new Thread() {
			@Override
			public void run() {
				tm.enter();
				cdl1.countDown();
				lock2.lock();
				lock2.unlock();
				tm.leave();
				cdl2.countDown();
			};
		}.start();

		cdl1.await();
		Assert.assertEquals(3, tm.count());
		lock2.unlock();
		cdl2.await();
		Assert.assertEquals(2, tm.count());
	}

	@Test
	public void testLockout() throws Exception {
		final CountDownLatch cdl1 = new CountDownLatch(2);
		final ReentrantLock lock1 = new ReentrantLock();
		lock1.lock();

		for (int i = 0; i < 2; i++)
			new Thread() {
				@Override
				public void run() {
					tm.enter();
					cdl1.countDown();
					lock1.lock();
					lock1.unlock();
					tm.leave();
				};
			}.start();
		cdl1.await();
		tm.lock();
		Assert.assertEquals(2, tm.count());
		Assert.assertFalse(tm.tryEnter(0));
	}

	@Test
	public void testRelease() throws Exception {
		final CountDownLatch cdl1 = new CountDownLatch(2);
		final CountDownLatch cdl2 = new CountDownLatch(2);
		final ReentrantLock lock1 = new ReentrantLock();
		tm.lock();
		lock1.lock();

		for (int i = 0; i < 2; i++)
			new Thread() {
				@Override
				public void run() {
					tm.enter();
					cdl1.countDown();
					lock1.lock();
					lock1.unlock();
					tm.leave();
					cdl2.countDown();
				};
			}.start();
		cdl1.await(5, TimeUnit.MILLISECONDS);
		Assert.assertEquals(0, tm.count());
		tm.release();
		cdl1.await();
		Assert.assertEquals(2, tm.count());
		lock1.unlock();
		cdl2.await();
		Assert.assertEquals(0, tm.count());
	}

	@Test
	public void testIllegalStates() throws Exception {
		tm.lock();
		try {
			tm.lock();
			Assert.fail();
		} catch (final IllegalMonitorStateException e) {}
		tm.release();
		try {
			tm.release();
			Assert.fail();
		} catch (final IllegalMonitorStateException e) {}
	}

	@Test
	public void testWaitAbs() throws Exception {
		final CountDownLatch cdl1 = new CountDownLatch(2);
		final ReentrantLock lock1 = new ReentrantLock();
		lock1.lock();

		for (int i = 0; i < 2; i++)
			new Thread() {
				@Override
				public void run() {
					tm.enter();
					cdl1.countDown();
					lock1.lock();
					lock1.unlock();
					tm.leave();
				};
			}.start();
		cdl1.await();
		tm.lockwaitAbs(2);
		Assert.assertEquals(2, tm.count());
		lock1.unlock();
		tm.waitAbs(0);
		Assert.assertEquals(0, tm.count());
	}
}
