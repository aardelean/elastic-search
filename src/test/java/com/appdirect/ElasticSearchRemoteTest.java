package com.appdirect;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.appdirect.model.User;
import com.appdirect.model.UserElasticSearchService;

/**
 * Created by alex.ardelean on 29/04/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticSearchRemoteConfiguration.class, ESApplicationTestConfiguration.class})
public class ElasticSearchRemoteTest {

	private static final Long USER_ID = 11l;
	private static final String BILLING_ID = "11211";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";

	@Autowired
	public UserElasticSearchService searchService;

	@Value("${elasticsearch.index}")
	private String index;

	@Value("${elasticsearch.collection}")
	private String collection;

	@Test
	public void searchRemote() throws ExecutionException, InterruptedException {

		User user = User.builder()
			.billingId(BILLING_ID)
			.firstName(FIRST_NAME)
			.lastName(LAST_NAME)
			.id(USER_ID)
			.build();

		searchService.saveIndex(user);

		List<Long> userId = searchService.searchByBillingId(BILLING_ID);
		Assert.assertEquals(userId.get(0), USER_ID);
	}
}
