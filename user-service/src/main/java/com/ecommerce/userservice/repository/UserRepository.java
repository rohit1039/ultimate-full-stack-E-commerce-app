package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByEmailID(String emailID);

}
