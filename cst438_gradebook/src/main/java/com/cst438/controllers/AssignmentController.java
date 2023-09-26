package com.cst438.controllers;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.sql.Date;
import java.util.List;

@RestController
@CrossOrigin
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    AssignmentGradeRepository assignmentGradeRepository;

    private final String instructorEmail = "dwisneski@csumb.edu";

    @GetMapping("/assignment")
    public AssignmentDTO[] getAllAssignmentsForInstructor() {
        // get all assignments for this instructor
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

    @GetMapping("/assignment/{id}")
    public AssignmentDTO getAssignment(@PathVariable("id") Integer id) {
        Assignment assignment = findAssignmentById(id);
        Course course = assignment.getCourse();

        if (!isAuthorized(id))
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User " +
                            instructorEmail +
                            " is not authorized to access assignment id " +
                            assignment.getCourse().getCourse_id());

        return new AssignmentDTO(
                assignment.getId(),
                assignment.getName(),
                assignment.getDueDate().toString(),
                course.getTitle(),
                course.getCourse_id());
    }

    @PostMapping("/assignment")
    @Transactional
    public int createAssignment(@RequestBody AssignmentDTO assignmentDTO) {
        Course course = safeFindCourse(assignmentDTO);

        if (!isAuthorized(course))
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User " +
                            instructorEmail +
                            " is not authorized to create assignment under " +
                            course.getInstructor());

        Assignment assignment = new Assignment();
        assignment.setName(assignmentDTO.assignmentName());
        assignment.setDueDate(Date.valueOf(assignmentDTO.dueDate()));
        assignment.setCourse(course);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return savedAssignment.getId();
    }

    @PutMapping("/assignment/{id}")
    @Transactional
    public void updateAssignment(@RequestBody AssignmentDTO assignmentDTO,
                                 @PathVariable("id") Integer assignmentId) {
        Assignment assignment = findAssignmentById(assignmentId);
        Course course = safeFindCourse(assignmentDTO);

        if (!isAuthorized(assignmentId))
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User " +
                            instructorEmail +
                            " is not authorized to update assignment id " +
                            assignment.getCourse().getCourse_id());

        assignment.setCourse(course);
        assignment.setDueDate(Date.valueOf(assignmentDTO.dueDate()));
        assignment.setName(assignmentDTO.assignmentName());
        assignmentRepository.save(assignment);
    }

    @DeleteMapping("/assignment/{id}")
    @Transactional
    public void deleteAssignment(@PathVariable("id") Integer assignmentId,
                                 @RequestParam(name = "force", required = false) Boolean force) {
        Assignment assignment = findAssignmentById(assignmentId);
        if (!isAuthorized(assignment.getId()))
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User " +
                            instructorEmail +
                            " is not authorized to delete assignment id " +
                            assignment.getCourse().getCourse_id());

        // Check if the assignment has grades
        boolean hasGrades = false;
        List<Enrollment> students = assignment.getCourse().getEnrollments();
        for (Enrollment student : students) {
            // does student have a grade for this assignment
            AssignmentGrade ag = assignmentGradeRepository.findByAssignmentIdAndStudentEmail(assignmentId, student.getStudentEmail());
            if (ag != null) {
                hasGrades = true;
                break;
            }
        }

        if (!hasGrades || force != null && force)
            assignmentRepository.delete(assignment);
        else {
            System.out.println("WARNING: Assignment id " +
                    assignmentId +
                    " delete attempted with Grades saved\n" +
                    "use ?force=true option to continue operation");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "WARNING: Assignment id " +
                            assignmentId +
                            " delete attempted with Grades saved\n" +
                            "use ?force=true option to continue operation");
        }
    }

    private boolean isAuthorized(int assignmentId) {
        return assignmentRepository.findByEmailAndAssignmentId(instructorEmail, assignmentId) != null;
    }

    private boolean isAuthorized(Course course) {
        return course.getInstructor().equals(instructorEmail);
    }

    private Assignment findAssignmentById(Integer assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid assignment primary key " + assignmentId
                ));
    }

    private Course safeFindCourse(AssignmentDTO assignmentDTO) {
        Course course = courseRepository.findById(assignmentDTO.courseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid course primary key " + assignmentDTO.courseId()
                ));
        // Note: Removed check for matching course id to title for easier creation/update
        /*
        if (!course.getTitle().equals(assignmentDTO.courseTitle()))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid course title " +
                            assignmentDTO.courseTitle() +
                            " for course primary key " +
                            assignmentDTO.courseId()
            );
         */
        return course;
    }
}
