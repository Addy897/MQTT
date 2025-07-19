package MQTT.Broker;

import java.net.*;
import java.io.*;
public class Broker {
  
    private Socket s = null;
    private ServerSocket ss = null;
    public Broker(int port) {
      
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");
			while(true){
				System.out.println("Waiting for a client ...");
				s = ss.accept();
				new ClientHandler(s).start();
			}
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[])
    {
        Broker s = new Broker(5000);
    }
}