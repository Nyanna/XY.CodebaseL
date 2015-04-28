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
 * high performance dynamic byte array for using in streamcopy.
 * Not synchronized.
 * 
 * @author xyan
 * 
 */
public class ByteArray {
    private byte[] data;
    private final int growth;
    private int lastIndex;

    /**
     * creates an empty array and growth by 10%
     */
    public ByteArray() {
        this(1, 10);
    }

    /**
     * creates an capacity array with growth of 10%
     * 
     * @param capacity
     */
    public ByteArray(final int capacity) {
        this(capacity, 10);
    }

    /**
     * creates an array with capacity and growth specified
     * 
     * @param capacity
     * @param growth
     */
    public ByteArray(final int capacity, final int growth) {
        data = new byte[capacity];
        this.growth = growth;
        lastIndex = 0;
    }

    /**
     * if able increases the capacity by capacity
     * 
     * @param capacity
     */
    public void upsize(final int capacity) {
        if (capacity > data.length) {
            final byte[] newOne = new byte[capacity];
            System.arraycopy(data, 0, newOne, 0, lastIndex);
            data = newOne;
        }
    }

    /**
     * increases by growth but at least by 1
     */
    public void upsize() {
        upsize(data.length + data.length / 100 * growth + 1); // grows by x
                                                              // percent
    }

    /**
     * caps any null values beyond the last used index
     */
    public void cap() {
        final byte[] newOne = new byte[lastIndex];
        System.arraycopy(data, 0, newOne, 0, lastIndex);
        data = newOne;
    }

    /**
     * add one byte
     * 
     * @param b
     */
    public void add(final byte b) {
        if (lastIndex + 1 > data.length) {
            upsize();
        }
        data[lastIndex] = b;
        lastIndex++;
    }

    /**
     * appends bytearray
     * 
     * @param b
     */
    public void add(final byte[] b) {
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
    public void add(final byte[] b, final int off, final int len) {
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
    public byte[] get() {
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
}
