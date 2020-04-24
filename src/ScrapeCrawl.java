import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.jsoup.select.*;

import java.util.*;
import java.util.concurrent.TimeUnit;



public class ScrapeCrawl {

    private String BASE_URL;
    private WebDriver browser;
    private String soupContent;

    ScrapeCrawl( String whichOS)
    {
        // constructor loads selenium webdriver into variable.
        this.BASE_URL = "";

        String driverPath = driver(whichOS);
        System.setProperty("webdriver.chrome.driver", driverPath);

        this.browser = new ChromeDriver();

        this.soupContent = "";
    }

    public Map<String, List<String>> scrape(String url){
        setBASE_URL(url);

        setUp();

        clickCookiesAndLoadMore();

        Map<String, List<String>> passDataToDB = obtainScrapeData();

        //quitBrowser();

        return passDataToDB;
    }

    private String driver(String OS)
    {
        if (OS.equals("windows")) {
            return "chromedriver.exe";
        }
        return "chromedriver";
    }

    public void setBASE_URL(String url)
    {
        this.BASE_URL = url;
    }

    private void setUp() {
        // this method points the driver to the superdry site
        // will need to eventually make this method take a variable to change
        // the page it goes to: jackets, shirts, shorts etc.

        sleep(5);
        this.browser.get(this.BASE_URL);

    }

    private void clickCookiesAndLoadMore() {
        // this method clicks on the accept cookies button and then the "load more"
        // buttons until the entire page is loaded

        sleep(5);

        try {
            WebElement acceptCookies = this.browser.findElement(By.xpath("//a[@aria-label='allow cookies'][@role='button']"));
            acceptCookies.click();
        }
        catch (Exception e)
        {
            System.out.println("error while trying to click on cookies");
        }
        sleep(5);

        while (true)
        {
            // try catch statement to break the loop once all the "load more" buttons were clicked
            try{
                WebElement loadMoreButton = this.browser.findElement(By.xpath("//button[@id='loadMore'][@onclick='categoryPageController.loadMore();']"));
                loadMoreButton.click();
                System.out.println(loadMoreButton);
                sleep(5);
            }
            catch (Exception e)
            {
                break;
            }
        }

        // these next few lines make the browser scroll all the way to the bottom so all of the items load

        WebElement element = this.browser.findElement(By.xpath("//button[@id='newsletter_submit']"));
        ((JavascriptExecutor) this.browser).executeScript("arguments[0].scrollIntoView();", element);
        sleep(5);
    }

    private Map<String, List<String>> obtainScrapeData() {

        this.soupContent = this.browser.getPageSource(); // get the fully loaded html from selenium

        // file writing and reading for debugging purposes
        //FileWriter pageSource = new FileWriter("pageSource");
        //pageSource.write(this.browser.getPageSource());
        //pageSource.close();

        Document soup = Jsoup.parse(this.soupContent); // Jsoup parser with the HTML from selenium

        //File input = new File("pageSource");
        //Document soup = Jsoup.parse(input, "UTF-8");
        Elements allJackets = soup.getElementsByClass("product-container col-6 col-md-4 col-lg-3  "); // gets all the elements / containers for each item and places them within Elements list

        System.out.println(allJackets.size());
        Map<String, List<String>> itemsMapData = new HashMap<>();

        for (Element i : allJackets)
        {
            List<String> namePriceLinkValues = new ArrayList<>();

            // get name of item
            namePriceLinkValues.add(i.getElementsByClass("product-details__name").text());

            // get the price of the item
            namePriceLinkValues.add(findPrice(i));

            // get the link to the item
            namePriceLinkValues.add(i.select("a[href]").get(1).attr("href"));

            // get the link to the image of the item
            namePriceLinkValues.add(findImg(i));

            // hashmap with the item id as the key and the list as the value
            itemsMapData.put(i.attr("id"), namePriceLinkValues);

            System.out.println(i.attr("id"));
            System.out.println(namePriceLinkValues);
        }


        return itemsMapData;
    }

    private String findImg(Element item)
    {
        if (!item.getElementsByTag("img").attr("src").equals("/public/images/nothing.png"))
        {
            return item.getElementsByTag("img").attr("src");
        }
        else
        {
            return item.getElementsByTag("img").attr("data-src");
        }
    }

    private String findPrice(Element item)
    {
        // DEBUG
        // System.out.println("find price item element: ");
        // System.out.println(item.attr("data-price"));
        // System.out.println(" ");

        //TODO price error on polos and beanies page. need to check code to fix getting some of the prices

        // if "data-price" value is not found ie: empty string
        if (item.attr("data-price").length() == 0)
        {
           Elements pricePrice = item.getElementsByClass("product-details__price price");

           // DEBUG
           // System.out.println(" pricePrice.get(0)");
           // System.out.println(pricePrice.first());


            // if "price font_bold" element has a string    ie: not an empty string
            if (pricePrice.first().getElementsByClass("price font_bold").text().length() != 0)
           {

               String priceString = item.getElementsByClass("price font_bold").text();
               String[] priceStringSplit = priceString.split("\\$");

               // DEBUG
               //System.out.println(Arrays.toString(priceStringSplit));
               //System.out.println("priceString ");
               //System.out.println(priceString);

               return priceStringSplit[1];
           }
           else
           {
               String[] priceStringSplit = pricePrice.text().split("\\$");

               // DEBUG
               // System.out.println(Arrays.toString(priceStringSplit));
               // System.out.println("PricePrice.text() ");
               // System.out.println(priceStringSplit[0]);

               // try-catch statement because if the page isn't loaded it will return an empty split array
               // which results in null pointer exception
               try {
                   return priceStringSplit[1];
               }
               catch (Exception e)
               {
                   // System.out.println("Catch error");
                   return priceStringSplit[0];
               }
           }

        }
        else
        {
            return item.attr("data-price");
        }
    }

    private void sleep(int seconds)
    {
        // method to call and invoke a pause during the scraping
        try{
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException e)
        {
            System.err.format("IOException: %s%n", e);
        }
    }

    public void quitBrowser()
    {
        // closes the browser when done
        this.browser.quit();
    }

}
