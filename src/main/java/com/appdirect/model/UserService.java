package com.appdirect.model;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserElasticSearchService elasticSearchService;

	@Autowired
	private UserRepository userRepository;

	public void save(String firstName, String lastName, String billingId) throws ExecutionException, InterruptedException {
		User user = User.builder()
			.billingId(billingId)
			.firstName(firstName)
			.lastName(lastName)
			.build();

		userRepository.save(user);
		elasticSearchService.saveIndex(user);
	}


	public List<User> searchByBillingId(String billingId) {
		List<Long> userIds = elasticSearchService.searchByBillingId(billingId);
		return userRepository.findUsersByIds(userIds);
	}
}
