package net.xy.codebase.test;

//import net.xy.codebase.thread.WRMonitor.ResurrectedReference;

public class WRObject /* extends WeakReference<Object> */{
	// private ResurrectedReference rec = null;

	// public WRObject(final Object referent, final ReferenceQueue<? super
	// Object> q) {
	// super(referent, q);
	// }

	@Override
	protected void finalize() throws Throwable {
		// System.out.println("Discard object");
	}

	// public void setWR(final ResurrectedReference rec) {
	// this.rec = rec;
	// }
}