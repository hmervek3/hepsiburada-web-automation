package com.vrgsl.step.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * ==============================================================================
 * ProductDetailPage (Ürün Detay ve Sepete Ekleme Sayfası)
 * ==============================================================================
 * Seçilen ürünün detay sayfasında:
 * 1. Ürün başlığını doğrular/kaydeder.
 * 2. "Sepete Ekle" butonuna tıklar (buton bulunamazsa metin tabanlı fallback uygular).
 * 3. Sepete eklendi onay bildirimini (toast veya sepet rozet sayacı ile) doğrular.
 * 4. Çok aşamalı güvenli yönlendirme (Modal -> Header İkonu -> Direkt URL) ile sepet sayfasına geçer.
 */
public class ProductDetailPage extends BasePage {

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Ürün detay sekmesine geçişi tamamlar ve çerezleri temizler.
     */
    public void switchToProductDetailTab() {
        if (!driver.getCurrentUrl().contains("-p-") && driver.getWindowHandles().size() > 1) {
            switchToLastTab();
        }
        waitForPageLoad();
        dismissCookiesIfPresent();
    }

    /**
     * Detay sayfasındaki ürün ana başlığını okur.
     */
    public String getProductTitle() {
        try {
            WebElement titleElem = findVisibleElement("productDetailTitle");
            String title = titleElem.getText().trim();
            if (!title.isEmpty()) return title;
        } catch (Exception ignored) {}

        // Başlık elementi yakalanamazsa sayfa <title> etiketinden ayıkla
        String pageTitle = driver.getTitle();
        if (pageTitle != null && pageTitle.contains("-")) {
            return pageTitle.split("-")[0].trim();
        }
        return pageTitle != null && !pageTitle.isEmpty() ? pageTitle : "Bilgisayar Ürünü";
    }

    /**
     * "Sepete Ekle" butonuna tıklar.
     * Savunmacı Mekanizma: Eğer standart locator butonu bulamazsa, sayfadaki tüm
     * butonları tarayıp metninde 'sepet' ve 'ekle' geçen butona tıklar.
     */
    public void clickAddToCart() {
        dismissCookiesIfPresent();
        sleep(1000);

        try {
            click("addToCartButton");
        } catch (Exception fallback) {
            System.out.println("[ProductDetailPage] Standart Sepete Ekle butonu bulunamadı, metin bazlı fallback deneniyor...");
            boolean clicked = false;
            List<WebElement> allButtons = driver.findElements(By.xpath("//button"));
            for (WebElement btn : allButtons) {
                try {
                    String txt = btn.getText().toLowerCase();
                    if (txt.contains("sepet") && txt.contains("ekle")) {
                        safeClick(btn);
                        clicked = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            if (!clicked) {
                throw new RuntimeException("❌ 'Sepete Ekle' butonu sayfada bulunamadı!", fallback);
            }
        }
    }

    /**
     * Ürünün sepete eklendiğine dair modal veya toast onay mesajının göründüğünü doğrular.
     * Eğer toast çok hızlı kaybolursa sepet rozetindeki ürün adedini (badge count > 0) kontrol eder.
     */
    public boolean isAddedToCartConfirmationVisible() {
        // 1. Toast / Modal mesajını kontrol et
        if (isElementVisible("cartAddedToastModal", 6)) {
            return true;
        }

        // 2. Fallback: Sepet ikonunun rozet (badge) sayacı arttı mı?
        try {
            List<WebElement> badges = findElements("cartBadgeCount");
            for (WebElement b : badges) {
                if (b.isDisplayed() && !b.getText().trim().isEmpty() && !b.getText().trim().equals("0")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * Sepetim sayfasına güvenli şekilde gider.
     * 3 Aşamalı B Planı:
     * 1. Modal üzerindeki 'Sepete Git' butonu varsa tıklar.
     * 2. Header'daki Sepetim ikonuna tıklar.
     * 3. İkisi de başarısız olursa doğrudan 'https://checkout.hepsiburada.com/' URL'ine yönlenir.
     */
    public void navigateToCartPage() {
        sleep(1000);

        // 1. Aşama: Modal içi buton
        try {
            if (isElementVisible("goToCartButtonInModal", 4)) {
                click("goToCartButtonInModal");
                waitForPageLoad();
                dismissCookiesIfPresent();
                return;
            }
        } catch (Exception ignored) {}

        // 2. Aşama: Header sepet ikonu
        try {
            if (isElementVisible("headerCartIcon", 4)) {
                click("headerCartIcon");
                waitForPageLoad();
                dismissCookiesIfPresent();
                return;
            }
        } catch (Exception ignored) {}

        // 3. Aşama: Doğrudan checkout sayfasına yönlendirme
        navigateTo("https://checkout.hepsiburada.com/");
        waitForPageLoad();
        dismissCookiesIfPresent();
    }
}
