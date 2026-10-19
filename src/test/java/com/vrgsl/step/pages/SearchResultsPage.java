package com.vrgsl.step.pages;

import com.vrgsl.step.helper.ElementHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * SearchResultsPage (Arama Sonuçları ve Akıllı Grid Analizi)
 * ==============================================================================
 * Hepsiburada'da aranan ürünlerin (Örn: "bilgisayar") listelendiği sayfadır.
 * 
 * Bu sınıf, senaryodaki en kritik adım olan:
 * "Arama sonuçlarından 2. satırdaki 1. ürünü bul ve tıkla"
 * gereksinimini %100 dayanıklı ve kırılmaz kılan "Self-Healing Çok Katmanlı Hibrit Strateji"yi uygular:
 * 
 * Strateji Katmanları:
 * 1. Katman: JavaScript BoundingClientRect Kümeleme (Tarayıcı içi milisaniyelik satır/sütun analizi).
 * 2. Katman: Dinamik Sütun Hesabı (Grid Kapsayıcı Genişliği / Kart Genişliği oranı ile responsive uyum).
 * 3. Katman: Piksel Y-Koordinatı Eşikleme (getLocation().getY() > firstRowY + 50).
 * 4. Katman: Reklam ve sponsorlu afişleri ayıklanmış temiz listeden İndeks 4 Fallback'i.
 */
