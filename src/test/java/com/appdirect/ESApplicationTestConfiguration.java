package com.appdirect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.appdirect.model.UserElasticSearchService;

/**
 * Created by alex.ardelean on 28/04/16.
 */
@Configuration
@Import(ElasticSearchEmbeddedConfiguration.class)
@PropertySource("classpath:test-application.properties")
public class ESApplicationTestConfiguration {
	@Bean
	public UserElasticSearchService elasticSearchService(){
		return new UserElasticSearchService();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
		return new PropertySourcesPlaceholderConfigurer();
	}

}
