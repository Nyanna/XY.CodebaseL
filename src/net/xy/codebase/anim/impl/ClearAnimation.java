package net.xy.codebase.anim.impl;

import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;

public class ClearAnimation extends AbstractAnimation {
	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		actor.clearAnimations();
	}
}
