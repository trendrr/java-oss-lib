/**
 * 
 */
package com.trendrr.oss;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * 
 * Builder to get to the SSLContext. 
 * 
 * To create a self signed server cert:
 * http://www.sslshopper.com/article-most-common-java-keytool-keystore-commands.html
 * 
 * keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 1360 -keysize 2048
 * 
 * 
 * @author Dustin Norlander
 * @created Feb 15, 2011
 * 
 */
public class SSLContextBuilder {

	protected static Log log = LogFactory.getLog(SSLContextBuilder.class);
	
	private String certificatePassword = null;
	private String keystorePassword = null;
	
	private InputStream stream = null;
	
	private String protocol = "TLS";
	private KeyStore ks;
	private String algorithm = null;
	private TrustManager tm[] = null;
	private SecureRandom random = null;
	
	private boolean client = false;
	
	/**
	 * New builder instance.  
	 * 
	 * set client to true if this is clientside, false for serverside
	 * 
	 * @param client
	 */
	public SSLContextBuilder(boolean client) {
		//set sensible defaults
		try {
			this.ks = KeyStore.getInstance("JKS");
		} catch (KeyStoreException e) {
			log.error("Caught", e);
		}
		algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        this.random = new SecureRandom();
	}
	
	public SSLContext toSSLContext() throws Exception {
		try {
			KeyManager km[] = null;
			SSLContext context = SSLContext.getInstance(this.protocol);
			if (this.stream != null) {
			    ks.load(this.stream,
			    		this.keystorePassword.toCharArray());
			    // Set up key manager factory to use our key store
			    KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			    kmf.init(ks, this.certificatePassword.toCharArray());
			    km = kmf.getKeyManagers();
			}
			context.init(km, this.getTrustManager(), this.random);
		    return context;
		} catch (Exception e) {
		    throw new Exception(
		            "Failed to initialize the server-side SSLContext", e);
		} finally {
			if (this.stream !=null) {
				try {this.stream.close();} catch (Exception x){}
			}
		}
	}
	
	
	
	private TrustManager[] getTrustManager() {
		if (this.tm != null)
			return this.tm;
		
		if (this.client)
	      {
	         // we are in client mode and do not want to perform server cert
	         // authentication
	         // return a trust manager that trusts all certs
	         return new TrustManager[] { new X509TrustManager() 
	         {
	            public void checkClientTrusted(final X509Certificate[] chain, final String authType)
	            {
	            }

	            public void checkServerTrusted(final X509Certificate[] chain, final String authType)
	            {
	            }

	            public X509Certificate[] getAcceptedIssuers()
	            {
	               return null;
	            }
	         } };
	      }
//
//	      else
//	      {
//	         TrustManagerFactory trustMgrFactory;
//	         KeyStore trustStore = SSLSupport.loadKeystore(trustStorePath, trustStorePassword);
//	         trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//	         trustMgrFactory.init(trustStore);
//	         return trustMgrFactory.getTrustManagers();
//	      }
		
		
		return null;
		
	}
	
	/**
	 * sets the SecureRandom. 
	 * @param rand
	 * @return
	 */
	public SSLContextBuilder secureRandom(SecureRandom rand) {
		this.random = rand;
		return this;
	}
	
	/**
	 * set the TrustManager. Believe this is only used for clients validating server certs
	 * @param tm
	 * @return
	 */
	public SSLContextBuilder trustManager(TrustManager[] tm) {
		this.tm = tm;
		return this;
	}
	
	public SSLContextBuilder certificatePassword(String password) {
		this.certificatePassword = password;
		return this;
	}
	
	public SSLContextBuilder keystorePassword(String password) {
		this.keystorePassword = password;
		return this;
	}
	
	/**
	 * sets the protocol.  defaults to "TLS" 
	 * @param protocol
	 * @return
	 */
	public SSLContextBuilder protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	public SSLContextBuilder keystoreFilename(String filename) {
		try {
			this.stream = FileHelper.fileStream(filename);
		} catch (Exception e) {
			log.error("Caught", e);
		}
		return this;
	}
	
	public SSLContextBuilder keystoreBytes(byte[] bytes) {
		try {
			this.stream = new ByteArrayInputStream(bytes);
		} catch (Exception e) {
			log.error("Caught", e);
		}
		return this;
	}
	
	public SSLContextBuilder keystoreStream(InputStream stream) {
		this.stream = stream;
		return this;
	}
	
	public SSLContextBuilder keystore(KeyStore ks) {
		this.ks = ks;
		return this;
	}
}
