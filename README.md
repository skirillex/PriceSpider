# PriceSpider - Java based Webscraper to populate content for wwww.colliecolliecollie.ninja

## What is this?
WebScraper to obtain historical data of items and prices from superdry.com and store them within a MySQL database utilizing Selenium, Jsoup, and MySQlConnector

This scraper has the following dependencies
 * Java
 * Selenium-server-standalone-3.114
 * Jsoup-1.12
 * MySQLConnector-Java-8.0
 * Sendgrid-Java-latest


# Program Behavior

This program starts by reading a file with a list of urls to crawl - all urls are product categories on superdry.com

Then Selenium opens the url and scrolls down to clicks "load more" until all the relevant data is loaded into HTML. Then passes the HTML content to Jsoup.

Jsoup then scrapes the item name, price, image, url and places the data  within a list within a hashmap.

After each page is scraped a StoreScrapeData object is created then the hashmap with the data is passed along to it

The StoreScrapeData object then stores that data persistently within the MySQL database

After it finishes that for all of the URL items in the .txt file it then creates a RetrieveEmailData object which checks if any user needs to be emailed a price notification via sendgrid by accessing a table in the database that has emails and item price thresholds. Then deletes that entry once it send the email.


#

One part of www.colliecolliecollie.ninja - a site that stores historical data of prices of items and provides tools for users to be notified of price decreases. 

* Java: Jsoup + Selenium scraper to get prices/items - PriceSpider - https://github.com/skirillex/PriceSpider 
* Python: Flask + uwsgi server - collie_api - https://github.com/skirillex/collie_api
* SQL: Relational database to store data 
    * MySQL
* Dart/Flutter front end
