package com.cst438.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Service
public class Authentication {
    private static final String testUsername = "test";
    private static final String testPassword = "user";
    public static final String authJWT = "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjk4MjkwNjQxfQ.kEDmAieWzfeNqB2fNwlAEJRW2ynP8mBNsXHJetKmBOZEu5c7A0RYC2scF9DCfQfX";

    public void authenticateForTest(WebDriver driver) {
        driver.findElement(By.xpath("//input[@name='username']"))
                .sendKeys(testUsername);
        driver.findElement(By.xpath("//input[@name='password']"))
                .sendKeys(testPassword);
        driver.findElement(By.id("login-submit")).click();
    }
}
