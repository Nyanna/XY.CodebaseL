package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IAxis;

public abstract class AbstractAxis implements IAxis {
	@Override
	public IAxis clone() throws CloneNotSupportedException {
		return (AbstractAxis) super.clone();
	}
}
