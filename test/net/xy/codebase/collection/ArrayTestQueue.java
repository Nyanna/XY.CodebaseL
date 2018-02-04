package net.xy.codebase.collection;

import net.xy.codebase.exec.ThreadUtils;

/**
 * for test purpose only
 *
 * @author Xyan
 *
 */
public class ArrayTestQueue extends ArrayQueue<Integer> {

	public ArrayTestQueue(final int maxCount) {
		super(Integer.class, maxCount);
	}

	/**
	 * inner emthod to select index and put object
	 *
	 * @param elem
	 * @return
	 */
	@Override
	protected boolean addInner(final Integer elem) {
		int putIdx, getIdx, loop = 0;
		for (;;) {
			putIdx = putIndex.get();
			getIdx = getIndex.get(); // old broken !!
			final int checkLimit = checkLimit(-1);
			if (putIndex.compareAndSet(putIdx, putIdx) && getIndex.compareAndSet(getIdx, getIdx))
				if (checkLimit == SIZE_MAXED)
					return false;

				else if (checkLimit == SIZE_OK && putIndex.compareAndSet(putIdx, putIdx + 1))
					break;
			loop = ThreadUtils.yieldCAS(loop);
		}

		final int tarIdx = remainder(putIdx, elements.length());
		while (!elements.compareAndSet(tarIdx, null, tarIdx))
			ThreadUtils.yield();
		return true;
	}
}
