package com.appdirect.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.id in ?1")
	List<User> findUsersByIds(List<Long> ids);
}
