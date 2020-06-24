package com.unw.repo;

import com.unw.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PersonRepository extends JpaRepository<Person, Long> {

  Person findByName(String name);

}
