package com.cst438;

import com.cst438.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JunitTestAssignment {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private static MockHttpServletResponse response;

    @Test
    public void getAllAssignmentsForInstructor() throws Exception {
        // Perform the fetch all
        response = mvc.perform(MockMvcRequestBuilders
                .get("/assignment")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check the response status
        assertEquals(200, response.getStatus());

        // Check the assignmentDTO[] content
        AssignmentDTO[] resultContent = fromJsonString(response.getContentAsString(), AssignmentDTO[].class);
        assertEquals(resultContent[0],
                new AssignmentDTO(
                        1,
                        "db design",
                        "2021-09-01",
                        "CST 363 - Introduction to Database Systems",
                        31045
                ));
    }

    @Test
    public void getAssignment() throws Exception {
        // Perform the fetch single
        response = mvc.perform(MockMvcRequestBuilders
                .get("/assignment/1")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check the response status
        assertEquals(200, response.getStatus());

        // Check the assignmentDTO content
        assertEquals(fromJsonString(response.getContentAsString(), AssignmentDTO.class),
                new AssignmentDTO(
                        1,
                        "db design",
                        "2021-09-01",
                        "CST 363 - Introduction to Database Systems",
                        31045
                ));
    }

    @Test
    public void createAssignment() throws Exception {
        final AssignmentDTO assignmentDTO = new AssignmentDTO(
                999,
                "created assignment",
                "2023-09-19",
                "CST 363 - Introduction to Database Systems",
                31045
        );
        response = mvc.perform(MockMvcRequestBuilders
                .post("/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        // Check the response status
        assertEquals(200, response.getStatus());

        // Check the assignmentId content is not null (responded with the new assignment's id)
        assertNotNull(fromJsonString(response.getContentAsString(), Integer.class));
    }

    @Test
    public void updateAssignment() throws Exception {
        final int updateId = 2;

        // Perform the fetch single
        response = mvc.perform(MockMvcRequestBuilders
                .get("/assignment/" + updateId)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check the response status
        assertEquals(200, response.getStatus());

        // Check the assignmentDTO content
        assertEquals(fromJsonString(response.getContentAsString(), AssignmentDTO.class),
                // 1	2021-09-01	'db design'	31045
                new AssignmentDTO(
                        updateId,
                        "requirements",
                        "2021-09-02",
                        "CST 363 - Introduction to Database Systems",
                        31045
                ));
        // Update information
        final AssignmentDTO assignmentDTO = new AssignmentDTO(
                999, // DTO id will not be used TODO: Consider changing int->Integer
                "updated assignment",
                "2023-09-19",
                "CST 363 - Introduction to Database Systems",
                31045
        );
        response = mvc.perform(MockMvcRequestBuilders
                .put("/assignment/" + updateId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        // Check the response status
        assertEquals(200, response.getStatus());

        // Check that the update occurred
        Assignment assignment = assignmentRepository.findById(updateId)
                .orElseThrow(() -> new RuntimeException(
                        "Assignment not found for assignment id" + updateId
                ));
        assertEquals("updated assignment", assignment.getName());
    }

    @Test
    public void deleteAssignment() throws Exception {
        // Create a new Assignment to be deleted
        Course c = courseRepository.findById(31045).orElseThrow(() ->
                new RuntimeException("Course id " + 31045 + " not found")
        );
        Assignment a = new Assignment();
        a.setName("to be deleted");
        a.setDueDate(Date.valueOf("2021-09-01"));
        a.setCourse(c);

        int deleteId = assignmentRepository.save(a).getId();

        // Validate the existence of the Assignment
        assignmentRepository.findById(deleteId).orElseThrow(() ->
                new RuntimeException("Assignment id " + deleteId + " not found")
        );

        // Perform the delete by id
        response = mvc.perform(MockMvcRequestBuilders
                .delete("/assignment/" + deleteId)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Check response status
        assertEquals(200, response.getStatus());

        // Validate the Assignment no longer exists
        Optional<Assignment> assignmentOptional = assignmentRepository.findById(deleteId);
        assertTrue(assignmentOptional.isEmpty());
    }

    // TODO: Pull this out (being used in two different Test suites
    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Pull this out (being used in two different Test suites
    private static String asJsonString(final Object obj) {
        try {

            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
