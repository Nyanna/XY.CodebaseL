package net.xy.codebase.jmx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jmx.remote.internal.RMIExporter;

import sun.rmi.registry.RegistryImpl;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;
import sun.rmi.transport.LiveRef;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public abstract class AbstractJMXRegistry implements IJMXRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJMXRegistry.class);

	private final Map<String, String> users = new HashMap<>();
	private JMXConnectorServer con;

	@Override
	public void register(final String alias, final Object inst) {
		register(alias, null, inst);
	}

	@Override
	public void register(final Object inst) {
		register(null, inst);
	}

	@Override
	public void register(final String alias, final String component, final Object inst) {
		try {
			final ObjectName name = getName(inst.getClass(), alias, component);
			if (LOG.isDebugEnabled())
				LOG.debug("JMX Object registered [" + name + "][" + inst + "]");
			getBeanServer().registerMBean(inst, name);
		} catch (final Exception e) {
			LOG.error("Error on registering MBean", e);
		}
	}

	@Override
	public void unregister(final String alias, final Class<?> clazz) {
		unregister(alias, null, clazz);
	}

	@Override
	public void unregister(final Class<?> clazz) {
		unregister(null, clazz);
	}

	@Override
	public void unregister(final String alias, final String component, final Class<?> clazz) {
		ObjectName name = null;
		try {
			name = getName(clazz, alias, component);
			getBeanServer().unregisterMBean(name);
		} catch (final InstanceNotFoundException e) {
			if (LOG.isDebugEnabled())
				LOG.debug("MBean instance not found [" + name + "]");
		} catch (final Exception e) {
			LOG.error("Error during MBean Removal", e);
		}
	}

	private ObjectName getName(final Class<?> clazz, final String alias, final String component) {
		StringBuilder sb = null;
		try {
			sb = new StringBuilder("jmx.generic:type=");
			sb.append(clazz.getSimpleName().replace("MBean", "").replace("MXBean", ""));
			if (component != null)
				sb.append(",component=").append(component);
			if (alias != null)
				sb.append(",name=").append(alias);
			return new ObjectName(sb.toString());
		} catch (final Exception e) {
			LOG.error("Error creating beanname [" + sb + "]", e);
			return null;
		}
	}

	protected void init(final int connectorPort, final int registryPort, final boolean useSSL) throws Exception {
		for (final MBeanServer entry : MBeanServerFactory.findMBeanServer(null))
			LOG.info("MBeanserver: " + entry.getDefaultDomain() + ", " + Arrays.toString(entry.getDomains()));

		final HashMap<String, Object> env = new HashMap<String, Object>();
		final PermanentExporter exporter = new PermanentExporter();
		env.put(RMIExporter.EXPORTER_ATTRIBUTE, exporter);
		RMIClientSocketFactory csf = null;
		RMIServerSocketFactory ssf = null;
		if (useSSL) {
			csf = new SslRMIClientSocketFactory();
			ssf = createServerSocketFactory(null, null);

			env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
			env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
		}
		env.put(JMXConnectorServer.AUTHENTICATOR, new IntegratedAuth());

		final MBeanServer beanServer = getBeanServer();
		LOG.debug("Created bean server");

		final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://0.0.0.0:" + connectorPort + "");
		con = JMXConnectorServerFactory.newJMXConnectorServer(url, env, beanServer);
		LOG.debug("Created RMI connector");

		con.start();
		LOG.info("Started RMI connector [" + con.getAddress() + "]");

		if (registryPort > -1)
			new SingleEntryRegistry(registryPort, csf, ssf, "jmxrmi", exporter.firstExported);
		LOG.info("Published to single entry registry");
	}

	public void shutdown() {
		if (con != null)
			try {
				con.stop();
			} catch (final IOException e) {
				LOG.error("Error stopping JMX Toolkit", e);
			}
	}

	protected abstract RMIServerSocketFactory createServerSocketFactory(final String[] enabledCipherSuites,
			final String[] enabledProtocols);

	protected MBeanServer getBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	protected void addUser(final String name, final String password) {
		users.put(name, password);
	}

	protected static TrustManager[] createTrustAllCerts() {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
			}
		} };
		return trustAllCerts;
	}

	protected static KeyStore loadKeystore(final String keyStore, final char[] keyStorePasswd) throws KeyStoreException,
			FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
		KeyStore ks;
		ks = KeyStore.getInstance(KeyStore.getDefaultType());
		final FileInputStream ksfis = new FileInputStream(keyStore);
		try {
			ks.load(ksfis, keyStorePasswd);
		} finally {
			ksfis.close();
		}
		return ks;
	}

	protected static KeyStore createSelfsignedKeystore(final String password) throws Exception {
		final int keysize = 1024;
		final String commonName = "Auto Generated Certificate";
		final String organizationalUnit = "IT";
		final String organization = "None";
		final String city = "None";
		final String state = "None";
		final String country = "None";
		final long validity = 31; // 3 years
		final String alias = "certificate0";
		final char[] keyPass = password.toCharArray();

		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);

		final CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
		final X500Name x500Name = new X500Name(commonName, organizationalUnit, organization, city, state, country);

		keypair.generate(keysize);
		final X509Certificate[] chain = new X509Certificate[1];

		chain[0] = keypair.getSelfCertificate(x500Name, new Date(), validity * 24 * 60 * 60);
		chain[0].checkValidity();
		ks.setCertificateEntry(alias, chain[0]);
		ks.setKeyEntry(alias, keypair.getPrivateKey(), keyPass, chain);

		return ks;
	}

	protected class IntegratedAuth implements JMXAuthenticator {
		public IntegratedAuth() {
		}

		@Override
		public Subject authenticate(final Object credentials) {
			if (!(credentials instanceof String[])) {
				if (credentials == null)
					throw new IllegalArgumentException("Credentials required");

				final String message = "Credentials should be String[] instead of " + credentials.getClass().getName();
				throw new IllegalArgumentException(message);
			}

			final String[] creds = (String[]) credentials;

			final String name = creds[0];
			if (!users.containsKey(creds[0]) || !creds[1].equals(users.get(name)))
				throw new IllegalArgumentException("Not authorized");

			final HashSet<JMXPrincipal> prin = new HashSet<>();
			prin.add(new JMXPrincipal("admin"));
			return new Subject(false, prin, new HashSet<>(), new HashSet<>());
		}
	}

	/**
	 * obsolete case, canceled. Attention install alls trusted certmanager!
	 *
	 * @param password
	 * @param enabledCipherSuites
	 * @param enabledProtocols
	 * @return
	 */
	protected SslRMIServerSocketFactory createSSLFactorySelfsigned(final String password,
			final String[] enabledCipherSuites, final String[] enabledProtocols) {
		try {

			final char[] keyStorePasswd = password.toCharArray();
			final KeyStore ks = createSelfsignedKeystore(password);
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyStorePasswd);

			final KeyStore ts = null;
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			final SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(kmf.getKeyManagers(), createTrustAllCerts(), null);
			SSLContext.setDefault(ctx);

			return new SslRMIServerSocketFactory(ctx, enabledCipherSuites, enabledProtocols, true);
		} catch (final Exception e) {
			throw new IllegalStateException("Error on socket factory init", e);
		}
	}

	/**
	 * obsolete case canceled
	 *
	 * @param enabledCipherSuites
	 * @param enabledProtocols
	 * @return
	 */
	@SuppressWarnings({ "unused", "null" })
	protected SslRMIServerSocketFactory createSSLFactoryByFile(final String[] enabledCipherSuites,
			final String[] enabledProtocols) {
		try {
			// Load the SSL keystore properties from the config file
			final String keyStore = "resources/jmx/lwserver.jks";
			final String keyStorePassword = "WUNHMhVhbQJHVXm4V4vK";
			final String trustStore = null; // "resources/lwserver_trust.jks";
			final String trustStorePassword = null; // "DfmOpvMgZ1HVvQV0jEsl";
			// TODO add trustmanager to allow only specific clients to connect

			char[] keyStorePasswd = null;
			KeyStore ks = null;
			if (keyStore != null) {
				if (keyStorePassword.length() != 0)
					keyStorePasswd = keyStorePassword.toCharArray();
				ks = loadKeystore(keyStore, keyStorePasswd);
			}
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyStorePasswd);

			char[] trustStorePasswd = null;
			KeyStore ts = null;
			if (trustStore != null) {
				if (trustStorePassword.length() != 0)
					trustStorePasswd = trustStorePassword.toCharArray();
				ts = loadKeystore(trustStore, trustStorePasswd);
			}
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			final SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			return new SslRMIServerSocketFactory(ctx, enabledCipherSuites, enabledProtocols, false);
		} catch (final Exception e) {
			throw new IllegalStateException("Error on socket factory init", e);
		}
	}

	protected static class PermanentExporter implements RMIExporter {
		public Remote firstExported;

		@Override
		public Remote exportObject(final Remote obj, final int port, final RMIClientSocketFactory csf,
				final RMIServerSocketFactory ssf) throws RemoteException {

			ObjID id = new ObjID();
			synchronized (this) {
				if (firstExported == null) {
					firstExported = obj;
					id = new ObjID(port);
				}
			}

			if (LOG.isDebugEnabled())
				LOG.debug("Exporting object id [" + id + "][" + obj + "]");
			final UnicastServerRef ref;
			if (csf == null && ssf == null)
				ref = new UnicastServerRef(new LiveRef(id, port));
			else
				ref = new UnicastServerRef2(new LiveRef(id, port, csf, ssf));
			return ref.exportObject(obj, null, true);
		}

		// Nothing special to be done for this case
		@Override
		public boolean unexportObject(final Remote obj, final boolean force) throws NoSuchObjectException {
			return UnicastRemoteObject.unexportObject(obj, force);
		}

	}

	protected class SingleEntryRegistry extends RegistryImpl {
		private static final long serialVersionUID = -4897238949499730950L;

		private final String name;
		private final Remote object;

		SingleEntryRegistry(final int port, final RMIClientSocketFactory csf, final RMIServerSocketFactory ssf,
				final String name, final Remote object) throws RemoteException {
			super(port, csf, ssf);
			this.name = name;
			this.object = object;
		}

		@Override
		public String[] list() {
			return new String[] { name };
		}

		@Override
		public Remote lookup(final String name) throws NotBoundException {
			if (name.equals(this.name))
				return object;
			throw new NotBoundException("Not bound: \"" + name + "\" (only " + "bound name is \"" + this.name + "\")");
		}

		@Override
		public void bind(final String name, final Remote obj) throws AccessException {
			throw new AccessException("Cannot modify this registry");
		}

		@Override
		public void rebind(final String name, final Remote obj) throws AccessException {
			throw new AccessException("Cannot modify this registry");
		}

		@Override
		public void unbind(final String name) throws AccessException {
			throw new AccessException("Cannot modify this registry");
		}
	}
}
