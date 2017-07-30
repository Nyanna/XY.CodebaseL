package net.xy.codebase.anim;

/**
 * every actor animation combination runs in an animation context
 * 
 * @author Xyan
 *
 */
public interface IAnimationContext {

	public long getTime();

	public void start();

}
