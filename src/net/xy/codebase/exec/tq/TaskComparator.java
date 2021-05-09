package net.xy.codebase.exec.tq;

import java.util.Comparator;

import net.xy.codebase.Primitive;
import net.xy.codebase.exec.tasks.ITask;

/**
 * comparator for task ordering
 *
 * @author Xyan
 *
 */
public class TaskComparator implements Comparator<ITask> {
	@Override
	public int compare(final ITask t1, final ITask t2) {
		if (t1 == t2)
			throw new IllegalStateException("Same object allready in queue [" + t1 + "][" + t2 + "]");
		if (t1 == null || t2 == null)
			throw new IllegalStateException("Queue task is null [" + t1 + "][" + t2 + "]");
		return Primitive.compare(t1.nextRunFixed(), t2.nextRunFixed());
	}
}