/*===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) Technology Edition, v6
 * (C) Copyright IBM Corp. 2013, 2013. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *===========================================================================
 */
/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package net.xy.codebase.concurrent.jdk;

/**
 * A synchronizer that may be exclusively owned by a thread. This class provides
 * a basis for creating locks and related synchronizers that may entail a notion
 * of ownership. The <tt>AbstractOwnableSynchronizer</tt> class itself does not
 * manage or use this information. However, subclasses and tools may use
 * appropriately maintained values to help control and monitor access and
 * provide diagnostics.
 *
 * @since 1.6
 * @author Doug Lea
 */
public abstract class AbstractOwnableLock {
	/**
	 * Empty constructor for use by subclasses.
	 */
	protected AbstractOwnableLock() {
	}

	/**
	 * The current owner of exclusive mode synchronization.
	 */
	private Thread exclusiveOwnerThread;

	/**
	 * Sets the thread that currently owns exclusive access. A <tt>null</tt>
	 * argument indicates that no thread owns access. This method does not
	 * otherwise impose any synchronization or <tt>volatile</tt> field accesses.
	 */
	protected void setExclusiveOwnerThread(final Thread t) {
		exclusiveOwnerThread = t;
	}

	/**
	 * Returns the thread last set by <tt>setExclusiveOwnerThread</tt>, or
	 * <tt>null</tt> if never set. This method does not otherwise impose any
	 * synchronization or <tt>volatile</tt> field accesses.
	 *
	 * @return the owner thread
	 */
	protected Thread getExclusiveOwnerThread() {
		return exclusiveOwnerThread;
	}
}
