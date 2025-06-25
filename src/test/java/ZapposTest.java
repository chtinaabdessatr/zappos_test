

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ZapposTest {
    WebDriver driver;
    @BeforeClass
    public void setup() {
        // Set path to your local chromedriver.exe
        System.setProperty("webdriver.chrome.driver", "../../../chrome/chromedriver.exe");

        // Setup ChromeOptions to point to portable Chromium
        ChromeOptions options = new ChromeOptions();
        options.setBinary(new File("../../../chrome/chrome-win/chrome.exe"));  // Path to portable Chromium
        options.addArguments("--remote-allow-origins=*"); // Optional to fix common errors
        options.addArguments("--start-maximized");

        // Initialize driver
        driver = new ChromeDriver(options);
    }

    @Test(priority = 1)
    public void openHomePage() {
        long start = System.currentTimeMillis();
        driver.get("https://www.zappos.com");
        long end = System.currentTimeMillis();
        System.out.println("Page loaded in: " + (end - start) + "ms");
        Assert.assertTrue(driver.getTitle().toLowerCase().contains("zappos"));
    }

    @Test(priority = 2)
    public void searchProduct() {
        WebElement searchBox = driver.findElement(By.name("term"));
        searchBox.sendKeys("running shoes" + Keys.ENTER);
    }


    @Test(priority = 3)
    public void extractProductsInfo() throws InterruptedException {
        Thread.sleep(3000); // Wait for search results
        List<WebElement> products = driver.findElements(By.cssSelector("[data-component='ProductCard']"));

        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            try {
                String name = products.get(i).findElement(By.cssSelector("a[aria-label]")).getText();
                String price = products.get(i).findElement(By.cssSelector("[data-test='product-price']")).getText();
                System.out.println("Produit : " + name + " | Prix : " + price);
                Assert.assertFalse(name.isEmpty());
                Assert.assertTrue(price.contains("$"));
            } catch (NoSuchElementException e) {
                System.out.println("Produit manquant d'informations.");
            }
        }
    }

    @Test(priority = 4)
    public void applyFilter() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions actions = new Actions(driver);

        // Scroll to Brand filter button and expand if collapsed
        WebElement brandFilterButton = driver.findElement(By.cssSelector("button.iu-z[data-test-id-facet-head-name='Brand']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", brandFilterButton);
        if ("false".equals(brandFilterButton.getAttribute("aria-expanded"))) {
            brandFilterButton.click();
        }

        // Wait for brand search input, type Nike
        WebElement brandSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Brand")));
        brandSearchInput.clear();
        brandSearchInput.sendKeys("Nike");

        // Wait for first <li> under the brand list (right below the input)
        By firstBrandLiLocator = By.xpath("//ul[@aria-labelledby='brandNameFacet']/li[1]/a");
        WebElement firstBrandLi = wait.until(ExpectedConditions.elementToBeClickable(firstBrandLiLocator));

        // Scroll into view and click the first <li>
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", firstBrandLi);
        actions.moveToElement(firstBrandLi).click().perform();

        // Wait for filter to apply
        Thread.sleep(5000);

        // Verify filter applied (you might want to check a specific element or URL change)
        Assert.assertTrue(driver.getPageSource().contains("Nike"));
    }




    @Test(priority = 5)
    public void addToCart() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Find all products with the product card selector
        List<WebElement> products = driver.findElements(By.cssSelector("[data-component='ProductCard']"));

        if (products.isEmpty()) {
            System.out.println("No products found to add to cart.");
            return; // Exit the test or throw exception based on your test strategy
        }

        // Click the first product card
        products.get(0).click();

        // Save the current window handle
        String originalWindow = driver.getWindowHandle();

        // Wait for the new tab/window to open (assuming product detail opens in new tab)
        wait.until(driver -> driver.getWindowHandles().size() > 1);

        // Switch to the new window/tab
        Set<String> allWindows = driver.getWindowHandles();
        for (String windowHandle : allWindows) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Wait for the sizing chooser to be visible and interactable
        WebElement firstSize = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("fieldset legend#sizingChooser + div input[type='radio']")));

        firstSize.click();

        // Wait for "Add to Bag" button to be clickable and click it
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#add-to-cart-button")));

        addToCartBtn.click();

        // Optional: wait for confirmation, cart update, or any expected result here
        // Example: wait until cart count is updated or confirmation message visible

        // Close the product tab and switch back to original window if needed
        driver.close();
        driver.switchTo().window(originalWindow);
    }


    @Test(priority = 6)
    public void verifyCart() throws InterruptedException {
        driver.get("https://www.zappos.com/cart");
        Thread.sleep(3000);
        WebElement cartContent = driver.findElement(By.cssSelector("div[data-test='cartItems']"));
        String cartText = cartContent.getText();
        System.out.println("Contenu Panier : " + cartText);
        Assert.assertTrue(cartText.contains("Item") || cartText.matches(".*\\d+.*"));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
