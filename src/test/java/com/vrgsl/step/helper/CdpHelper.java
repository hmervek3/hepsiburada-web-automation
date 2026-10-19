package com.vrgsl.step.helper;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Random;

/**
 * ==============================================================================
 * CdpHelper (Anti-Bot & Human Simulation Assistant)
 * ==============================================================================
 * Hepsiburada Akamai bot tespit sistemine (N1E2 hatasına) takılmamak için
 * doğal insan yazma ve tıklama reflekslerini simüle eder.
 */
public class CdpHelper {

    private static final Random random = new Random();

    /**
     * Güvenli ve doğal tıklama gerçekleştirir.
     */
    public static void humanClick(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Elementi odaklar, temizler ve metni insan hızında karakter karakter yazar.
     */
    public static void humanType(WebDriver driver, WebElement element, String text) {
        try {
            element.click();
        } catch (Exception ignored) {}
        pause(150 + random.nextInt(100));
        element.clear();

        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            pause(40 + (long) (Math.random() * 40));
        }
    }

    /**
     * Güvenli milisaniye bazlı bekleme yardımcı metodu.
     */
    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}

