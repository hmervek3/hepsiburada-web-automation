package com.vrgsl.step.steps;

import com.thoughtworks.gauge.Step;
import com.vrgsl.step.driver.DriverManager;
import com.vrgsl.step.helper.ConfigHelper;
import com.vrgsl.step.helper.ElementHelper;
import com.vrgsl.step.helper.StoreHelper;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ==============================================================================
 * BaseSteps (Genel Amaçlı Test Adımları Kütüphanesi)
 * ==============================================================================
 * Web otomasyonunda sıkça ihtiyaç duyulan genel eylemleri (tıklama, yazma, scroll,
 * hover, alert yönetimi, sekme geçişleri vb.) iki dilli (Türkçe & İngilizce) Gauge
 * adımları olarak hazır sunar.
 */
public class BaseSteps {

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait getDefaultWait() {
        int timeout = ConfigHelper.getInt("timeouts.explicitWait", 15);
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeout));
    }

    protected WebDriverWait getWait(int seconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(seconds));
    }

    protected Actions getActions() {
        return new Actions(getDriver());
    }

    protected JavascriptExecutor getJs() {
        return (JavascriptExecutor) getDriver();
    }

    protected WebElement findElement(By by) {
        return getDefaultWait().until(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected WebElement findVisibleElement(By by) {
        return getDefaultWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    // ==========================================
    // Temel Tıklama & Etkileşim Adımları
    // ==========================================

    @Step({"Click element <key>", "<key> elementine tıkla"})
    public void clickElement(String key) {
        By by = ElementHelper.getBy(key);
        try {
            WebElement element = getDefaultWait().until(ExpectedConditions.elementToBeClickable(by));
            element.click();
        } catch (Exception e) {
            WebElement element = findElement(by);
            getJs().executeScript("arguments[0].click();", element);
        }
    }

    @Step({"Hover element <key>", "<key> elementinin üzerine gel"})
    public void hoverElement(String key) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        getActions().moveToElement(element).perform();
    }

    @Step({"Write <text> to element <key>", "<key> elementine <text> değerini yaz"})
    public void writeText(String key, String text) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        element.clear();
        element.sendKeys(text);
    }

    @Step({"Write <text> to element <key> and press ENTER", "<key> elementine <text> değerini yaz ve ENTER bas"})
    public void writeTextAndEnter(String key, String text) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        element.clear();
        element.sendKeys(text);
        element.sendKeys(Keys.ENTER);
    }

    @Step({"Clear text in element <key>", "<key> elementinin metnini temizle"})
    public void clearText(String key) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        element.clear();
    }

    @Step({"Select <text> from dropdown <key>", "<key> dropdownından <text> seç"})
    public void selectFromDropdown(String key, String text) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        Select select = new Select(element);
        select.selectByVisibleText(text);
    }

    // ==========================================
    // Kaydırma (Scroll) Adımları
    // ==========================================

    @Step({"Scroll to element <key>", "<key> elementine kaydır"})
    public void scrollToElement(String key) {
        WebElement element = findElement(ElementHelper.getBy(key));
        getJs().executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    @Step({"Scroll to bottom of page", "Sayfanın en altına kaydır"})
    public void scrollToBottom() {
        getJs().executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    @Step({"Scroll to top of page", "Sayfanın en üstüne kaydır"})
    public void scrollToTop() {
        getJs().executeScript("window.scrollTo(0, 0);");
    }

    // ==========================================
    // Doğrulama (Assertion) Adımları
    // ==========================================

    @Step({"Check if element <key> is displayed", "<key> elementinin görünür olduğunu kontrol et"})
    public void checkIfElementDisplayed(String key) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        Assertions.assertThat(element.isDisplayed())
                .as("Element [%s] sayfada görünür olmalıdır", key)
                .isTrue();
    }

    @Step({"Check if element <key> contains text <text>", "<key> elementinin <text> metnini içerdiğini kontrol et"})
    public void checkIfElementContainsText(String key, String text) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        Assertions.assertThat(element.getText().trim())
                .as("Element [%s] içeriğinde [%s] metni bulunmalıdır", key, text)
                .containsIgnoringCase(text);
    }

    @Step({"Check if current url contains <expected>", "Mevcut adresin <expected> içerdiğini kontrol et"})
    public void checkUrlContains(String expected) {
        Assertions.assertThat(getDriver().getCurrentUrl())
                .as("Mevcut sayfa URL'i [%s] içermelidir", expected)
                .contains(expected);
    }

    // ==========================================
    // Hafıza ve Sekme Adımları
    // ==========================================

    @Step({"Save text of element <key> to scenario store with key <storeKey>", "<key> elementinin metnini <storeKey> anahtarıyla senaryo hafızasına kaydet"})
    public void saveTextToStore(String key, String storeKey) {
        WebElement element = findVisibleElement(ElementHelper.getBy(key));
        String text = element.getText().trim();
        StoreHelper.putScenario(storeKey, text);
    }

    @Step({"Switch to new tab", "Yeni sekmeye geç", "Son sekmeye geç"})
    public void switchToLastTab() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
        Set<String> handles = getDriver().getWindowHandles();
        List<String> handleList = new ArrayList<>(handles);
        getDriver().switchTo().window(handleList.get(handleList.size() - 1));
    }

    @Step({"Close current tab and switch to main tab", "Mevcut sekmeyi kapat ve ana sekmeye geç"})
    public void closeTabAndSwitchToMain() {
        Set<String> handles = getDriver().getWindowHandles();
        List<String> handleList = new ArrayList<>(handles);
        if (handleList.size() > 1) {
            getDriver().close();
            getDriver().switchTo().window(handleList.get(0));
        }
    }
}
