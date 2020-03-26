import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StoreScrapeData {

    private String mySqlDriver = "com.mysql.cj.jdbc.Driver";
    private String myUrl;
    private String username;
    private String password;
    private Connection sqlConnection;

    public StoreScrapeData()
    {
        this.myUrl = readFile("myUrl", Charset.defaultCharset());
        this.username = readFile("username", Charset.defaultCharset());
        this.password = readFile("mysql", Charset.defaultCharset());
    }

    public void store(Map<String, List<String>> dataToDatabase)
    {
        // driver method to connect to db, and do insert statements

        establishConnection();

        Set productIdList = dataToDatabase.keySet();


        for (int i = 0; i < productIdList.size(); i++)
        {
            String product_id = (String)productIdList.toArray()[i];
            String name = dataToDatabase.get(productIdList.toArray()[i]).get(0);
            String url = dataToDatabase.get(productIdList.toArray()[i]).get(2);
            String img_url = dataToDatabase.get(productIdList.toArray()[i]).get(3);
            String price = dataToDatabase.get(productIdList.toArray()[i]).get(1);

            insertItem(product_id, name, url, img_url, price);

        }

        closeConnection();

    }

    private void establishConnection(){

        try {
            Class.forName(this.mySqlDriver);
            this.sqlConnection = DriverManager.getConnection(this.myUrl,this.username, this.password);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.err.println("Can't Establish a Connection to MySQL");
            // System.err.println("Retrying to Connect....");
            // sleep(5);
        }

    }



    private void insertItem(String product_id, String name, String url, String img_url, String price)
    {

        // TODO to fix possible bug in API insert statement for MariaDB/MySQl of duplicate entries into price_history
        // have this method check if an entry was inputted for the product_id today, if so, skip that entry


        // method inserts relevant data into items table and price_history table
        String sql = "INSERT IGNORE INTO items (product_id, name, url, img_url, product_id_string, date_created )" + " VALUES (?, ?, ?, ?, ?, now())";
        String sqlprice = "INSERT INTO price_history (price, date_stored, product_id)" + " VALUES (?, NOW(), ?)";
        try{
            PreparedStatement sqlStatement = sqlConnection.prepareStatement(sql);
            sqlStatement.setInt(1,removeLetters(product_id));
            sqlStatement.setString(2, name);
            sqlStatement.setString(3, url);
            sqlStatement.setString(4, img_url);
            sqlStatement.setString(5, product_id);
            sqlStatement.executeUpdate();


            PreparedStatement sqlPriceStatement = sqlConnection.prepareStatement(sqlprice);
            sqlPriceStatement.setDouble(1, Double.parseDouble(price));
            sqlPriceStatement.setInt(2,removeLetters(product_id));
            sqlPriceStatement.executeUpdate();

        }

        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.err.println("error happened while trying to insert the statement");
        }

    }

    private int removeLetters(String product_id)
    {
        // helper method to split off the numbers from the rest of the string in product_id
        String productString = product_id;
        String[] productStringSplit = productString.split("product");

        return Integer.parseInt(productStringSplit[1]);
    }
    static String readFile(String path, Charset encoding)
    {
        // method to read strings from files
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
            System.out.println("file opening error");
        }
        return new String(encoded, encoding);
    }


    private void closeConnection(){
        try {
            sqlConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

}
