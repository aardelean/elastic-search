package com.appdirect.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class User {
	@Id
	@GeneratedValue
	private Long id;
	private String firstName;
	private String lastName;
	private String billingId;

	@Builder(builderClassName = "UserBuilder")
	public User(Long id, String billingId, String firstName, String lastName) {
		this.billingId = billingId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public User(){}
}
