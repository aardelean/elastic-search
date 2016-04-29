package com.appdirect;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * Created by alex.ardelean on 29/04/16.
 */
@Configuration
public class ElasticSearchRemoteConfiguration {

	@Value("${elasticsearch.cluster.name}")
	private String clusterName;
	@Value("${elasticsearch.remote.host}")
	private String host;
	@Value("${elasticsearch.remote.port}")
	private Integer port;
	@Value("${elasticsearch.timeout}")
	private Long timeout;


	@Bean
	public Client client() throws UnknownHostException {
		InetAddress address = InetAddress.getByName(host);
		return new TransportClient.Builder().build()
			.addTransportAddress(new InetSocketTransportAddress(address, port));
	}

	@Bean(name = "elasticsearch")
	public ObjectMapper objectMapper() {
		return new ObjectMapper().registerModule(new Jdk8Module());
	}

	@Bean
	public AdminClient adminClient(Client client){
		return client.admin();
	}

	@Bean
	public TimeValue timeoutValue(){
		return TimeValue.timeValueSeconds(timeout);
	}
}
