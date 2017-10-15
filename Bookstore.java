/*
 * Lester Lee and Brianna Rettig
 * server.java reads in a database of books and will handle
 * client requests to purchase books. It will also handle
 * the bookstore inventory and restocking.
 */
import java.sql.*;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.util.Hashtable;

public class Bookstore {
    private static Connection db = null;
    private static Hashtable<Integer, Integer> purchaseLog = new Hashtable<Integer, Integer>();
    /*
      public String log(); // returns log of purchase history
    */

    public String search(String query){
	String res = "";
	try{
	    Statement stmt = db.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, TOPIC FROM BOOKS;");
	    while (rs.next()){
		String topic = rs.getString("topic");
		if (topic.contains(query)){
		    int id = rs.getInt("id");
		    String title = rs.getString("title");
		    res += String.format("%d | %s \n", id, title);
		}
	    }
	    rs.close();
	    stmt.close();
	}catch (Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
	return res.equals("") ? "No books found." : res;
    }

    public String lookup(Integer query){
	String res = "";
	try{
	    Statement stmt = db.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS;");
	    while (rs.next()){
		int id = rs.getInt("id");
		if (query.equals(id)){
		    String title = rs.getString("title");
		    String topic = rs.getString("topic");
		    int stock = rs.getInt("stock");
		    float price = rs.getFloat("price");
		    res += String.format("%d | %s | %s | %s | %s \n", id,
					 title, topic, stock, price);
		}
	    }
	    rs.close();
	    stmt.close();
	}catch (Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
	return res.equals("") ? "No books found." : res;
    }

    public String buy(Integer query){
	String res = "";
	String title = "";
	try{
	    // Look for the book and check if there is remaining stock
	    Statement stmt = db.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, STOCK FROM BOOKS;");
	    int remainingStock = 0;
	    while (rs.next()){
		int id = rs.getInt("id");
		if (query.equals(id)){
		    remainingStock = rs.getInt("stock");
		    title = rs.getString("title");
		}
	    }
	    // If there is no stock, return "no stock"
	    if (remainingStock == 0) { return "That book is not in stock."; }

	    // Otherwise, update database with new stock
	    remainingStock--;
	    String update = String.format("UPDATE BOOKS SET STOCK = %d WHERE ID = %d", remainingStock, query);
	    stmt.executeUpdate(update);
	    db.commit();
	    stmt.close();
	    rs.close();
	    // Update the purchaseLog
	    purchaseLog.put(query, purchaseLog.get(query) + 1);
	    System.out.println(purchaseLog);
	    
	}catch(Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
	return "You bought " + title;
    }

    private static void update(int query, float cost){
	try{
	    Statement stmt = db.createStatement();
	    stmt.executeUpdate(String.format("UPDATE BOOKS SET PRICE = %f WHERE ID = %d", cost, query));
	}catch (Exception e){
	    System.err.println(e);
	}
    }

    private static void restock(){
	try{
	    Statement stmt = db.createStatement();
	    stmt.executeUpdate(String.format("UPDATE BOOKS SET STOCK=8888"));
	}catch (Exception e){
	    System.err.println(e);
	}
    }
	
    private static void propagateBook(int id, String title, String topic, int stock, float price){
	try {
	    Statement stmt = db.createStatement();
	    String book = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
		String.format("VALUES (%d, '%s', '%s', %d, %f );", id, title, topic, stock, price);
	    stmt.executeUpdate(book);
	    stmt.close();

	    purchaseLog.put(id, 0);
	}catch (SQLException e){
	    if(e.getMessage().contains("UNIQUE constraint")){
		try{
		    Statement stmt = db.createStatement();
		    stmt.executeUpdate(String.format("UPDATE BOOKS SET STOCK = %d, PRICE = %f WHERE ID = %d", stock, price, id));
		    System.out.println(stock);

		    purchaseLog.put(id, 0);
		}catch(SQLException f){
		    System.err.println(f);
		}
	    }else{
		System.err.println(e);
	    }
	}
    }
    
    public static void main(String[] args){
	// setup / check database
	try {
	    Class.forName("org.sqlite.JDBC");
	    db = DriverManager.getConnection("jdbc:sqlite:looks4books.db");
	    db.setAutoCommit(false);
	    System.out.println("nice database");
	    propagateBook(53477, "Achieve Less Bugs and More Hugs in CSCI 339",
			  "distributed systems", 1555, 1.00f);
	    propagateBook(53573, "Distributed Systems for Dummies",
			  "distributed systems", 2555, 2.00f);
	    propagateBook(12365, "Surviving College", "college life", 3555, 3.00f);
	    propagateBook(12498, "Cooking for the Impatient Undergraduate",
			  "college life", 4555, 4.00f);
	    System.out.println("books in stock now!");
	    update(12498, 5.00f);
	    restock();
	    System.out.println(purchaseLog);
	    
	}catch (Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
	// make a webserver
	try {
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    XmlRpcServer xmlrs;

	    WebServer server = new WebServer(8888);
	    xmlrs = server.getXmlRpcServer();
	    phm.addHandler("bookstore", Bookstore.class);
	    xmlrs.setHandlerMapping(phm);
	    server.start();
	    System.out.println("Server started! Accepting requests now.");
	}catch (Exception e){
	    System.err.println(e);
	}
	// parse client requests
	// perform appropriate function
    }
    
}
