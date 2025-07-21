package MQTT.Broker;
import java.io.*;
import java.net.*;
import MQTT.Message.*;

class ClientHandler extends Thread{
	private Socket client = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
	private BufferedInputStream bufferedInputStream = null;
	private ByteArrayOutputStream byteArrayOutputStream = null;
	private volatile boolean running = true;
    public ClientHandler(Socket client)
    {
        try {
			this.client=client;
			in = new DataInputStream(System.in);
            out = new DataOutputStream(client.getOutputStream());
			bufferedInputStream = new BufferedInputStream(client.getInputStream());
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }
    }
	void sendMessage(Message m) throws IOException{
		out.write(m.getMessageBytes());
		out.flush();
		System.out.println("[+] Sent: "+m.getType());
	}
	void close(){
		try{
			System.out.println("Closing connection");
			running=false;
			client.close();
		}catch (IOException e) {
                System.err.println("Error in receive loop: " + e.getMessage());
        }
			
	}
	void receive(){
		DataInputStream dis = new DataInputStream(bufferedInputStream);
        try {
            while (running) {
                
                Message receivedMessage = new Message(dis);
                System.out.println("\nReceived message:");
                receivedMessage.print();
                if (receivedMessage.getType() == MessageType.CONNECT) {
                    System.out.println("Client Connected Successfully!");
					Message m=new Message(MessageType.CONNACK);
					sendMessage(m);
                }
           
            }
        } catch (EOFException e) {
            System.out.println("Server closed the connection gracefully.");
        } catch (IOException e) {
            if (running) { 
                System.err.println("Error in receive loop: " + e.getMessage());
            }
        } finally {
            System.out.println("Receiver loop terminated.");
			close();
        }
	}
	public void run(){
		
			
		try
		{
			
			Thread receiverThread = new Thread(this::receive, "MQTT-Receiver");
			receiverThread.start();
			
			
			
		}
		catch(Exception i)
		{
			System.out.println(i);
		}
				
            
      
			
	}
}