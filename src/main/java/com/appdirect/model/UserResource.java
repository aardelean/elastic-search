package com.appdirect.model;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by alex.ardelean on 27/04/16.
 */
@RestController
@RequestMapping("/resources")
public class UserResource {
	@Autowired
	private UserService userService;

	@RequestMapping(path = "", method = RequestMethod.POST)
	public void save(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
									 @RequestParam("billingId") String billingId) throws ExecutionException, InterruptedException {
		userService.save(firstName, lastName, billingId);
	}

	@RequestMapping(path = "/billingId/{id}", method = RequestMethod.GET)
	public List<User> search(@PathVariable("id") String billingId){
		return userService.searchByBillingId(billingId);
	}
}
