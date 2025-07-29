package MQTT.Client;

import java.io.*;
import java.net.*;
import java.util.*;

import MQTT.Message.*;
public class Client {
  
    private Socket s = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
	private BufferedInputStream bufferedInputStream = null;
	private ByteArrayOutputStream byteArrayOutputStream = null;
	private volatile boolean running = true;
	private PacketIdGenerator generator;
	private Map<Integer,String> pendingSubs;
    public Client(String addr, int port)
    {
        try {
            s = new Socket(addr, port);
            in = new DataInputStream(System.in);
            out = new DataOutputStream(s.getOutputStream());
			bufferedInputStream = new BufferedInputStream(s.getInputStream());
            byteArrayOutputStream = new ByteArrayOutputStream();
			generator = new PacketIdGenerator();
			pendingSubs=new HashMap<>();
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
	void receive(){
		 DataInputStream dis = new DataInputStream(bufferedInputStream);
        try {
            while (running) {
                
                Message receivedMessage = new Message(dis);
                System.out.println("\nReceived message:");
                receivedMessage.print();
                if (receivedMessage.getType() == MessageType.CONNACK) {
                    System.out.println("Client Connected Successfully!");
                }
				else if (receivedMessage.getType() == MessageType.SUBACK) {
					String topic =pendingSubs.get(receivedMessage.getPacketId());
					if(topic !=null){
						System.out.printf("Subscribed to %s Successfully!\n",topic);
						pendingSubs.remove(receivedMessage.getPacketId());
					}
                }
				else if (receivedMessage.getType() == MessageType.UNSUBACK) {
					String topic =pendingSubs.get(receivedMessage.getPacketId());
					if(topic !=null){
						System.out.printf("Unsubscribed to %s Successfully!\n",topic);
						pendingSubs.remove(receivedMessage.getPacketId());
					}
                }
				else if (receivedMessage.getType() == MessageType.DISCONNECT) {
					System.out.println("Server Disconnected!");
					running=false;
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
          
        }
	}
	void sendMessage(Message m) throws IOException{
		out.write(m.getMessageBytes());
		out.flush();
		System.out.println("Sent Packet: "+m.getType());

	}
	void connect(){
		try {
			Message message= new Message(MessageType.CONNECT);
			sendMessage(message);
			Thread receiverThread = new Thread(this::receive, "MQTT-Receiver");
			receiverThread.start();
			
			String m = "";
			int packetId;
	        while (running && !m.equals("Over")) {
	            try {
	                m = in.readLine();
	                if(m.startsWith("PUB")){
							String[] arr = m.split(" ");
							if(arr.length<3){
									System.out.println("Requires topic and value");
									continue;
							}
							packetId=generator.getNextPacketId();
							if(packetId==-1){
								System.out.println("No available packet id");
								continue;
							}
							message =new Message(MessageType.PUBLISH);
							message.setTopic(arr[1]);
							message.setPayload(arr[2].getBytes());
							message.setQoS(0);
							message.setDup(0);
							message.setRetain(0);
							
							message.setPacketId(packetId);
							sendMessage(message);
							generator.remove(packetId);
					}else if(m.startsWith("SUB")){
							String[] arr = m.split(" ");
							if(arr.length<2){
									System.out.println("Requires topic");
									continue;
							}
							packetId=generator.getNextPacketId();
							if(packetId==-1){
								System.out.println("No available packet id");
								continue;
							}
							message =new Message(MessageType.SUBSCRIBE);
							message.setPayload(arr[1].getBytes());
							message.setQoS(0);
							message.setDup(0);
							message.setRetain(0);
							
							pendingSubs.put(packetId,arr[1]);
							message.setPacketId(packetId);
							sendMessage(message);
					}else if(m.startsWith("UNSUB")){
							String[] arr = m.split(" ");
							if(arr.length<2){
									System.out.println("Requires topic");
									continue;
							}
							packetId=generator.getNextPacketId();
							if(packetId==-1){
								System.out.println("No available packet id");
								continue;
							}
							message =new Message(MessageType.UNSUBSCRIBE);
							message.setPayload(arr[1].getBytes());
							message.setQoS(0);
							message.setDup(0);
							message.setRetain(0);
							pendingSubs.put(packetId,arr[1]);
							message.setPacketId(packetId);							
							sendMessage(message);
					}
					
	            }
	            catch (IOException i) {
	                System.out.println(i);
	            }
	        }
			if(running){
				message= new Message(MessageType.DISCONNECT);
				sendMessage(message);
				running=false;
			}
			
			
        }
        catch (IOException i) {
            System.out.println(i);
            
        }
			
       
        try {
            in.close();
            out.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
	}

    public static void main(String[] args) {
        Client c = new Client("127.0.0.1", 5000);
		c.connect();
    }
}