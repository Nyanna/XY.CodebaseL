package net.xy.codebase.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.xy.codebase.Primitive;

public class BasicArrayAdapter<T> implements IArray<T> {

	private T[] array;

	public BasicArrayAdapter() {
	}

	public BasicArrayAdapter(final T[] array) {
		this.array = array;
	}

	public BasicArrayAdapter<T> setArray(final T[] array) {
		this.array = array;
		return this;
	}

	@Override
	public int length() {
		return array.length;
	}

	@Override
	public T get(final int idx) {
		return array[idx];
	}

	@Override
	public void set(final int idx, final T t) {
		array[idx] = t;
	}

	@Override
	public void copy(final int idx1, final int idx2) {
		array[idx1] = array[idx2];
	}

	@Override
	public void copy(final int start1, final int start2, final int len) {
		System.arraycopy(array, start1, array, start2, len);
	}

	@Override
	public void copyFrom(final T[] array2, final int start1, final int start2, final int len) {
		System.arraycopy(array2, start1, array, start2, len);
	}

	@Override
	public void copyTo(final int start1, final T[] array2, final int start2, final int len) {
		System.arraycopy(array, start1, array2, start2, len);
	}

	// test method
	public static void main(final String[] args) {
		final TimSortX<Integer> ts = new TimSortX<Integer>();

		Integer[] a = new Integer[] { 3, 5, 3, 4, 7, 6, 3, 6, 8, 56, 324, 3, 23 };
		System.out.println(Arrays.toString(a));
		ts.doSort(new BasicArrayAdapter<Integer>(a), new Comparator<Integer>() {
			@Override
			public int compare(final Integer object1, final Integer object2) {
				return Primitive.compare(object1, object2);
			}
		}, 0, a.length);
		System.out.println(Arrays.toString(a));

		final List<Integer> tl = new ArrayList<Integer>();
		final Random rnd = new Random();
		for (int i = 0; i < 10000; i++)
			tl.add(rnd.nextInt(5000));
		a = tl.toArray(new Integer[tl.size()]);
		System.out.println(Arrays.toString(a));
		ts.doSort(new BasicArrayAdapter<Integer>(a), new Comparator<Integer>() {
			@Override
			public int compare(final Integer object1, final Integer object2) {
				return Primitive.compare(object1, object2);
			}
		}, 0, a.length);
		System.out.println(Arrays.toString(a));
	}
}
