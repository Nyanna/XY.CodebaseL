package net.xy.codebase.collection;

public interface IArray<T> {

	public int length();

	public T get(int idx);

	public void set(int idx, T t);

	public void copy(int idx1, int idx2);

	public void copy(final int start1, final int start2, final int len);

	public void copyFrom(T[] array2, int start1, int start2, int len);

	public void copyTo(int start1, T[] array2, int start2, int len);
}
