package com.cst438.controllers;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@CrossOrigin
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @GetMapping("/assignment")
    public AssignmentDTO[] getAllAssignmentsForInstructor() {
        // get all assignments for this instructor
        String instructorEmail = "dwisneski@csumb.edu";  // user name (should be instructor's email)
        List<Assignment> assignments = assignmentRepository.findByEmail(instructorEmail);
        AssignmentDTO[] result = new AssignmentDTO[assignments.size()];
        for (int i = 0; i < assignments.size(); i++) {
            Assignment as = assignments.get(i);
            AssignmentDTO dto = new AssignmentDTO(
                    as.getId(),
                    as.getName(),
                    as.getDueDate().toString(),
                    as.getCourse().getTitle(),
                    as.getCourse().getCourse_id());
            result[i] = dto;
        }
        return result;
    }

    @PostMapping("/assignment")
    @Transactional
    public ResponseEntity<Void> createAssignment(@RequestBody Assignment assignment) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/assignment/{id}")
    @Transactional
    public ResponseEntity<Void> updateAssignment(@RequestBody Assignment assignment, @PathVariable("id") Integer assignmentId) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/assignment/{id}")
    @Transactional
    public ResponseEntity<Void> deleteAssignment(@PathVariable("id") Integer assignmentId) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // TODO create CRUD methods for Assignment
}
