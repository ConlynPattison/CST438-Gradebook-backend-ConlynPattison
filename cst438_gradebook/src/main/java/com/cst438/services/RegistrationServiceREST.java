package com.cst438.services;

import com.cst438.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(prefix = "registration", name = "service", havingValue = "rest")
@RestController
public class RegistrationServiceREST implements RegistrationService {


    RestTemplate restTemplate = new RestTemplate();

    @Value("${registration.url}")
    String registration_url;

    public RegistrationServiceREST() {
        System.out.println("REST registration service ");
    }

    @Override
    public void sendFinalGrades(int course_id, FinalGradeDTO[] grades) {
        //TODO use restTemplate to send final grades to registration service
        restTemplate.put(registration_url + "/course/" + course_id, grades);
    }

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;


    /*
     * endpoint used by registration service to add an enrollment to an existing
     * course.
     */
    @PostMapping("/enrollment")
    @Transactional
    public EnrollmentDTO addEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {

        // Receive message from registration service to enroll a student into a course.

        System.out.println("GradeBook addEnrollment " + enrollmentDTO);

        // Get course or throw
        Course course = courseRepository.findById(enrollmentDTO.courseId()).orElseThrow(() -> {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Course id " + enrollmentDTO.courseId() + " not found");
                }
        );

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudentName(enrollmentDTO.studentName());
        enrollment.setStudentEmail(enrollmentDTO.studentEmail());
        // TODO: should this id be decided by the other server's request?
        enrollment.setId(enrollmentDTO.id());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new EnrollmentDTO(
                savedEnrollment.getId(),
                savedEnrollment.getStudentEmail(),
                savedEnrollment.getStudentName(),
                savedEnrollment.getCourse().getCourse_id());

    }

}
