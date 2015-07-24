package bin.spider.dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteJdbc {

	
	public static void main(String[] args) {
		try {
            // The SQLite (3.3.8) Database File
            // This database has one table (pmp_countries) with 3 columns (country_id, country_code, country_name)
            // It has like 237 records of all the countries I could think of.
            String fileName = "D:/ProgramFiles/SQLite Expert/DBRepo/spiderLinks.db";
            // Driver to Use
            // http://www.zentus.com/sqlitejdbc/index.html
            Class.forName("org.sqlite.JDBC");
            // Create Connection Object to SQLite Database
            // If you want to only create a database in memory, exclude the +fileName
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
            // Create a Statement object for the database connection, dunno what this stuff does though.
            Statement stmt = conn.createStatement();
            // Create a result set object for the statement
            stmt.execute("INSERT INTO TOCRAWL (geturl) VALUES('www.net.com.cn')");           
            ResultSet rsW = stmt.executeQuery("SELECT * FROM TOCRAWL");            
            // Iterate the result set, printing each column
            // if the column was an int, we could do rs.getInt(column name here) as well, etc.
            while(rsW.next()){
                String urlString = rsW.getString("geturl");
                System.out.println("TOCRAWL: "+urlString);
            }
            
            stmt.execute("DELETE FROM FINISH WHERE visited = 'http://www.qq.com'");
            ResultSet rsF = stmt.executeQuery("SELECT * FROM FINISH");
            while (rsF.next()) {
                String url = rsF.getString("visited");   // Column 1
                System.out.println("finish_links: "+url);

            }
            // Close the connection
            conn.close();
        }
        catch (Exception e) {
            // Print some generic debug info
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        }
	}
}
