package MQTT.Broker;

import java.net.*;
import java.io.*;
import java.util.*;
public class Broker {
  
    private Socket s = null;
    private ServerSocket ss = null;
	private volatile boolean running = true;
	private final List<ClientHandler> clientThreads = Collections.synchronizedList(new ArrayList<>());
    public Broker(int port) {
      
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");
			System.out.println("Waiting for a client ...");
			Thread brokerThread = new Thread(this::accept, "Broker-Thread");
			brokerThread.start();
			Scanner sc = new Scanner(System.in);
			
			while(running){
				String m = sc.nextLine();
				if(m.equals("Over")){
						running=false;
						System.out.println("Quitting");
						
				}
				else if(m.equals("SUBS")){
						System.out.println("Subscribers: ");
						System.out.println(Subscribers.map);
				}
					
			}
			shutdown();
			ss.close();
			
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
	void shutdown(){
		System.out.println("Quitting");
		
			for (ClientHandler t : clientThreads) {
				t.interrupt();
			}
			
	}
	public void accept(){
		try{
			while(running){
				
				s = ss.accept();
				ClientHandler c= new ClientHandler(s);
				c.start();
				clientThreads.add(c);
			}
			
	
		}catch(IOException i)
        {
			if(running)
            System.out.println(i);
        }
	}

    public static void main(String args[])
    {
        Broker s = new Broker(5000);
    }
}