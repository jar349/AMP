package amp.topology.core.repo.rmq;

import java.util.ArrayList;
import java.util.Collection;

import rabbitmq.httpclient.BasicAuthHttpClientProvider;
import rabbitmq.httpclient.HttpClientProvider;
import rabbitmq.httpclient.SslWithBasicAuthHttpClientProvider;
import rabbitmq.mgmt.RabbitMgmtService;
import amp.topology.core.model.Cluster;
import amp.topology.core.model.ClusterRepository;
import amp.topology.core.model.ManagementEndpoint;

/**
 * Factory for retrieving the management interface to RabbitMQ.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class RmqManagementFactory {

	ClusterRepository clusterRepository;
	
	public RmqManagementFactory(ClusterRepository clusterRepository){
		
		this.clusterRepository = clusterRepository;
	}
	
	/**
	 * Get a collection of RabbitMQ Management Instances.
	 * 
	 * @param clusterId ID of the cluster.
	 * @return A collection of Management Instances registered to the cluster.
	 */
	public Collection<RabbitMgmtService> getInstances(String clusterId){
		
		Cluster cluster = this.clusterRepository.get(clusterId);
		
		ArrayList<RabbitMgmtService> mgmtServices = new ArrayList<RabbitMgmtService>();
		
		for (ManagementEndpoint mgmt : cluster.getManagementEndpoints()){
			
			mgmtServices.add( getService( mgmt ) );
		}
		
		return mgmtServices;
	}
	
	/**
	 * Get an instance of the Management service from an individual endpoint.
	 * @param endpoint Endpoint of the RabbitMQ Management Console.
	 * @return Management Service.
	 */
	RabbitMgmtService getService(ManagementEndpoint endpoint){
		
		HttpClientProvider provider = null;
		
		if (endpoint.getKeystore() != null){
			
			provider = new SslWithBasicAuthHttpClientProvider(
				endpoint.getKeystore(), endpoint.getKeystorePassword(), 
				endpoint.getTruststore(), endpoint.getTruststorePassword(), 
				endpoint.getUsername(), endpoint.getPassword());
			
		} else {
			
			provider = new BasicAuthHttpClientProvider(
				endpoint.getUsername(), endpoint.getPassword());
		}
		
		return new RabbitMgmtService(
			endpoint.getHostname(), endpoint.getPort(), provider).initialize();
	}
}