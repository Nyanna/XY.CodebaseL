/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebasel;

/**
 * high performance dynamic object array.
 * Not synchronized.
 * 
 * @author xyan
 * 
 */
public class ObjectArray {
  private Object[] data;
  private final int growth;
  private int lastIndex;

  /**
   * creates an empty array and growth by 10%
   */
  public ObjectArray() {
    this(1, 10);
  }

  /**
   * creates an capacity array with growth of 10%
   * 
   * @param capacity
   */
  public ObjectArray(final int capacity) {
    this(capacity, 10);
  }

  /**
   * creates an array with capacity and growth specified
   * 
   * @param capacity
   * @param growth
   */
  public ObjectArray(final int capacity, final int growth) {
    data = new Object[capacity];
    this.growth = growth;
    lastIndex = 0;
  }

  /**
   * if able increases the capacity by capacity
   * 
   * @param capacity
   */
  synchronized public void upsize(final int capacity) {
    if (capacity > data.length) {
      final Object[] newOne = new Object[capacity];
      System.arraycopy(data, 0, newOne, 0, lastIndex);
      data = newOne;
    }
  }

  /**
   * increases by growth but at least by 1
   */
  synchronized public void upsize() {
    upsize(data.length + data.length / 100 * growth + 1); // grows by x
                                                          // percent
  }

  /**
   * caps any null values beyond the last used index
   */
  synchronized public void cap() {
    if (data.length >= lastIndex) {
      final Object[] newOne = new Object[lastIndex];
      System.arraycopy(data, 0, newOne, 0, lastIndex);
      data = newOne;
    }
  }

  /**
   * add one Object
   * 
   * @param b
   */
  synchronized public void add(final Object b) {
    if (lastIndex + 1 > data.length) {
      upsize();
    }
    data[lastIndex] = b;
    lastIndex++;
  }

  /**
   * appends Objectarray
   * 
   * @param b
   */
  synchronized public void add(final Object[] b) {
    while (lastIndex + b.length > data.length) {
      upsize();
    }
    System.arraycopy(b, 0, data, lastIndex, b.length);
    lastIndex += b.length;
  }

  /**
   * adds only an range
   * 
   * @param b
   * @param off
   * @param len
   */
  synchronized public void add(final Object[] b, final int off, final int len) {
    while (lastIndex + len > data.length) {
      upsize();
    }
    System.arraycopy(b, off, data, lastIndex, len);
    lastIndex += len;
  }

  /**
   * gets an reference to the array and calls cap
   * 
   * @return
   */
  public Object[] get() {
    cap();
    return data;
  }

  /**
   * returns the last used index or -1 in case of is empty
   * 
   * @return
   */
  public int getLastIndex() {
    return lastIndex - 1;
  }

  /**
   * copies all values to the given array
   * 
   * @param objs
   */
  public void toType(final Object[] objs) {
    cap();
    System.arraycopy(data, 0, objs, 0, data.length);
  }
}
