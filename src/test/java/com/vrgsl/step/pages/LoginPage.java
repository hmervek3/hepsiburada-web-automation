package com.vrgsl.step.pages;

import com.vrgsl.step.helper.CdpHelper;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * ==============================================================================
 * LoginPage (Hepsiburada Giriş Yap Sayfası)
 * ==============================================================================
 * Hepsiburada'nın dinamik giriş akışını (tek adımlı veya iki adımlı) yönetir:
 * 
 * Akıllı Davranış:
 * 1. Tek Adımlı Giriş: Şifre alanı kullanıcı adı ile aynı anda ekranda görünüyorsa
 *    her ikisini de doldurup tek seferde Giriş Yap'a tıklar.
 * 2. İki Adımlı Giriş: Şifre alanı henüz yoksa, e-posta yazılır -> Giriş Yap'a tıklanır
 *    -> şifre ekranının gelmesi beklenir -> şifre yazılır -> Onayla'ya tıklanır.
 * 3. Bot Koruması: Karakterler CdpHelper üzerinden insani hızda yazılır.
 */
public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * E-posta ve şifre ile tam login akışını yürütür.
     */
    public void login(String email, String password) {
        dismissCookiesIfPresent();

        // 1. E-posta alanını bul ve insani hızda yaz
        WebElement emailElem = findVisibleElement("usernameInput");
        scrollToElement(emailElem);
        CdpHelper.humanType(driver, emailElem, email);
        sleep(400);

        // 2. Şifre alanının aynı ekranda olup olmadığını kontrol et (Tek/Çift adım tespiti)
        List<WebElement> passInputs = findElements("passwordInput");
        if (!passInputs.isEmpty() && passInputs.get(0).isDisplayed()) {
            // --- TEK ADIMLI GİRİŞ FORMU ---
            WebElement passElem = passInputs.get(0);
            scrollToElement(passElem);
            CdpHelper.humanType(driver, passElem, password);
            sleep(400);

            try {
                click("loginSubmitButton");
            } catch (Exception e) {
                safeClick(findVisibleElement("loginSubmitButton"));
            }
        } else {
            // --- İKİ ADIMLI GİRİŞ FORMU ---
            try {
                click("loginSubmitButton");
            } catch (Exception e) {
                safeClick(findVisibleElement("loginSubmitButton"));
            }
            sleep(800);

            WebElement passElem = findVisibleElement("passwordInput");
            scrollToElement(passElem);
            CdpHelper.humanType(driver, passElem, password);
            sleep(400);

            try {
                click("emailLoginSubmitButton");
            } catch (Exception e) {
                safeClick(findVisibleElement("emailLoginSubmitButton"));
            }
        }

        // Login sonrası yönlenmeyi bekle
        isRedirectedAwayFromLogin();
        waitForPageLoad();
        dismissCookiesIfPresent();
    }

    /**
     * Sadece e-posta girmek için tekil metod (Gauge adımları için).
     */
    public void enterEmail(String email) {
        dismissCookiesIfPresent();
        WebElement emailElem = findVisibleElement("usernameInput");
        scrollToElement(emailElem);
        CdpHelper.humanType(driver, emailElem, email);
        sleep(400);

        List<WebElement> passInputs = findElements("passwordInput");
        if (passInputs.isEmpty() || !passInputs.get(0).isDisplayed()) {
            try {
                click("loginSubmitButton");
            } catch (Exception e) {
                safeClick(findVisibleElement("loginSubmitButton"));
            }
            sleep(800);
        }
    }

    /**
     * Sadece şifre girmek için tekil metod (Gauge adımları için).
     */
    public void enterPassword(String password) {
        WebElement passElem = findVisibleElement("passwordInput");
        scrollToElement(passElem);
        CdpHelper.humanType(driver, passElem, password);
        sleep(400);

        List<WebElement> passSelectBtns = findElements("emailLoginSubmitButton");
        if (!passSelectBtns.isEmpty() && passSelectBtns.get(0).isDisplayed()) {
            safeClick(passSelectBtns.get(0));
        } else {
            try {
                click("loginSubmitButton");
            } catch (Exception e) {
                safeClick(findVisibleElement("loginSubmitButton"));
            }
        }
    }

    /**
     * Kullanıcının 'giris.hepsiburada.com' sayfasından ayrılıp ayrılmadığını doğrular.
     */
    public boolean isRedirectedAwayFromLogin() {
        for (int i = 0; i < 20; i++) {
            try {
                String url = driver.getCurrentUrl();
                if (!url.contains("giris.hepsiburada.com")) {
                    return true;
                }
            } catch (Exception ignored) {}
            sleep(1000);
        }
        return false;
    }

    /**
     * Login sırasında hata mesajı (şifre yanlış, hesap kilitlendi vb.) çıktıysa okur.
     */
    public String getErrorMessageIfPresent() {
        try {
            List<WebElement> errors = findElements("loginErrorMessage");
            for (WebElement err : errors) {
                if (err.isDisplayed() && !err.getText().isBlank()) {
                    return err.getText().trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}
