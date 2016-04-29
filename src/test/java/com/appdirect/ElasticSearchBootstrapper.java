package com.appdirect;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.Classes;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@Slf4j
public class ElasticSearchBootstrapper {
	// Index settings location (classpath resource)
	private static final String INDEX_SETTINGS_LOCATION = "elasticsearch/index-settings.json";
	private static final String INDEX_MAPPINGS_LOCATION = "elasticsearch/index-mappings.json";
	private static final String INDEX_TEMPLATES_LOCATION = "elasticsearch/index-templates.json";

	@Value("${elasticsearch.index}")
	private String index;

	@Autowired
	private TimeValue timeoutValue;

	@Value("${elasticsearch.collection}")
	private String collection;

	@Value("${elasticsearch.cluster.name}")
	private String clusterName;

	@Autowired
	private AdminClient adminClient;

	private ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

	@PostConstruct
	public void init() {
		bootstrap();
	}

	public void bootstrap() {
			log.info("Connecting to Elasticsearch cluster '{}' ...", clusterName);

			if (!indexExists()) {
				log.debug("Index '{}' does not exist, creating", index);
				createIndex();
			}
	}


	/**
	 * Check if the index exists.
	 */
	private boolean indexExists() {
		return new IndicesExistsRequestBuilder(adminClient.indices(), IndicesExistsAction.INSTANCE)
			.setIndices(index)
			.get(timeoutValue)
			.isExists();
	}



	/**
	 * Create the index and all associated mappings (collections).
	 */
	private void createIndex() {
		createIndexTemplates();
		createIndexSettings();
		createIndexMappings();
	}

	/**
	 * Create all associated mappings (collections). Returns collection names (mapping types).
	 */
	private void createIndexMappings() {
		final JsonNode mappings = loadFromClasspath(INDEX_MAPPINGS_LOCATION);

		mappings.get("mappings").fields().forEachRemaining(entry -> {
			final PutMappingResponse response = new PutMappingRequestBuilder(adminClient.indices(), PutMappingAction.INSTANCE)
				.setIndices(index)
				.setTimeout(timeoutValue)
				.setSource(entry.getValue().toString())
				.setType(entry.getKey())
				.get();

			assertAcknowledged(response);
		});
	}

	/**
	 * Create new index from the settings provided.
	 */
	private void createIndexSettings() {
		final Settings settings = Settings.builder()
				.loadFromStream(INDEX_SETTINGS_LOCATION, getDefaultClassLoader().getResourceAsStream(INDEX_SETTINGS_LOCATION))
				.build();

		final CreateIndexResponse response = new CreateIndexRequestBuilder(adminClient.indices(), CreateIndexAction.INSTANCE)
			.setIndex(index)
			.setTimeout(timeoutValue)
			.setSettings(settings)
			.get();

		assertAcknowledged(response);
	}

	/**
	 * Create new index templates
	 */
	private void createIndexTemplates() {
		final JsonNode templates = loadFromClasspath(INDEX_TEMPLATES_LOCATION);

		templates.fields().forEachRemaining(entry -> {
			final PutIndexTemplateResponse response = new PutIndexTemplateRequestBuilder(adminClient.indices(), PutIndexTemplateAction.INSTANCE, entry.getKey())
				.setSource(entry.getValue().toString())
				.get(timeoutValue);

			assertAcknowledged(response);
		});
	}

	/**
	 * Loads the JSON document (settings or mappings) from the classpath.
	 */
	private JsonNode loadFromClasspath(String resourceName) {
		final InputStream is = getDefaultClassLoader().getResourceAsStream(resourceName);

		 if (is == null) {
			 throw new RuntimeException("Unable to locate JSON resource from classpath location:" + resourceName);
		 }

		 try {
			 return mapper.readTree(is);
		 } catch (final IOException ex) {
			 throw new RuntimeException("Unable to read JSON resource from classpath location:" + resourceName, ex);
		 }
	}

	private void assertAcknowledged(final AcknowledgedResponse response) {
		if (!response.isAcknowledged()) {
			throw new RuntimeException("Response is not acknowledged: " + response);
		}
	}

	/**
	 * Return the default ClassLoader to use: typically the thread context
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
	 * class will be used as fallback.
	 * <p/>
	 * <p>Call this method if you intend to use the thread context ClassLoader
	 * in a scenario where you absolutely need a non-null ClassLoader reference:
	 * for example, for class path resource loading (but not necessarily for
	 * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
	 * reference as well).
	 *
	 * @return the default ClassLoader (never <code>null</code>)
	 * @see Thread#getContextClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			 return Classes.class.getClassLoader();
		}
		return cl;
	}
}
