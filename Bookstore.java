/*
 * Lester Lee and Brianna Rettig, October 16, 2017
 * server.java reads in a database of books and will handle
 * client requests to purchase books. It will also handle
 * the bookstore inventory and restocking. The front end server
 * exposes search, lookup, and buy commands to the clients. 
 * Operations not exposed to the clients but are supported in
 * the front end server: log, restock, and update.
 */
import java.sql.*;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.util.Hashtable;
import java.util.Scanner;
import java.text.DecimalFormat;

public class Bookstore {
    private static Connection db = null;

    //keep a log of how many of each of the books has been bought;
    //integer1=bookid, integer2=number of copies bought
    private static Hashtable<Integer, Integer> purchaseLog = new Hashtable<Integer, Integer>();

    //search command (exposed) for clients
    public String search(String query){
	String res = "";
	//search through the database
	try{
	    Statement stmt = db.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, TOPIC FROM BOOKS;");
	    //cycle through
	    while (rs.next()){
		String topic = rs.getString("topic");
		//if found, return title,id,topic, and price
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

    //lookup command (exposed) for clients
    public String lookup(Integer query){
	String res = "";
	//search through the database (slightly different search functionality than search,
	//requires different search input)
	try{
	    Statement stmt = db.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS;");
	    //cycle through the database
	    while (rs.next()){
		int id = rs.getInt("id");
		//if found match, grab title, topic, stock, and price
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
	//return results
	return res.equals("") ? "No books found." : res;
    }
    
    //buy command (exposed) for the clients
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
	    System.out.println(String.format("Book %d was purchased.", query));
	    System.out.print("> ");
			       
	}catch(Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
	return "You bought " + title;
    }

    //update only for the server's use (hidden to clients)
    private static void update(int query, float cost){
	try{
	    Statement stmt = db.createStatement();
	    stmt.executeUpdate(String.format("UPDATE BOOKS SET PRICE = %f WHERE ID = %d", cost, query));
	    System.out.println("Update successful!");
	}catch (Exception e){
	    System.err.println(e);
	}
    }

    
    //restock command for server's use (hidden to clients)
    private static void restock(){
	try{
	    Statement stmt = db.createStatement();
	    stmt.executeUpdate(String.format("UPDATE BOOKS SET STOCK=8888"));
	}catch (Exception e){
	    System.err.println(e);
	}
    }

    //inserts book if not already in the databse, updates book status if it is already in the database
    private static void propagateBook(int id, String title, String topic, int stock, float price){
	//book isn't in database already, insert
	try {
	    Statement stmt = db.createStatement();
	    String book = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
		String.format("VALUES (%d, '%s', '%s', %d, %f );", id, title, topic, stock, price);
	    stmt.executeUpdate(book);
	    stmt.close();

	    purchaseLog.put(id, 0);
	//book is in database, update
	}catch (SQLException e){
	    //check exception error and use update instead of insert
	    if(e.getMessage().contains("UNIQUE constraint")){
		try{
		    Statement stmt = db.createStatement();
		    stmt.executeUpdate(String.format("UPDATE BOOKS SET STOCK = %d, PRICE = %f WHERE ID = %d", stock, price, id));
		    purchaseLog.put(id, 0);
		}catch(SQLException f){
		    System.err.println(f);
		}
	    }else{
		System.err.println(e);
	    }
	}
    }

    //connect to databse and propagate it with the book stock given in assignment
    private static void setUpDatabase(){
	try {
	    Class.forName("org.sqlite.JDBC");
	    db = DriverManager.getConnection("jdbc:sqlite:looks4books.db");
	    db.setAutoCommit(false);
	    System.out.println("nice database: looks4books");
	    propagateBook(53477, "Achieve Less Bugs and More Hugs in CSCI 339",
			  "distributed systems", 1555, 1.00f);
	    propagateBook(53573, "Distributed Systems for Dummies",
			  "distributed systems", 2555, 2.00f);
	    propagateBook(12365, "Surviving College", "college life", 3555, 3.00f);
	    propagateBook(12498, "Cooking for the Impatient Undergraduate",
			  "college life", 4555, 4.00f);
	    System.out.println("books in stock now!");
	}catch (Exception e){
	    System.err.println(e);
	    System.exit(0);
	}
    }

    //connect to web
    private static void setUpWebserver(){
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
    }

    //read in client inputs from the main method and direct to correct methods
    private static void parseRequest(String req){
	switch (req.toLowerCase()){
	case "log":
	    System.out.println(purchaseLog);
	    break;
	case "restock":
	    System.out.println("Bookstore restocked!");
	    restock();
	    break;
	default:
	    if (req.contains("update")){
		String[] query = req.split("\\s+");
		try{
		    float price = Float.parseFloat(query[2]);
		    DecimalFormat decimal = new DecimalFormat("0.00");
		    String fprice = decimal.format(price);
		    update(Integer.parseInt(query[1]),
			   Float.parseFloat(fprice));
		}catch(Exception e){
		    System.out.println("Invalid request!");
		}
	    }else{
		System.out.println("Invalid request!");
	    }
	    break;
	}
    }

    //set up the database, webserver, and an infinite loop scanner to listen in to client requests
    public static void main(String[] args){
	setUpDatabase();
	setUpWebserver();
	//scanner to read in client requests
	Scanner sc = new Scanner(System.in);
	String request;
	//infinite loop to read in
	while (true){
	    System.out.print("> ");
	    request = sc.nextLine();
	    parseRequest(request);
	}
    }
    
}
