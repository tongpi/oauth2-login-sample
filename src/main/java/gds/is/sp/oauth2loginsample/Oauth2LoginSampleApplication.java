package gds.is.sp.oauth2loginsample;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Oauth2LoginSampleApplication {

	public static void main(String[] args) throws ServletException {
		disableHostNameVerify();		
		SpringApplication.run(Oauth2LoginSampleApplication.class, args);
	}

	private static void disableHostNameVerify() throws ServletException {
		try {

			SSLContext sc;

			// Get SSL context
			sc = SSLContext.getInstance("SSL");

			// Create empty HostnameVerifier
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			// SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

			// HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
			SSLContext.setDefault(sc);
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
