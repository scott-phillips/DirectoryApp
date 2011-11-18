package edu.tamu.directoryapp.directory.ldap;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.partition.impl.btree.MutableBTreePartitionConfiguration;
import org.apache.directory.server.core.schema.bootstrap.TestSchema;
import org.apache.directory.server.core.schema.bootstrap.TestSchemaNormalizerProducer;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.junit.BeforeClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.spring.Spring;

/**
 * Control an embedded LDAP Directory Server. The serever will pre-load test data for
 * the tests to run against. Since the directoryapp does not write data it handles
 * keeps a clean state for each test. 
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */


@OnApplicationStart
public class EmbeddedLdapServer extends Job {

	/** Test LDAP Configuration: **/
	private static final Resource LDAP_TEST_DATA = new ClassPathResource("ldap_test_data.ldif");
	private static final int LDAP_TEST_PORT = 1389;
	private static final String LDAP_TEST_PRINCIPAL = "uid=admin,ou=system";
	private static final String LDAP_TEST_CREDENTIALS = "secret";
	private static final String LDAP_WORKSPACE_DIR = "ldap_workspace/";

	/** The LDAP Server Context **/
	private DirContext dirContext;

	/**
	 * Start the embedded directory server, it is safe to call this multiple times.
	 */
	public void doJob() throws NamingException, IOException {

		if (dirContext != null)
			return;

		// Create a workspace for the server
		File workspaceDir = new File(System.getProperty("java.io.tmpdir") + LDAP_WORKSPACE_DIR);
		FileUtils.deleteQuietly(workspaceDir);
		Logger.info("Starting embedded directory server, workspace: "+workspaceDir);
		
		
		// Configure the server
		MutableServerStartupConfiguration cfg = new MutableServerStartupConfiguration();
		cfg.setWorkingDirectory(workspaceDir);

		cfg.setLdapPort(LDAP_TEST_PORT);
		Hashtable env = new Properties();
		env.put(Context.PROVIDER_URL, "");
		env.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.directory.server.jndi.ServerContextFactory");
		env.put(Context.SECURITY_PRINCIPAL, LDAP_TEST_PRINCIPAL);
		env.put(Context.SECURITY_CREDENTIALS, LDAP_TEST_CREDENTIALS);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		// Schema's, the test schema is included as sub project to the directoryapp.
		Set schemas = cfg.getBootstrapSchemas();
		schemas.add(new TestSchema());
		cfg.setBootstrapSchemas(schemas);

		// Configure the library partition. Partitions are where LDAP stores data.
		MutableBTreePartitionConfiguration partitionConfiguration = new MutableBTreePartitionConfiguration();
		partitionConfiguration.setSuffix("dc=library,dc=tamu,dc=edu");
		partitionConfiguration.setName("library");

		{ // from getRootPartitionAttributes
			BasicAttributes attributes = new BasicAttributes();
			BasicAttribute objectClassAttribute = new BasicAttribute(
					"objectClass");
			objectClassAttribute.add("top");
			objectClassAttribute.add("domain");
			objectClassAttribute.add("extensibleObject");
			attributes.put(objectClassAttribute);
			attributes.put("dc", "library");

			partitionConfiguration.setContextEntry(attributes);
		}

		cfg.setContextPartitionConfigurations(Collections
				.singleton(partitionConfiguration));

		// Start the server
		env.putAll(cfg.toJndiEnvironment());
		dirContext = new InitialDirContext(env);

		// Load an test ldiff data
		File tmpFile = File.createTempFile("ldap_test_data", ".ldif");
		try {
			InputStream is = LDAP_TEST_DATA.getInputStream();
			IOUtils.copy(is, new FileOutputStream(tmpFile));
			LdifFileLoader fileLoader = new LdifFileLoader(dirContext,
					tmpFile.getAbsolutePath());
			fileLoader.execute();
		} finally {
			tmpFile.delete();
		}
	}


	/**
	 * Just to be safe, let's cleanly close the directory server.
	 */
	protected void finalize() throws Throwable{
		try {
			if (dirContext != null)
				dirContext.close();
			dirContext = null;
		} finally {
			super.finalize();
		}
	}

}
