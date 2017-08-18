package net.xy.codebase.anim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.anim.impl.Transition;
import net.xy.codebase.collection.Array;

/**
 * default implementation for an animation remembering actor
 *
 * @author Xyan
 *
 */
public class Actor implements IActor {
	private static final Logger LOG = LoggerFactory.getLogger(Actor.class);
	private Array<IAnimation> anims;
	private Array<IAnimation> copy;

	@Override
	public void step(final IAnimationContext ac) {
		final Array<IAnimation> anims = this.anims;
		if (anims != null) {
			if (copy == null)
				copy = new Array<IAnimation>(IAnimation.class, anims.size());

			copy.reset();
			synchronized (anims) {
				copy.addAll(anims);
			}

			for (int i = 0; i < copy.size(); i++) {
				final IAnimation an = copy.get(i);
				an.step(this, ac);
			}
		}
	}

	@Override
	public IActor clearAnimations() {
		synchronized (this) {
			if (anims != null)
				anims.rewind();
		}
		return this;
	}

	@Override
	public void removeAnimation(final IAnimation anim) {
		synchronized (this) {
			if (anims != null)
				anims.remove(anim);
		}
	}

	@Override
	public void removeAnimation(final String id) {
		synchronized (this) {
			if (anims != null)
				for (int i = anims.size() - 1; i >= 0; i--)
					if (id.equals(anims.get(i).getId()))
						anims.removeIndex(i);
		}
	}

	@Override
	public boolean containsAnimation(final String id) {
		synchronized (this) {
			if (anims != null)
				for (int i = 0; i < anims.size(); i++)
					if (id.equals(anims.get(i).getId()))
						return true;
		}
		return false;
	}

	@Override
	public IAnimation getAnimation(final String id) {
		synchronized (this) {
			if (anims != null)
				for (int i = 0; i < anims.size(); i++) {
					final IAnimation anim = anims.get(i);
					if (id.equals(anim.getId()))
						return anim;
				}
		}
		return null;
	}

	@Override
	public IAnimation addAnimation(final IAnimation anim) {
		synchronized (this) {
			if (anims == null)
				anims = new Array<IAnimation>(IAnimation.class, 5);
			anims.addChecked(anim);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("Adding animation [" + anim + "][" + anims.toConcatString('\n') + "]");
		return anim;
	}

	@Override
	public void addAnimations(final IAnimation... anims) {
		synchronized (this) {
			if (this.anims == null)
				this.anims = new Array<IAnimation>(IAnimation.class, Math.max(5, anims.length));
			this.anims.addAll(anims);
		}
	}

	@Override
	public Transition addTransition(final INumericAttribute field, final double start, final double target,
			final long duration) {
		final Transition res = new Transition(field, start, target, duration);
		addAnimation(res);
		return res;
	}

	@Override
	public Transition addTransition(final INumericAttribute field, final double target, final long duration) {
		final Transition res = new Transition(field, target, duration);
		addAnimation(res);
		return res;
	}
}
