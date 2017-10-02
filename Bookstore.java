/*
 * Lester Lee and Brianna Rettig
 * server.java reads in a database of books and will handle
 * client requests to purchase books. It will also handle
 * the bookstore inventory and restocking.
 */
public class Bookstore {
    public String search(String topic); // returns all entries under topic
    public String lookup(int bookid); // returns entry w/ bookid
    public void buy(int bookid); // updates inventory w/ new book
    public String log(); // returns log of purchase history
    public void restock(); // restocks inventory
    public void update(int bookid, int price); // updates price of book
    public static void main(String[] args){
	// setup / check database
	// make a webserver
	// wait for client requests
	// parse client requests
	// perform appropriate function
    }
    
}
