package com.tenzin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tenzin.datatypes.State;
import com.tenzin.models.Address;


public interface AddressRepository extends JpaRepository<Address, Integer>{
	
	Optional<Address> findByState(State state);
}
