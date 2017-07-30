package net.xy.codebase.anim;

import net.xy.codebase.collection.Array;

/**
 * container object to build complex animation chains
 * 
 * @author Xyan
 *
 */
public class Timeline {

	/**
	 * first, start animations
	 */
	private final Array<IAnimation> start = new Array<IAnimation>(IAnimation.class);
	/**
	 * pointer to current/last animation set
	 */
	private IAnimation current = null;
	/**
	 * prior pointer to current/last animation set
	 */
	private IAnimation lastCurrent = null;

	/**
	 * default empty
	 */
	public Timeline() {
	}

	/**
	 * with single start animation
	 *
	 * @param anim
	 */
	public Timeline(final IAnimation anim) {
		after(anim);
	}

	/**
	 * after the current animation ahs ended attach the given one
	 *
	 * @param anim
	 */
	public IAnimation after(final IAnimation anim) {
		if (start.isEmpty())
			start.addChecked(anim);
		if (current != null)
			current.after(anim);
		lastCurrent = current;
		current = anim;
		return anim;
	}

	/**
	 * add concurrently to the actual animation
	 *
	 * @param anim
	 */
	public IAnimation add(final IAnimation anim) {
		if (lastCurrent != null)
			lastCurrent.after(anim);
		else {
			if (start.isEmpty())
				current = anim;
			start.addChecked(anim);
		}
		return anim;
	}

	/**
	 * andd concurrently to the actual animation
	 *
	 * @param anims
	 */
	public void add(final IAnimation[] anims) {
		for (final IAnimation anim : anims)
			add(anim);
	}

	/**
	 * inserts the first elemt after and adds further elements
	 *
	 * @param anims
	 */
	public void after(final IAnimation[] anims) {
		for (int i = 0; i < anims.length; i++)
			if (i == 0)
				add(anims[i]);
			else
				after(anims[i]);
	}

	/**
	 * @return the ready build raw animation data chain, read to be added to an
	 *         actor
	 */
	public IAnimation[] get() {
		return start.shrink();
	}
}
