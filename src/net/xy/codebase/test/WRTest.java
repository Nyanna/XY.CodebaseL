package net.xy.codebase.test;

import net.xy.codebase.mem.GCManagedPool;

public class WRTest {

	public static void main(final String[] args) throws InterruptedException {
		final GCManagedPool<WRObject> pool = new GCManagedPool<WRObject>() {

			@Override
			protected WRObject createObject() {
				System.out.println("Creating");
				return new WRObject();
			}

			@Override
			public void free(final WRObject entry) {
				System.out.println("Freeing");
				super.free(entry);
			}
		};

		while (true) {
			WRObject[] objs = new WRObject[4];
			for (int i = 0; i < objs.length; i++)
				objs[i] = pool.obtain();

			System.gc();
			Thread.sleep(100);
			objs = null;
			System.gc();
			Thread.sleep(3000);
		}
	}
}
