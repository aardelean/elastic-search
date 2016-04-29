package com.appdirect;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.appdirect.model.User;
import com.appdirect.model.UserElasticSearchService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticSearchEmbeddedConfiguration.class, ESApplicationTestConfiguration.class})
public class ElasticSearchTest {

	private static final Long USER_ID = 11l;
	private static final String BILLING_ID = "11211";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";

	@Autowired
	public UserElasticSearchService searchService;

	@Test
	public void storeAndSearch() throws ExecutionException, InterruptedException {
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
