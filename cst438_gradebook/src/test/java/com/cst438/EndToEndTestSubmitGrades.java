package com.cst438;

import static org.assertj.core.api.Assertions.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.spel.ast.Assign;

/*
 * This example shows how to use selenium testing using the web driver
 * with Chrome browser.
 *
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 *
 *  In SpringBootTest environment, the test program may use Spring repositories to
 *  setup the database for the test and to verify the result.
 */

@SpringBootTest
public class EndToEndTestSubmitGrades {

    public static final String CHROME_DRIVER_FILE_LOCATION = "/Users/conlynpattison/Desktop/chromedriver";

    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.
    public static final String TEST_ASSIGNMENT_NAME = "db design";
    public static final String NEW_GRADE = "99";

    @Autowired
    public AssignmentRepository assignmentRepository;

    @Autowired
    public CourseRepository courseRepository;


    @Test
    public void addCourseTest() throws Exception {


        // set the driver location and start driver
        //@formatter:off
        // browser	property name 				Java Driver Class
        // edge 	webdriver.edge.driver 		EdgeDriver
        // FireFox 	webdriver.firefox.driver 	FirefoxDriver
        // IE 		webdriver.ie.driver 		InternetExplorerDriver
        //@formatter:on

        /*
         * initialize the WebDriver and get the home page.
         */

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        WebDriver driver = new ChromeDriver();
        // Puts an Implicit wait for 10 seconds before throwing exception
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);

        WebElement w;


