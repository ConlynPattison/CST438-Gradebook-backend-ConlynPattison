package com.cst438;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.utils.Authentication;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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

    public static final String CHROME_DRIVER_FILE_LOCATION = "C:\\Users\\conly\\OneDrive\\Desktop\\chromedriver.exe";

    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.
    public static final String TEST_ASSIGNMENT_NAME = "db design";
    public static final String NEW_GRADE = "99";

    @Autowired
    public AssignmentRepository assignmentRepository;

    @Autowired
    public CourseRepository courseRepository;

    @Autowired
    public Authentication authentication;


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
            authentication.authenticateForTest(driver);
            Thread.sleep(SLEEP_DURATION);

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
            authentication.authenticateForTest(driver);

            Thread.sleep(SLEEP_DURATION);

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
        // init variables
        String NEW_NAME = "changed assignment";

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        WebDriver driver = new ChromeDriver();
        // Puts an Implicit wait for 10 seconds before throwing exception
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // starting from home
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);

        try {
//            createAssignment();
//            Thread.sleep(SLEEP_DURATION);
//            driver.navigate().refresh();
            authentication.authenticateForTest(driver);
            Thread.sleep(SLEEP_DURATION);

            // grab the created assignment's row and click its 'edit' a tag
            WebElement editLink = driver.findElement(By.xpath("//tr[td[text()='db design']][1]/td/a[text()='Edit']"));
            editLink.click();
            Thread.sleep(SLEEP_DURATION);

            // save the original data for the assignment
            WebElement nameElement = driver.findElement(By.xpath("//input[@name='name']"));

            String originalName = nameElement.getAttribute("value");

            // grab the values for each of the inputs (name & due date)
            while (!nameElement.getAttribute("value").equals("")) {
                nameElement.sendKeys(Keys.BACK_SPACE);
            }
            nameElement.sendKeys(NEW_NAME);

            // click update button and validate message
            driver.findElement(By.id("sgrade")).click();
            Thread.sleep(SLEEP_DURATION);

            assertThat(driver.findElement(By.xpath("//h4")).getText())
                    .withFailMessage("Message should announce success state")
                    .startsWith("Successfully updated course");

            // TODO: reload page if possible, otherwise return here from home
            driver.navigate().refresh();
            Thread.sleep(SLEEP_DURATION);

            // validate the information has changed
            nameElement = driver.findElement(By.xpath("//input[@name='name']"));
            assertThat(nameElement.getAttribute("value"))
                    .withFailMessage("Changed name was not saved")
                    .isEqualTo(NEW_NAME);

            // change the information back to original data
            while (!nameElement.getAttribute("value").equals("")) {
                nameElement.sendKeys(Keys.BACK_SPACE);
            }
            nameElement.sendKeys(originalName);
            driver.findElement(By.id("sgrade")).click();

        } catch (Exception e) {
            throw e;
        } finally {
            driver.close();
        }
    }

    @Test
    public void deleteAssignmentTest() throws Exception {
        // init variables
        String DELETE_NAME = "requirements";

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        WebDriver driver = new ChromeDriver();
        // Puts an Implicit wait for 10 seconds before throwing exception
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // starting from home
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);

        try {
            authentication.authenticateForTest(driver);
            Thread.sleep(SLEEP_DURATION);

            driver.findElement(By.id("force")).click();

            // grab the created assignment's row and click delete (no grades should exist yet)
            WebElement deleteButton = driver.findElement(By.xpath("//tr[td[text()='" + DELETE_NAME + "']][1]/td/button"));
            deleteButton.click();

            Thread.sleep(SLEEP_DURATION);

            // check that we cannot find that row
            List<WebElement> foundElements = driver.findElements(By.xpath("//tr[td[text()='" + DELETE_NAME + "']]"));
            assertThat(foundElements)
                    .withFailMessage("Should not have found deleted element")
                    .isNullOrEmpty();

            // refresh the page (to re-fetch the data from a mount)
            driver.navigate().refresh();
            Thread.sleep(SLEEP_DURATION);

            // again, check that we cannot find that row
            List<WebElement> foundElementsRefresh = driver.findElements(By.xpath("//tr[td[text()='" + DELETE_NAME + "']]"));
            assertThat(foundElementsRefresh)
                    .withFailMessage("Should not have found deleted element")
                    .isNullOrEmpty();

        } catch (Exception e) {
            throw e;
        } finally {
            driver.close();
        }
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
