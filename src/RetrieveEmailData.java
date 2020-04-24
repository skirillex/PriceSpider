import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrieveEmailData {

    private String mySqlDriver = "com.mysql.cj.jdbc.Driver";
    private String myUrl;
    private String username;
    private String password;
    private Connection sqlConnection;

    public RetrieveEmailData() throws IOException {
        /*
        this.myUrl = readFile("myUrl", Charset.defaultCharset());
        this.username = readFile("username", Charset.defaultCharset());
        this.password = readFile("mysql", Charset.defaultCharset());

         */
        InputStream uin = getClass().getResourceAsStream("/db_cred/myUrl");
        BufferedReader urlreader = new BufferedReader(new InputStreamReader(uin));
        this.myUrl = urlreader.readLine();

        InputStream unin = getClass().getResourceAsStream("/db_cred/username");
        BufferedReader userreader = new BufferedReader(new InputStreamReader(unin));
        this.username = userreader.readLine();

        InputStream pin = getClass().getResourceAsStream("/db_cred/mysql");
        BufferedReader passreader = new BufferedReader(new InputStreamReader(pin));
        this.password = passreader.readLine();
    }

    public ResultSet queryAndExtractAlerts() throws SQLException {


        String query = "SELECT * FROM price_watch";

        ResultSet queryResults = sqlConnection.createStatement().executeQuery(query);

        return queryResults;
    }

    public void compareAndSendEmail() throws SQLException, IOException {
        establishConnection();

        ResultSet queryResults = queryAndExtractAlerts();

        while (queryResults.next())
        {
            String email = queryResults.getString("email");
            double price = queryResults.getDouble("price");
            int id = queryResults.getInt("product_id");
            String itemname = getItemNameUrl(id)[0];
            String itemurl = getItemNameUrl(id)[1];


            if (price !=0) {
                ResultSet allitemprices = getItemData(id, false);
                while (allitemprices.next()) {
                    if (price >= allitemprices.getDouble("price")) {
                        EmailSender emailSender = new EmailSender(email, itemname, itemurl, Double.toString(allitemprices.getDouble("price")));
                        emailSender.sendAlert();
                        cleanUpAlert(id, email);
                    }

                }
            }
            else {
                ResultSet allitemprices = getItemData(id, true);
                List<Double> priceCompare = new ArrayList<>();

                while (allitemprices.next())
                {
                    priceCompare.add(allitemprices.getDouble("price"));
                }

                if (priceCompare.get(0) < priceCompare.get(1))
                {
                    EmailSender emailSender = new EmailSender(email, itemname, itemurl, Double.toString(priceCompare.get(0)));
                    emailSender.sendAlert();
                    cleanUpAlert(id, email);
                }

            }


        }

        closeConnection();

    }



    private String[] getItemNameUrl (int id) throws SQLException{
        String query = "SELECT * FROM items WHERE product_id = ?";
        PreparedStatement statement = sqlConnection.prepareStatement(query);
        statement.setInt(1, id);
        String name = "";
        String url = "";

        ResultSet itemResults = statement.executeQuery();

        while (itemResults.next())
        {
            name = itemResults.getString("name");
            url = "www.superdry.com"+itemResults.getString("url");
        }
        String[] nameUrl = {name, url};

        return nameUrl;
    }
    public ResultSet getItemData (int id, boolean anyPrice) throws SQLException {
        if (anyPrice == false) {
            String query = "SELECT * FROM  price_history WHERE product_id = ? ORDER BY date_stored DESC LIMIT 1";
            PreparedStatement statement = sqlConnection.prepareStatement(query);
            statement.setInt(1, id);

            return statement.executeQuery();
        }
        else {
            String query = "SELECT * FROM  price_history WHERE product_id = ? ORDER BY date_stored DESC LIMIT 2";
            PreparedStatement statement = sqlConnection.prepareStatement(query);
            statement.setInt(1, id);

            return statement.executeQuery();
        }

    }
    private void cleanUpAlert(int id, String email) throws SQLException {
        String query = "DELETE FROM price_watch WHERE email = ? AND product_id = ?";
        PreparedStatement statement = sqlConnection.prepareStatement(query);
        statement.setString(1, email);
        statement.setInt(2, id);
        statement.executeUpdate();

    }
/*
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

 */


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

    private void closeConnection(){
        try {
            sqlConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }


}
