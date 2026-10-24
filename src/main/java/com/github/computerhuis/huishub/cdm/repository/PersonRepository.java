package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.enumeration.PersonRoleType;
import com.github.computerhuis.huishub.cdm.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<Person> findByFirstNameAndLastName(String lastName, String firstName);

    List<Person> findByFirstNameLikeOrLastNameLike(String firstName, String lastName);

    List<Person> findByFirstNameLike(String firstName);

    List<Person> findByLastNameLike(String firstName);

    List<Person> findByFirstName(String firstName);

    List<Person> findByLastName(String lastName);

    List<Person> findByRoles(PersonRoleType role);

    List<Person> findByRolesAndPasshashIsNotNullOrderByFirstNameAsc(PersonRoleType role);
}