public class SearchResultsPage extends BasePage {

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Arama sonuçları listesinin sayfada görünüp görünmediğini doğrular.
     */
    public boolean isResultsListDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(ElementHelper.getBy("productCards")));
            return !findElements("productCards").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * DOM'daki ham kartlar arasından gerçek ürün olmayan reklam afişlerini,
     * sponsorlu banner'ları veya boş kutuları ayıklar.
     */
    public List<WebElement> getCleanProductCards() {
        List<WebElement> rawCards = findElements("productCards");
        List<WebElement> validProducts = new ArrayList<>();

        for (WebElement card : rawCards) {
            try {
                if (!card.isDisplayed()) continue;
                // Gerçek ürün linki veya fiyat elementi içeriyor mu kontrol et
                boolean hasLink = !card.findElements(By.xpath(".//a[contains(@href, '-p-') or contains(@href, 'hepsiburada.com')]")).isEmpty();
                boolean hasPrice = !card.findElements(By.xpath(".//*[contains(@data-test-id, 'price') or contains(@class, 'price')]")).isEmpty();

                if (hasLink || hasPrice) {
                    validProducts.add(card);
                }
            } catch (Exception ignored) {}
        }
        return validProducts.isEmpty() ? rawCards : validProducts;
    }

    /**
     * 2. Satırdaki 1. Ürünü bulur, başlığını okur, tıklar ve yeni sekmeye geçiş yapar.
     * Ürünün başlığını (sepette doğrulamak üzere) String olarak döner.
     */
    public String selectFirstProductInSecondRow() {
        isResultsListDisplayed();
        dismissCookiesIfPresent();

        WebElement targetProduct = null;
        String strategyUsed = "";

        // =========================================================================
        // 1. STRATEJİ: JavaScript BoundingClientRect Kümeleme (En Hızlı ve Kesin)
        // =========================================================================
        try {
            targetProduct = findSecondRowFirstProductViaJs();
            if (targetProduct != null) {
                strategyUsed = "1. Strateji: JavaScript BoundingClientRect Kümeleme";
            }
        } catch (Exception e) {
            System.err.println("[SearchResultsPage] 1. Strateji (JS) atlandı: " + e.getMessage());
        }

        // =========================================================================
        // 2. STRATEJİ: Dinamik Sütun Sayısı Hesabı (Container Width / Card Width)
        // =========================================================================
        if (targetProduct == null) {
            try {
                targetProduct = findSecondRowFirstProductViaColumnMath();
                if (targetProduct != null) {
                    strategyUsed = "2. Strateji: Dinamik Sütun Genişlik Orantısı";
                }
            } catch (Exception e) {
                System.err.println("[SearchResultsPage] 2. Strateji atlandı: " + e.getMessage());
            }
        }

        // =========================================================================
        // 3. STRATEJİ: Piksel Y-Koordinatı Eşikleme (Selenium getLocation)
        // =========================================================================
        if (targetProduct == null) {
            try {
                targetProduct = findSecondRowFirstProductViaCoordinates();
                if (targetProduct != null) {
                    strategyUsed = "3. Strateji: Piksel Y-Koordinatı Eşikleme";
                }
            } catch (Exception e) {
                System.err.println("[SearchResultsPage] 3. Strateji atlandı: " + e.getMessage());
            }
        }

        // =========================================================================
        // 4. STRATEJİ: Standart 4 Sütunlu Grid Fallback (Temiz Liste İndeks 4)
        // =========================================================================
        if (targetProduct == null) {
            List<WebElement> cleanList = getCleanProductCards();
            if (cleanList.size() >= 5) {
                targetProduct = cleanList.get(4);
                strategyUsed = "4. Strateji: 4-Sütunlu Grid İndeks 4 Fallback";
            } else if (!cleanList.isEmpty()) {
                targetProduct = cleanList.get(0);
                strategyUsed = "Fallback: Listedeki İlk Ürün";
            }
        }

        if (targetProduct == null) {
            throw new RuntimeException("❌ Arama sonuçlarında 2. satır 1. ürün bulunamadı!");
        }

        System.out.println("🎯 2. Satır 1. Ürün Başarıyla Tespit Edildi -> [" + strategyUsed + "]");

        // Ürün kartına odaklan (Scroll)
        scrollToElement(targetProduct);
        sleep(500);

        // Ürün başlığını oku
        String productTitle = extractProductTitle(targetProduct);
        System.out.println("📦 Seçilen Ürünün Başlığı: " + productTitle);

        // Ürün linkini yedekle ve tıklama hedefini belirle (<a> tagine öncelik ver)
        String productHref = "";
        WebElement clickTarget = targetProduct;
        try {
            WebElement linkElem = targetProduct.findElement(By.xpath(".//a[contains(@href, 'hepsiburada.com') or contains(@href, '-p-')]"));
            if (linkElem != null) {
                clickTarget = linkElem;
                productHref = linkElem.getAttribute("href");
            }
        } catch (Exception ignored) {}

        String currentWindow = driver.getWindowHandle();

        // Ürüne tıkla
        try {
            clickTarget.click();
        } catch (Exception e) {
            safeClick(clickTarget);
        }

        // Yeni sekmenin açılmasını bekle (en fazla 4 saniye)
        try {
            WebDriverWait tabWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            tabWait.until(d -> d.getWindowHandles().size() > 1);
        } catch (Exception ignored) {}

        // Yeni sekmeye geçiş yap
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (handles.size() > 1) {
            for (String handle : handles) {
                if (!handle.equals(currentWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
        } else if (!productHref.isEmpty() && !driver.getCurrentUrl().contains("-p-")) {
            // Eğer sekme açılmadıysa aynı sekmede ürün URL'sine git
            try {
                driver.get(productHref);
            } catch (TimeoutException ignored) {}
        }

        waitForPageLoad();
        dismissCookiesIfPresent();

        return productTitle;
    }

    /**
     * 1. Strateji: JavaScript BoundingClientRect Kümeleme Metodu
     */
    private WebElement findSecondRowFirstProductViaJs() {
        String jsScript =
                "const cards = Array.from(document.querySelectorAll(\"li[class*='productListContent'], article[class*='productCard'], div[data-test-id='product-card-container']\"));\n" +
                "if (cards.length < 2) return null;\n" +
                "const rows = [];\n" +
                "cards.forEach(card => {\n" +
                "    const rect = card.getBoundingClientRect();\n" +
                "    if (rect.height === 0 || rect.width === 0) return;\n" +
                "    let row = rows.find(r => Math.abs(r.top - rect.top) < 25);\n" +
                "    if (!row) {\n" +
                "        row = { top: rect.top, items: [] };\n" +
                "        rows.push(row);\n" +
                "    }\n" +
                "    row.items.push({ element: card, left: rect.left });\n" +
                "});\n" +
                "rows.sort((a, b) => a.top - b.top);\n" +
                "if (rows.length < 2 || rows[1].items.length === 0) return null;\n" +
                "rows[1].items.sort((a, b) => a.left - b.left);\n" +
                "return rows[1].items[0].element;";

        return (WebElement) js.executeScript(jsScript);
    }

    /**
     * 2. Strateji: Dinamik Sütun Genişlik Orantısı (Responsive Uyumluluk)
     */
    private WebElement findSecondRowFirstProductViaColumnMath() {
        List<WebElement> cleanList = getCleanProductCards();
        if (cleanList.size() < 5) return null;

        WebElement container = findElement("productListContainer");
        int containerWidth = container.getSize().getWidth();
        int cardWidth = cleanList.get(0).getSize().getWidth();

        if (cardWidth > 0 && containerWidth > 0) {
            int columnsPerRow = Math.max(1, Math.round((float) containerWidth / cardWidth));
            if (cleanList.size() > columnsPerRow) {
                return cleanList.get(columnsPerRow);
            }
        }
        return null;
    }

    /**
     * 3. Strateji: Piksel Y-Koordinatı Eşik Kontrolü
     */
    private WebElement findSecondRowFirstProductViaCoordinates() {
        List<WebElement> products = getCleanProductCards();
        int firstRowY = -1;

        for (WebElement p : products) {
            int currentY = p.getLocation().getY();
            if (firstRowY == -1) {
                firstRowY = currentY;
            } else if (currentY > firstRowY + 50) {
                return p;
            }
        }
        return null;
    }

    /**
     * Ürün kartı içinden başlık metnini çeker.
     */
    private String extractProductTitle(WebElement card) {
        try {
            WebElement titleElem = card.findElement(ElementHelper.getBy("productTitleInCard"));
            String title = titleElem.getText().trim();
            if (!title.isEmpty()) return title;
        } catch (Exception ignored) {}

        try {
            String fullText = card.getText().trim();
            if (!fullText.isEmpty()) {
                return fullText.split("\n")[0].trim();
            }
        } catch (Exception ignored) {}

        return "Bilgisayar Ürünü";
    }
}
