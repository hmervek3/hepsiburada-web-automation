package com.vrgsl.step.steps;

import com.thoughtworks.gauge.Step;
import com.vrgsl.step.helper.ConfigHelper;
import com.vrgsl.step.helper.StoreHelper;
import com.vrgsl.step.helper.ValueHelper;
import com.vrgsl.step.pages.*;
import org.assertj.core.api.Assertions;

/**
 * ==============================================================================
 * StepImplementation (Hepsiburada Senaryo Adımları)
 * ==============================================================================
 * HB-TC01 senaryosundaki tüm adımları hem Türkçe hem İngilizce olarak karşılar.
 * 
 * Mimari Özellikler:
 * 1. Page Object Factory: Her adımda aktif ThreadLocal WebDriver ile ilgili Page nesnesini üretir.
 * 2. Gauge Scenario DataStore: Seçilen ürünün başlığını statik değişken yerine StoreHelper ile saklar.
 * 3. Fluent Assertions: AssertJ kütüphanesi ile anlaşılır ve detaylı hata mesajları üretir.
 * 4. ValueHelper Entegrasyonu: Giriş bilgileri ve arama terimlerini harici JSON'dan dinamik çözer.
 */
public class StepImplementation extends BaseSteps {

    private static final String SELECTED_PRODUCT_NAME_KEY = "SELECTED_PRODUCT_NAME";

    // Page Object Factory Metodları
    private HomePage homePage() {
        return new HomePage(getDriver());
    }

    private LoginPage loginPage() {
        return new LoginPage(getDriver());
    }

    private SearchResultsPage searchResultsPage() {
        return new SearchResultsPage(getDriver());
    }

    private ProductDetailPage productDetailPage() {
        return new ProductDetailPage(getDriver());
    }

    private CartPage cartPage() {
        return new CartPage(getDriver());
    }

    @Step({"Open Hepsiburada home page", "Hepsiburada anasayfasını aç"})
    public void openHomePage() {
        String baseUrl = ConfigHelper.getString("baseUrl", "https://www.hepsiburada.com");
        homePage().openHomePage(baseUrl);
    }

    @Step({"Open Hepsiburada home page <urlKey>", "<urlKey> Hepsiburada anasayfasını aç"})
    public void openHomePage(String urlKey) {
        String resolvedUrl = ConfigHelper.getString(urlKey, ValueHelper.getValue(urlKey));
        homePage().openHomePage(resolvedUrl);
    }

    @Step({"Navigate to login page", "Giriş yap sayfasına git"})
    public void navigateToLogin() {
        homePage().navigateToLoginPage();
    }

    @Step({"Enter email <email>", "E-posta gir <email>", "<email> e-postasını gir"})
    public void enterEmail(String email) {
        String resolvedEmail = ValueHelper.getValue(email);
        loginPage().enterEmail(resolvedEmail);
    }

    @Step({"Enter password <password>", "Şifre gir <password>", "<password> şifresini gir"})
    public void enterPassword(String password) {
        String resolvedPassword = ValueHelper.getValue(password);
        loginPage().enterPassword(resolvedPassword);
    }

    @Step({"Login with username <username> and password <password>", "<username> kullanıcı adı ve <password> şifresi ile giriş yap"})
    public void login(String username, String password) {
        String resolvedEmail = ValueHelper.getValue(username);
        String resolvedPassword = ValueHelper.getValue(password);
        loginPage().login(resolvedEmail, resolvedPassword);
    }

    @Step({"Login as default user", "Varsayılan kullanıcı olarak giriş yap", "Login with default credentials"})
    public void loginWithDefaultCredentials() {
        String email = ValueHelper.getValue("defaultUserEmail");
        String password = ValueHelper.getValue("defaultUserPassword");
        loginPage().login(email, password);
    }

    @Step({"Verify that user is successfully logged in", "Kullanıcının başarılı bir şekilde giriş yaptığını doğrula"})
    public void verifyLogin() {
        boolean loggedIn = false;
        String detectedError = "";
        for (int i = 0; i < 20; i++) {
            try {
                if (homePage().isUserLoggedIn()) {
                    loggedIn = true;
                    break;
                }
                detectedError = loginPage().getErrorMessageIfPresent();
            } catch (Exception ignored) {}
            try { Thread.sleep(1000); } catch (Exception ignored) {}
        }

        homePage().dismissCookiesIfPresent();

        String currentUrl = "";
        try {
            currentUrl = getDriver().getCurrentUrl();
        } catch (Exception ignored) {}

        Assertions.assertThat(loggedIn)
                .as("Kullanıcının sisteme başarılı şekilde giriş yaptığı doğrulanamadı! Mevcut URL: [%s]. Varsa Hata: [%s]",
                        currentUrl,
                        detectedError.isEmpty() ? "Yok" : detectedError)
                .isTrue();
    }

    @Step({"Search for product <keyword>", "<keyword> ürününü ara"})
    public void searchProduct(String keyword) {
        String resolvedKeyword = ValueHelper.getValue(keyword);
        homePage().searchProduct(resolvedKeyword);
    }

    @Step({"Verify search results are displayed", "Arama sonuçlarının görüntülendiğini doğrula"})
    public void verifySearchResults() {
        Assertions.assertThat(searchResultsPage().isResultsListDisplayed())
                .as("Arama sonuçları listesi ekranda görünür olmalıdır.")
                .isTrue();
    }

    @Step({"Select the first product in the second row and navigate to product details", "İkinci satır ilk ürünü seç ve detay sayfasına git"})
    public void selectSecondRowFirstProduct() {
        // Çok katmanlı hibrit algoritma ile 2. satır 1. ürünü seç ve başlığını al
        String productTitle = searchResultsPage().selectFirstProductInSecondRow();

        // Ürün başlığını sepette doğrulamak üzere Gauge Senaryo Hafızasına (DataStore) kaydet
        StoreHelper.putScenario(SELECTED_PRODUCT_NAME_KEY, productTitle);

        // Ürün sekmesine geçiş ve senkronizasyon
        productDetailPage().switchToProductDetailTab();
    }

    @Step({"Add product to cart and verify confirmation", "Ürünü sepete ekle ve onay mesajını doğrula"})
    public void addProductToCart() {
        productDetailPage().clickAddToCart();
        Assertions.assertThat(productDetailPage().isAddedToCartConfirmationVisible())
                .as("Ürünün sepete eklendiğine dair onay bildirimi (toast/rozet) görünür olmalıdır.")
                .isTrue();
    }

    @Step({"Navigate to shopping cart page", "Sepetim sayfasına git"})
    public void navigateToCart() {
        productDetailPage().navigateToCartPage();
        Assertions.assertThat(cartPage().isCartPageLoaded())
                .as("Sepet sayfası başarıyla yüklenmelidir.")
                .isTrue();
    }

    @Step({"Verify that the selected product is present in the cart", "Seçilen ürünün sepette olduğunu doğrula"})
    public void verifyProductInCart() {
        String expectedProductName = StoreHelper.getScenarioString(SELECTED_PRODUCT_NAME_KEY, "");

        Assertions.assertThat(expectedProductName)
                .as("Senaryo hafızasındaki seçilen ürün adı boş olmamalıdır.")
                .isNotEmpty();

        boolean isPresent = cartPage().isProductPresentInCart(expectedProductName);

        Assertions.assertThat(isPresent)
                .as("Eklenen ürünün başlığı [%s] sepetteki ürünler ile eşleşmelidir.", expectedProductName)
                .isTrue();
    }
}
