package com.cst438.services;


import com.cst438.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(prefix = "registration", name = "service", havingValue = "mq")
public class RegistrationServiceMQ implements RegistrationService {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public RegistrationServiceMQ() {
        System.out.println("MQ registration service ");
    }


    Queue registrationQueue = new Queue("registration-queue", true);

    @Bean
    Queue createQueue() {
        return new Queue("gradebook-queue");
    }

    /*
     * Receive message for student added to course
     */
    @RabbitListener(queues = "gradebook-queue")
    @Transactional
    public void receive(String message) {

        System.out.println("Gradebook has received: " + message);

        EnrollmentDTO enrollmentDTO = fromJsonString(message, EnrollmentDTO.class);

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

        enrollmentRepository.save(enrollment);
    }

    /*
     * Send final grades to Registration Service
     */
    @Override
    public void sendFinalGrades(int course_id, FinalGradeDTO[] grades) {

        System.out.println("Start sendFinalGrades " + course_id);

        //TODO convert grades to JSON string and send to registration service
        String finalGradeData = asJsonString(grades);
        rabbitTemplate.convertAndSend(registrationQueue.getName(), finalGradeData);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
