package MQTT.Broker;
import java.io.*;
import java.net.*;
class ClientHandler extends Thread{
	Socket client;
	public ClientHandler(Socket client){
			this.client=client;
	}
	public void run(){
		try{
			System.out.println("Client accepted");
			InputStream in = client.getInputStream();
            
            String m = "";
            while (!m.equals("Over"))
            {
                try
                {
					int count = in.available();
					m="";
					while(count>0){
						byte[] b = new byte[count];
						int bytesRead = in.read(b);
						m += new String(b);
						count = in.available();

						
						
					}
					if(m.length()>0){
							System.out.println(m);
					}
				

                }
				catch(SocketException se){
					System.out.println(se);
					break;
				}
                catch(IOException i)
                {
                    System.out.println(i);
                }
				
            }
            System.out.println("Closing connection");
            client.close();
            in.close();
		}
		catch(IOException i){
            System.out.println(i);
        }
			
	}
}