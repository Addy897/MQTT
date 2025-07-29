package MQTT.Message;
import java.util.Arrays;
import java.net.*;
import java.io.*;

public class Message {
    private MessageType type;
    private byte dup = 0;
    private byte qos = 0;
    private byte retain = 0;
    private String topic = "";      
    private int packetId = 0;       
    private byte[] payload = new byte[0];  

    public Message(MessageType type) {
        this.type = type;
    }
	public Message(DataInputStream dis) throws IOException {
        byte fixedHeaderByte = dis.readByte();
        this.retain = (byte) (fixedHeaderByte & 0x01);
        this.qos = (byte) ((fixedHeaderByte >> 1) & 0x03);
        this.dup = (byte) ((fixedHeaderByte >> 3) & 0x01);
        this.type = MessageType.from((byte)((fixedHeaderByte >> 4) & 0x0F));
		int remainingLength = decodeRemainingLength(dis);
		parseHeader(dis);
		parsePayload(dis);
			
	}
	private void decodeUTF8(DataInputStream dis) throws IOException{
			int length = dis.readUnsignedShort();
			byte[] stringBytes = new byte[length];
			dis.readFully(stringBytes);
			topic=new String(stringBytes);
	}
	private void parsePayload(DataInputStream dis) throws IOException{
		int bytesAvailableForPayload = dis.available();
        if (bytesAvailableForPayload > 0) {
            this.payload = new byte[bytesAvailableForPayload];
            dis.readFully(this.payload);
        } else {
            this.payload = new byte[0];
        }
			
	}
	private void parseHeader(DataInputStream dis) throws IOException{
			switch (type) {
					case PUBLISH:
						decodeUTF8(dis);
						if (qos > 0) {
							packetId = dis.readUnsignedShort();
						}
						break;
					case PUBACK:
					case SUBSCRIBE:
					case SUBACK:
					case UNSUBSCRIBE:
					case UNSUBACK:
						packetId = dis.readUnsignedShort();
						break;
					default:
						
			}
			
	}
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setPacketId(int id) {
        this.packetId = id;
    }
	public void setQoS(int qos){
			if(qos>0 &&qos<4){
					this.qos=(byte)qos;
			}else{
				this.qos=0;
			}
	}
	public void setDup(int dup){
			this.dup=(byte)(dup==1?1:0);
	}
	public void setRetain(int retain){
			this.retain=(byte)(retain==1?1:0);
	}
    public byte getFixedHeader() {
        int msg = 0;
        msg |= (type.getType() & 0x0F) << 4;
        msg |= (dup & 0x01) << 3;
        msg |= (qos & 0x03) << 1;
        msg |= (retain & 0x01);
        return (byte) msg;
    }
	int decodeRemainingLength(DataInputStream dis) throws IOException{
		int multiplier = 1;
		int value = 0;
		int i=0;
		byte encodedByte=0;
		do{

			encodedByte = dis.readByte();
			value += ( encodedByte & 127) * multiplier;
			multiplier *= 128;
			i++;
			if (i > 4){
				throw new IOException("Malformed Remaining Length");
			}

		}while (( encodedByte & 128) != 0);
		return value;
	}
	byte[] encodeRemainingLength(int remainingLength){
		byte[] encodedBytes = new byte[4];
		int k = 0;

		do {
			byte encodedByte = (byte) (remainingLength % 128);
			remainingLength /= 128;
			if (remainingLength > 0) {
				encodedByte |= 128;
			}
			encodedBytes[k++] = encodedByte;
		} while (remainingLength > 0);

		return Arrays.copyOf(encodedBytes, k);
			
		}
	public MessageType getType(){
		return type;
	}
	public int getQoS(){
			return Byte.toUnsignedInt(qos);
	}
	public int getDup(){
			return Byte.toUnsignedInt(dup);
	}
	public int getRetain(){
			return Byte.toUnsignedInt(retain);
	}	
	public int getPacketId(){
			return packetId;
	}
	public String getTopic() {
        return topic;
    }
	public byte[] getMessageBytes(){
			byte[] variableHeader =getVariableHeader();
			int remainingLength=variableHeader.length+payload.length;
			byte[] msg = new byte[]{getFixedHeader()};
			msg=concat(msg,encodeRemainingLength(remainingLength));
			msg=concat(msg,variableHeader);
			msg=concat(msg,payload);
			
			return msg;
	}
    public byte[] getVariableHeader() {
        switch (type) {
            case PUBLISH:
                byte[] topicBytes = encodeUTF8(topic);
                if (qos > 0) {
                    return concat(topicBytes, encodePacketId(packetId));
                } else {
                    return topicBytes;
                }
            case PUBACK:
            case SUBSCRIBE:
            case SUBACK:
			case UNSUBSCRIBE:
			case UNSUBACK:
                return encodePacketId(packetId);
            default:
                return new byte[0];
        }
    }

    public byte[] getPayload() {
        return payload;
    }

    private byte[] encodeUTF8(String str) {
        byte[] utf = str.getBytes();
        int len = utf.length;
        return concat(new byte[] { (byte)(len >> 8), (byte)(len & 0xFF) },str.getBytes());
    }
    private byte[] encodePacketId(int id) {
        return new byte[] { (byte)(id >> 8), (byte)(id & 0xFF) };
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] out = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
	public void print() {
		System.out.println("===== MQTT Message =====");
		System.out.println("Type       : " + type);
		System.out.println("DUP Flag   : " + dup);
		System.out.println("QoS Level  : " + qos);
		System.out.println("Retain     : " + retain);
		
		if (!topic.isEmpty()) {
			System.out.println("Topic      : " + topic);
		}

		if (packetId!=0) {
			System.out.println("Packet ID  : " + packetId);
		}

		if (payload != null && payload.length > 0) {
			String asString = new String(payload);
			System.out.println("Payload    : \"" + asString + "\"");
			 System.out.println();
			
		} else {
			System.out.println("Payload    : [empty]");
		}
		System.out.println("========================");
	}


    
}