        try {
            /*
             * locate the <td> element for assignment title 'db design'
             *
             */

            List<WebElement> elements = driver.findElements(By.xpath("//td"));
            boolean found = false;
            for (WebElement we : elements) {
                if (we.getText().equals(TEST_ASSIGNMENT_NAME)) {
                    found = true;
                    we.findElement(By.xpath("..//a")).click();
                    break;
                }
            }
            assertThat(found).withFailMessage("The test assignment was not found.").isTrue();

            /*
             *  Locate and click Grade button to indicate to grade this assignment.
             */

            Thread.sleep(SLEEP_DURATION);

            /*
             *  enter grades for all students, then click save.
             */
            ArrayList<String> originalGrades = new ArrayList<>();
            elements = driver.findElements(By.xpath("//input"));
            for (WebElement element : elements) {
                originalGrades.add(element.getAttribute("value"));
                element.clear();
                element.sendKeys(NEW_GRADE);
                Thread.sleep(SLEEP_DURATION);
            }

            for (String s : originalGrades) {
                System.out.println("'" + s + "'");
            }

            /*
             *  Locate submit button and click
             */
            driver.findElement(By.id("sgrade")).click();
            Thread.sleep(SLEEP_DURATION);

            w = driver.findElement(By.id("gmessage"));
            assertThat(w.getText()).withFailMessage("After saving grades, message should be \"Grades saved.\"").startsWith("Grades saved");

            driver.navigate().back();  // back button to last page
            Thread.sleep(SLEEP_DURATION);

            // find the assignment 'db design' again.
            elements = driver.findElements(By.xpath("//td"));
            found = false;
            for (WebElement we : elements) {
                if (we.getText().equals(TEST_ASSIGNMENT_NAME)) {
                    found = true;
                    we.findElement(By.xpath("..//a")).click();
                    break;
                }
            }
            Thread.sleep(SLEEP_DURATION);
            assertThat(found).withFailMessage("The test assignment was not found.").isTrue();

            // verify the grades. Change grades back to original values

            elements = driver.findElements(By.xpath("//input"));
            for (int idx = 0; idx < elements.size(); idx++) {
                WebElement element = elements.get(idx);
                assertThat(element.getAttribute("value")).withFailMessage("Incorrect grade value.").isEqualTo(NEW_GRADE);

                // clear the input value by backspacing over the value
                while (!element.getAttribute("value").equals("")) {
                    element.sendKeys(Keys.BACK_SPACE);
                }
                if (!originalGrades.get(idx).equals("")) element.sendKeys(originalGrades.get(idx));
                Thread.sleep(SLEEP_DURATION);
            }
            driver.findElement(By.id("sgrade")).click();
            Thread.sleep(SLEEP_DURATION);

            w = driver.findElement(By.id("gmessage"));
            assertThat(w.getText()).withFailMessage("After saving grades, message should be \"Grades saved.\"").startsWith("Grades saved");


        } catch (Exception ex) {
            throw ex;
        } finally {
            driver.quit();
        }

    }

    @Test
    public void addAssignmentTest() throws Exception {
        // init variables
        final String ASSIGNMENT_NAME = "test assignment name";

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        WebDriver driver = new ChromeDriver();
        // Puts an Implicit wait for 10 seconds before throwing exception
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // starting from home
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);

        try {
            // use the Link tag to change to the addAssignment page
            // a tag, href="/addAssignment"
            WebElement element = driver.findElement(By.id("add-assignment"));
            element.click();

            // assign each of the assignment inputs to final values
            driver.findElement(By.id("name-text")).sendKeys(ASSIGNMENT_NAME);
            driver.findElement(By.id("due-date")).sendKeys("10122024");

            WebElement courseIdElement = driver.findElement(By.id("course-id"));
            while (!courseIdElement.getAttribute("value").equals("")) {
                courseIdElement.sendKeys(Keys.BACK_SPACE);
            }
            courseIdElement.sendKeys(String.valueOf(31045));

            // submit the creation of the assignment
            driver.findElement(By.id("sgrade")).click();
            Thread.sleep(SLEEP_DURATION);

            // check message state
            String createMessage = driver.findElement(By.id("create-message")).getText();
            assertThat(createMessage)
                    .withFailMessage("Message should begin with \"Success\" after assignment creation")
                    .startsWith("Successfully created assignment with id: ");

            // change to the listAssignment ("/") page
            driver.navigate().back();
            Thread.sleep(SLEEP_DURATION * 5);

            // validate the creation of the new assignment
            boolean found = false;
            List<WebElement> elements = driver.findElements(By.xpath("//td"));
            for (WebElement we : elements) {
                if (we.getText().equals(ASSIGNMENT_NAME)) {
                    found = true;
                    break;
                }
            }

            assertThat(found)
                    .withFailMessage("Created assignment with name" +
                            ASSIGNMENT_NAME + "could not be found")
                    .isTrue();

            // get the id from the message
            int assignmentId = Integer.parseInt(createMessage.substring(41, createMessage.length() - 1));

            // TODO: why is this returning unexpected ids?
//            assignmentRepository.findAll().forEach((assignment) -> System.out.println(assignment.getId() + '\n'));
        } catch (Exception e) {
            throw e;
        } finally {
            driver.quit();
        }
    }

    @Test
    public void updateAssignmentTest() throws Exception {
        // TODO: create + run function to manually create a new assignment
        // init variables
        final String ASSIGNMENT_NAME = "update assignment name";

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        WebDriver driver = new ChromeDriver();
        // Puts an Implicit wait for 10 seconds before throwing exception
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // starting from home
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);

        // grab the created assignment's row and click its 'edit' a tag


        // save the original data for the assignment

        // grab the values for each of the inputs (name & due date)

        // change each of these values

        // click update button and validate message

        // TODO: reload page if possible, otherwise return here from home

        // validate the information has changed

        // change the information back to original data
    }

    @Test
    public void deleteAssignmentTest() throws Exception {
        // TODO: create + run function to manually create a new assignment
        // init variables

        // starting from home

        // grab the created assignment's row and click delete (no grades should exist yet)

        // check that we cannot find that row

        // refresh the page (to re-fetch the data from a mount)

        // again, check that we cannot find that row
    }

    private int createAssignment() {
        final int COURSE_ID = 31045;

        Course course = courseRepository.findById(COURSE_ID)
                .orElseThrow(() -> new RuntimeException(
                        "courseId " + COURSE_ID + " not found"));

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setName("created assignment");
        assignment.setDueDate(Date.valueOf("2024-10-12"));

        return assignmentRepository.save(assignment).getId();
    }
}
