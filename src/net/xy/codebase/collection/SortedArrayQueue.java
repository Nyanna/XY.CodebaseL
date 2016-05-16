package net.xy.codebase.collection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import net.xy.codebase.Primitive;

/**
 * extension of the unbounded self expaning qeue to support sorting. sorting
 * should be done frome a single worker thread implementation.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class SortedArrayQueue<E> extends ArrayQueue<E> {
	private final TimSortX<E> sorter = new TimSortX<E>();
	private final Comparator<E> comparator;
	private final QueueArrayAdapter qadap = new QueueArrayAdapter();

	public SortedArrayQueue(final Class<E> clazz, final int maxCount, final Comparator<E> comparator) {
		super(clazz, maxCount);
		this.comparator = comparator;
	}

	public synchronized void sort() {
		sorter.doSort(qadap, comparator, 0, count);
	}

	public class QueueArrayAdapter implements IArray<E> {
		@Override
		public int length() {
			return elements.capacity();
		}

		protected int getRi(final int idx) {
			final int res = (getIdx + idx) % elements.capacity();
			// System.out.println("idx: " + idx + " > " + res);
			return res;
		}

		@Override
		public E get(final int idx) {
			return elements.get(getRi(idx));
		}

		@Override
		public void set(final int idx, final E t) {
			elements.set(getRi(idx), t);
		}

		@Override
		public void copy(final int idx1, final int idx2) {
			elements.copy(getRi(idx1), getRi(idx2));
		}

		@Override
		public void copy(final int start1, final int start2, final int len) {
			if (start1 < start2)
				for (int i = len - 1; i >= 0; i--)
					copy(start2 + i, start1 + i);
			else
				for (int i = 0; i < len; i++)
					copy(start2 + i, start1 + i);
		}

		@Override
		public void copyFrom(final E[] array2, final int start1, final int start2, final int len) {
			if (start1 < start2)
				for (int i = len - 1; i >= 0; i--)
					set(start2 + i, array2[start1 + i]);
			else
				for (int i = 0; i < len; i++)
					set(start2 + i, array2[start1 + i]);
		}

		@Override
		public void copyTo(final int start1, final E[] array2, final int start2, final int len) {
			if (start1 < start2)
				for (int i = len - 1; i >= 0; i--)
					array2[start2 + i] = get(start1 + i);
			else
				for (int i = 0; i < len; i++)
					array2[start2 + i] = get(start1 + i);
		}
	}

	public static void main(final String[] args) {
		final SortedArrayQueue<Integer> saq = new SortedArrayQueue<Integer>(Integer.class, 10000,
				new Comparator<Integer>() {
					@Override
					public int compare(final Integer object1, final Integer object2) {
						return Primitive.compare(object1, object2);
					}
				});
		final Random rnd = new Random();
		for (int i = 0; i < 60; i++)
			if (rnd.nextBoolean())
				saq.take();
			else
				saq.add(rnd.nextInt(1000));

		System.out.println(Arrays.toString(saq.elements.getElements()));
		saq.sort();
		System.out.println(Arrays.toString(saq.elements.getElements()));
	}
}
