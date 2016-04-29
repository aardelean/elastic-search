package com.appdirect;

import java.io.File;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@Configuration
public class ElasticSearchEmbeddedConfiguration {

	@Value("${elasticsearch.cluster.name}")
	private String clusterName;
	@Value("${elasticsearch.timeout}")
	private Long timeout;

	@Bean(destroyMethod = "close")
	public Node node() {
		return NodeBuilder
			.nodeBuilder()
			.clusterName(clusterName)
			.local(true)
			.settings(
				Settings
					.builder()
					.put("index.store.fs.memory.enabled", "true")
					.put("path.home", tempDir().toString())
			)
			.node();
	}
	@Bean(name = "elasticsearch")
	public ObjectMapper objectMapper() {
		return new ObjectMapper().registerModule(new Jdk8Module());
	}

	@Bean(destroyMethod = "delete")
	public TempDirectory tempDir() {
		return new TempDirectory();
	}

	@Bean
	public AdminClient adminClient(Client client) {
		return client.admin();
	}

	@Bean
	public TimeValue timeoutValue(){
		return TimeValue.timeValueSeconds(timeout);
	}

	@Bean
	public ElasticSearchBootstrapper bootstrapper(){
		return new ElasticSearchBootstrapper();
	}

	@Bean
	public Client client() {
		return node().client();
	}

	private class TempDirectory {
		private File tempDir;

		public TempDirectory() {
			this.tempDir = com.google.common.io.Files.createTempDir();
		}

		public void delete() {
			FileSystemUtils.deleteRecursively(this.tempDir);
		}

		@Override
		public String toString() {
			return this.tempDir.toString();
		}
	}
}
