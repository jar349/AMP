package amp.bus.rabbit;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import amp.bus.rabbit.topology.Exchange;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.NullTrustManager;

public class OneWaySSLChannelFactory extends BasicChannelFactory {

	private static final String DEFAULT_SSL_PROTOCOL = "TLSv1";
	
    public OneWaySSLChannelFactory(String username, String password) {
		super(username, password);
   }

	@Override
	protected void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {
		
		super.configureConnectionFactory(factory, exchange);
		 
        try {
			factory.setSocketFactory(getPermissiveSSLContext().getSocketFactory());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private SSLContext getPermissiveSSLContext() throws GeneralSecurityException{
		SSLContext instance = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
		instance.init(null, new TrustManager[]{new NullTrustManager()}, null);
		return instance;
	}
}