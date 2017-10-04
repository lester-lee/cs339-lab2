/*
 * This will buy books lol
 *
 */
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

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
    
    public static void main(String[] args){
	try {
	    config = new XmlRpcClientConfigImpl();
	    config.setServerURL(new URL("http://localhost:8888/xmlrpc"));
	    server = new XmlRpcClient();
	    server.setConfig(config);
	    System.out.println("walked into the store!");

	    System.out.println(search("bo"));
	    System.out.println(lookup(1234));
	    System.out.println(lookup(12498));
	    System.out.println(buy(12498));
	    System.out.println(lookup(12498));
	}catch(Exception e){
	    System.err.println(e);
	}
    }
}
