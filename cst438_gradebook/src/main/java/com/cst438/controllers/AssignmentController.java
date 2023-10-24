package com.cst438.controllers;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.security.Principal;
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

    @Autowired
    UserRepository userRepository;

    // FIXME: private final String instructorEmail = "dwisneski@csumb.edu";

    @GetMapping("/assignment")
    public AssignmentDTO[] getAllAssignmentsForInstructor(Principal principal) {
        // get all assignments for this instructor
        String instructorEmail = getInstructorEmail(principal.getName());
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
    public AssignmentDTO getAssignment(Principal principal,
                                       @PathVariable("id") Integer id) {
        Assignment assignment = findAssignmentById(id);
        Course course = assignment.getCourse();
        String instructorEmail = getInstructorEmail(principal.getName());

        if (!isAuthorized(id, instructorEmail))
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
    public int createAssignment(Principal principal,
                                @RequestBody AssignmentDTO assignmentDTO) {
        Course course = safeFindCourse(assignmentDTO);
        String instructorEmail = getInstructorEmail(principal.getName());

        if (!isAuthorized(course, instructorEmail))
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
    public void updateAssignment(Principal principal,
                                 @RequestBody AssignmentDTO assignmentDTO,
                                 @PathVariable("id") Integer assignmentId) {
        Assignment assignment = findAssignmentById(assignmentId);
        Course course = safeFindCourse(assignmentDTO);

        String instructorEmail = getInstructorEmail(principal.getName());

        if (!isAuthorized(assignmentId, instructorEmail))
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
    public void deleteAssignment(Principal principal, @PathVariable("id") Integer assignmentId,
                                 @RequestParam(name = "force", required = false) Boolean force) {
        Assignment assignment = findAssignmentById(assignmentId);
        String instructorEmail = getInstructorEmail(principal.getName());

        if (!isAuthorized(assignment.getId(), instructorEmail))
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

    private boolean isAuthorized(int assignmentId, String instructorEmail) {
        return assignmentRepository.findByEmailAndAssignmentId(instructorEmail, assignmentId) != null;
    }

    private boolean isAuthorized(Course course, String instructorEmail) {
        return course.getInstructor().equals(instructorEmail);
    }

    private String getInstructorEmail(String username) {
        return userRepository.findByUsername(username).getEmail();
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
        return course;
    }
}
