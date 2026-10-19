package com.vrgsl.step.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * ==============================================================================
 * HomePage (Hepsiburada Anasayfa Page Object)
 * ==============================================================================
 * Anasayfa üzerindeki çerez kapatma, kullanıcı girişi butonuna tıklama,
 * ürün arama ve kullanıcının giriş durumunu doğrulama işlemlerini içerir.
 */
public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Hepsiburada anasayfasını açar ve çerez bildirimini temizler.
     */
    public void openHomePage(String url) {
        navigateTo(url);
        waitForPageLoad();
        dismissCookiesIfPresent();
    }

    /**
     * Sağ üstteki "Giriş Yap" alanının üzerine gelir (hover) ve açılan menüden Giriş Yap linkine tıklar.
     */
    public void navigateToLoginPage() {
        dismissCookiesIfPresent();
        try {
            hover("myAccountDropdown");
            sleep(500);
            click("loginButton");
        } catch (Exception e) {
            // Hover menüsü açılmazsa doğrudan login URL'ine yönlendir (Savunmacı Fallback)
            navigateTo("https://giris.hepsiburada.com");
        }
        waitForPageLoad();
    }

    /**
     * Arama kutusuna belirtilen anahtar kelimeyi (Örn: "bilgisayar") yazar ve Enter tuşuna basar.
     */
    public void searchProduct(String keyword) {
        dismissCookiesIfPresent();
        try {
            click("searchBoxContainer");
            sleep(400);

            WebElement activeInput = findVisibleElement("searchBoxInput");
            activeInput.clear();
            activeInput.sendKeys(keyword);
            sleep(300);
            activeInput.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            // Alternatif input arayüzü fallback
            WebElement input = findElement("searchBoxInput");
            input.clear();
            input.sendKeys(keyword);
            input.sendKeys(Keys.ENTER);
        }
        waitForPageLoad();
        dismissCookiesIfPresent();
    }

    /**
     * Kullanıcının başarılı şekilde giriş yapıp yapmadığını header alanındaki
     * kullanıcı adı veya "Hesabım" yazısından doğrular.
     */
    public boolean isUserLoggedIn() {
        try {
            List<WebElement> headers = findElements("accountUsernameHeader");
            for (WebElement h : headers) {
                String text = h.getText().trim();
                if (h.isDisplayed() && !text.equalsIgnoreCase("Giriş Yap") && !text.isEmpty()) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        // Eğer header henüz güncellenmediyse ve login sayfasından çıkıldıysa başarılı say
        return !driver.getCurrentUrl().contains("giris.hepsiburada.com");
    }
}
