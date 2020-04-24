import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceSpiderDriver {

    // TODO get selenium to run headless (especially for linux)

    private List<String> pagesToCrawl;
    String osDriver;
    public PriceSpiderDriver(String windowsOrlinux) throws IOException {

        this.pagesToCrawl = Files.readAllLines(Paths.get("./pages.txt"), Charset.defaultCharset());

        this.osDriver = windowsOrlinux;

    }

    public void crawlAndConsume()
    {
        ScrapeCrawl crawler = new ScrapeCrawl(this.osDriver);
        StoreScrapeData dbStorage = new StoreScrapeData();

        for (String page : pagesToCrawl)
        {
            System.out.println("Scraping page: " + page);
            Map<String, List<String>> depositToDb = crawler.scrape(page);
            dbStorage.store(depositToDb);
        }

        crawler.quitBrowser();
    }

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("Starting SuperDry Web Scraper...");
        System.out.println(java.time.LocalDateTime.now());

        PriceSpiderDriver spider = new PriceSpiderDriver("windows");

        spider.crawlAndConsume();

        System.out.println("The spider finished it's crawl and deposited data "+ java.time.LocalDateTime.now());

        System.out.println("Starting Email Price Alert Blast...");

        RetrieveEmailData emailBlast = new RetrieveEmailData();
        emailBlast.compareAndSendEmail();

    }


}
