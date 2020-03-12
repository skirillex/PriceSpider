import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceSpiderDriver {

    private List<String> pagesToCrawl;
    String osDriver;
    public PriceSpiderDriver(String windowsOrlinux) throws IOException {

        this.pagesToCrawl = Files.readAllLines(Paths.get("pages.txt"), Charset.defaultCharset());

        this.osDriver = windowsOrlinux;

    }

    public void crawlAndConsume()
    {
        ScrapeCrawl crawler = new ScrapeCrawl(this.osDriver);
        StoreScrapeData dbStorage = new StoreScrapeData();

        for (String page : pagesToCrawl)
        {
            Map<String, List<String>> depositToDb = crawler.scrape(page);
            dbStorage.store(depositToDb);
        }
    }

    public static void main(String[] args) throws IOException {
        PriceSpiderDriver spider = new PriceSpiderDriver("windows");

        spider.crawlAndConsume();
    }


}
