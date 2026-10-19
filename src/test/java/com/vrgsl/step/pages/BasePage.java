package com.vrgsl.step.pages;

import com.vrgsl.step.helper.ConfigHelper;
import com.vrgsl.step.helper.ElementHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ==============================================================================
 * BasePage (Page Object Model Temel Sınıfı)
 * ==============================================================================
 * Tüm Page sınıflarının miras aldığı çekirdek sınıftır.
 * 
 * Birleştirilmiş Güçlü Yetenekler:
 * 1. Harici JSON Element Havuzu Entegrasyonu: Elementleri anahtarlarıyla bulur (findVisibleElement("loginButton")).
 * 2. Efilli Shadow DOM & OneTrust Çerez Yönetimi: Standart Selenium'un göremediği Shadow DOM çerezlerini JS ile kapatır.
 * 3. Savunmacı Tıklama (safeClick): ElementClickInterceptedException durumunda otomatik JavaScript click uygular.
 * 4. İnsan Davranış Simülasyonu: Tuş vuruşları ve fare hareketleri için insani refleks gecikmeleri uygular.
 * 5. Dinamik Sekme ve Sayfa Yüklenme Senkronizasyonu.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor js;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int timeout = ConfigHelper.getInt("timeouts.explicitWait", 15);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
    }

    /**
     * Belirtilen URL'ye gider.
     */
    public void navigateTo(String url) {
        driver.get(url);
    }

    /**
     * DOM'da var olan elementi (presence) döner.
     */
    public WebElement findElement(By by) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public WebElement findElement(String elementKey) {
        return findElement(ElementHelper.getBy(elementKey));
    }

    /**
     * Sayfada görünür olan elementi (visibility) döner.
     */
    public WebElement findVisibleElement(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public WebElement findVisibleElement(String elementKey) {
        return findVisibleElement(ElementHelper.getBy(elementKey));
    }

    /**
     * Eşleşen tüm elementleri liste olarak döner.
     */
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    public List<WebElement> findElements(String elementKey) {
        return findElements(ElementHelper.getBy(elementKey));
    }

    /**
     * Element anahtarını alıp güvenli tıklama uygular.
     * Tıklama engellenirse JS click fallback'e düşer.
     */
    public void click(String elementKey) {
        By by = ElementHelper.getBy(elementKey);
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
            scrollToElement(element);
            element.click();
        } catch (ElementClickInterceptedException | TimeoutException | StaleElementReferenceException e) {
            try {
                WebElement fallbackElement = findElement(by);
                safeClick(fallbackElement);
            } catch (Exception ex) {
                throw new RuntimeException("❌ [" + elementKey + "] elementine tıklanamadı: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * WebElement referansına güvenli tıklama uygular.
     */
    public void click(WebElement element) {
        try {
            scrollToElement(element);
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            safeClick(element);
        }
    }

    /**
     * JavaScript yardımıyla tıklama (Safe Click Fallback).
     * Element başka bir popup/banner altında kalsa bile doğrudan tetikler.
     */
    public void safeClick(WebElement element) {
        scrollToElement(element);
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Elementin üzerine fareyle gelir (Hover).
     */
    public void hover(String elementKey) {
        WebElement element = findVisibleElement(elementKey);
        actions.moveToElement(element).perform();
    }

    /**
     * Metin giriş alanını temizleyip yazar.
     */
    public void typeText(String elementKey, String text) {
        WebElement element = findVisibleElement(elementKey);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Karakter karakter insani refleks simülasyonu ile yazar.
     */
    public void humanType(String elementKey, String text) {
        WebElement element = findVisibleElement(elementKey);
        element.click();
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(40 + (long) (Math.random() * 40));
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Elementin görünen metnini döner.
     */
    public String getText(String elementKey) {
        return findVisibleElement(elementKey).getText().trim();
    }

    /**
     * Elementin görünür olup olmadığını belirtilen süre içinde kontrol eder.
     */
    public boolean isElementVisible(String elementKey, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(ElementHelper.getBy(elementKey))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Elementi ekranın merkezine kaydırır (Smooth scroll).
     */
    public void scrollToElement(String elementKey) {
        WebElement element = findElement(elementKey);
        scrollToElement(element);
    }

    public void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * Yeni açılan sekmeye geçiş yapar.
     */
    public void switchToLastTab() {
        try {
            if (driver.getWindowHandles().size() == 1) {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                shortWait.until(d -> d.getWindowHandles().size() > 1);
            }
        } catch (Exception ignored) {}

        List<String> handleList = new ArrayList<>(driver.getWindowHandles());
        if (handleList.size() > 1) {
            String lastHandle = handleList.get(handleList.size() - 1);
            if (!driver.getWindowHandle().equals(lastHandle)) {
                driver.switchTo().window(lastHandle);
            }
        }
        waitForPageLoad();
        dismissCookiesIfPresent();
    }

    /**
     * Hepsiburada Efilli Shadow DOM ve OneTrust Çerez Bildirimlerini Kapatma:
     * Hepsiburada 'efilli-layout-dynamic' özel Web Component'i kullanır ve butonlar Shadow DOM içindedir.
     * Standart Selenium findElement bu butonları bulamaz; JavaScript querySelector('...').shadowRoot üzerinden
     * '#hb-accept-all' butonuna erişip tıklar.
     */
    public void dismissCookiesIfPresent() {
        try {
            js.executeScript(
                "try {\n" +
                "  let host = document.querySelector('efilli-layout-dynamic');\n" +
                "  if (host && host.shadowRoot) {\n" +
                "    let btn = host.shadowRoot.querySelector('#hb-accept-all') || host.shadowRoot.querySelector('#hb-popup-accept-all');\n" +
                "    if (btn) btn.click();\n" +
                "  }\n" +
                "  let ot = document.getElementById('onetrust-accept-btn-handler');\n" +
                "  if (ot) ot.click();\n" +
                "} catch(e){}"
            );
        } catch (Exception ignored) {}
    }

    /**
     * Sayfanın tam olarak yüklenmesini (document.readyState == 'complete') bekler.
     */
    public void waitForPageLoad() {
        try {
            wait.until(d -> js.executeScript("return document.readyState").equals("complete") 
                         || js.executeScript("return document.readyState").equals("interactive"));
        } catch (Exception ignored) {}
    }

    /**
     * Milisaniye bazlı güvenli bekleme.
     */
    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
