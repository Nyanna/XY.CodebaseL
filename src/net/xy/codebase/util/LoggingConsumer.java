package net.xy.codebase.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * consumer for typical promise using and logging of error in completion stage
 *
 * @author Xyan
 *
 * @param <T>
 */
public abstract class LoggingConsumer<T> implements Consumer<T> {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingConsumer.class);

	@Override
	public final void accept(final T t) {
		try {
			acceptGuarded(t);
		} catch (final Throwable e) {
			LOG.error("Future was termianting abnormally", e);
		}
	}

	public abstract void acceptGuarded(T t);
}
