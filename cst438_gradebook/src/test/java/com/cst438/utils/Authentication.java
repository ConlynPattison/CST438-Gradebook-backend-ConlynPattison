package com.cst438.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Service
public class Authentication {
    private static final String testUsername = "test";
    private static final String testPassword = "user";
    public void authenticateForTest(WebDriver driver) {
        driver.findElement(By.xpath("//input[@name='username']"))
                .sendKeys(testUsername);
        driver.findElement(By.xpath("//input[@name='password']"))
                .sendKeys(testPassword);
        driver.findElement(By.id("login-submit")).click();
    }
}
