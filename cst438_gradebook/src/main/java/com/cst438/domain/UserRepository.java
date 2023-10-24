package com.cst438.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository <User, Integer> {

    @Query("select a from User a where a.username= :username")
    User findByUsername(@Param("username") String username);
}
