package net.xy.codebase.jmx;

import java.rmi.server.RMIServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXToolkit extends AbstractJMXRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(JMXToolkit.class);

	public JMXToolkit(final int port) {
		try {
			addUser("admin", "admin");
			init(port, -1, true);
		} catch (final Exception e) {
			LOG.error("Error initializing JMX Toolkit", e);
		}
	}

	@Override
	protected RMIServerSocketFactory createServerSocketFactory(final String[] enabledCipherSuites,
			final String[] enabledProtocols) {
		return super.createSSLFactorySelfsigned("password", enabledCipherSuites, enabledProtocols);
	}

	public static void main(final String[] args) {
		try {
			final JMXToolkit tk = new JMXToolkit(1000);
			Thread.sleep(6000000);
			tk.shutdown();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
