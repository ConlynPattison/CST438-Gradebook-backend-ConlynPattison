package com.cst438.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends CrudRepository <Assignment, Integer> {

	@Query("select a from Assignment a where a.course.instructor= :email order by a.id")
	List<Assignment> findByEmail(@Param("email") String email);

	@Query("select a from Assignment a where a.course.instructor= :email and a.id= :id")
	Assignment findByEmailAndAssignmentId(@Param("email") String email, @Param("id") int id);
}
