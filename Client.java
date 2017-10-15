/*
 * This will buy books lol
 *
 */
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import java.util.Scanner;

public class Client{
    static XmlRpcClientConfigImpl config = null;
    static XmlRpcClient server = null;

    public static String search(String topic){
	try{
	    Object[] params = new Object[]{topic};
	    return (String) server.execute("bookstore.search", params);
	}catch(Exception e){
	    System.err.println(e);
	    return "Bad request!";
	}
    }

    public static String lookup(int id){
	try{
	    Object[] params = new Object[]{new Integer(id)};
	    return (String) server.execute("bookstore.lookup", params);
	}catch(Exception e){
	    System.err.println(e);
	    return "Bad request!";
	}
    }

    public static String buy(int id){
	try{
	    Object[] params = new Object[]{new Integer(id)};
	    return (String) server.execute("bookstore.buy", params);
	}catch(Exception e){
	    System.err.println(e);
	    return "You didn't buy anything!";
	}
    }

    private static void parseRequest(String req){
	req = req.toLowerCase();
	try{
	    String[] query = req.split("\\s+");
	    if (query[0].equals("buy")){
		Integer id = Integer.parseInt(query[1]);
		System.out.println(buy(id));
	    }else if (query[0].equals("lookup")){
		Integer id = Integer.parseInt(query[1]);
		System.out.println(lookup(id));
	    }else if (query[0].equals("search")){
		String s = "";
		for (int i=1; i < query.length - 1; i++){
		    s += query[i] + " ";
		}
		s += query[query.length-1];
		System.out.println(search(s));
	    }
	}catch(Exception e){
	    System.out.println("Invalid request!");
	}
    }
    
    public static void main(String[] args){
	try {
	    String url = "http://"+args[0]+".cs.williams.edu:8888/xmlrpc";
	    config = new XmlRpcClientConfigImpl();
	    config.setServerURL(new URL(url));
	    server = new XmlRpcClient();
	    server.setConfig(config);
	    System.out.println("welcome to looks4books store for books");
	    
	    Scanner sc = new Scanner(System.in);
	    String request;
	    while (true){
		System.out.print("> ");
		request = sc.nextLine();
		parseRequest(request);
	    }	    
	}catch(Exception e){
	    System.err.println(e);
	}
    }
}
