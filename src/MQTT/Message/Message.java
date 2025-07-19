package MQTT.Message;
import java.util.Arrays;

public class Message {
    MessageType type;
    byte dup = 0;
    byte qos = 0;
    byte retain = 0;
    String topic = "";      
    int packetId = 0;       
    byte[] payload = new byte[0];  

    public Message(MessageType type) {
        this.type = type;
    }
	public Message(byte[] bytes){
			int fixedHeader= bytes[0];
			retain = (byte)(fixedHeader & 0x01);
			type = MessageType.from((byte)( fixedHeader >> 4));
			dup = (byte)((fixedHeader >> 3)&0b1);
			qos = (byte)((fixedHeader >> 1)&0b11);
			int s=decodeRemainingLength(bytes);
			if(s>0){
				s=parseHeader(bytes,s);
				parsePayload(bytes,s);
			}
			
	}
	private int decodeUTF8(byte[] bytes,int s){
			int len=bytes[s]<<8|bytes[s+1];
			s=s+2;
			byte[] topicBytes=Arrays.copyOfRange(bytes, s, s+len);
			topic=new String(topicBytes);
			return s+len;
	}
	private void parsePayload(byte[] bytes,int s){
			payload=Arrays.copyOfRange(bytes, s, bytes.length);
			
	}
	private int parseHeader(byte[] bytes,int s){
			switch (type) {
					case PUBLISH:
						s=decodeUTF8(bytes,s);
						if (qos > 0) {
							packetId=bytes[s]<<8|bytes[s+1];
							s+=2;
						}
						break;
					case PUBACK:
					case SUBSCRIBE:
					case SUBACK:
						packetId=bytes[s]<<8|bytes[s+1];
						s=s+2;
						break;
					default:
						
			}
			return s;
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

    public byte getFixedHeader() {
        int msg = 0;
        msg |= (type.getType() & 0x0F) << 4;
        msg |= (dup & 0x01) << 3;
        msg |= (qos & 0x03) << 1;
        msg |= (retain & 0x01);
        return (byte) msg;
    }
	int decodeRemainingLength(byte[] encodedBytes){
		int multiplier = 1;
		int value = 0;
		int i=1;
		byte encodedByte=0;
		do{

			encodedByte = encodedBytes[i++];
			value += ( encodedByte & 127) * multiplier;
			multiplier *= 128;
			if (multiplier > 128*128*128 || i > 5){
				System.out.println("Malformed Remaining Length");
				return 0;
			}

		}while (( encodedByte & 128) != 0);
		if(value>0) return i;
		else return 0;
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

		if (qos > 0 || type == MessageType.SUBSCRIBE || type == MessageType.SUBACK || type == MessageType.PUBACK) {
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


    public static void main(String[] args) {
        Message pub = new Message(MessageType.PUBLISH);
        pub.setTopic("sensors/temperature");
        pub.setPacketId(42);
        pub.qos = 1;
        pub.payload = "23.5".getBytes();

        byte[] bytes=pub.getMessageBytes();
		
		Message rpub =new Message(bytes);
		pub.print();
		System.out.println("Recieved: ");
		rpub.print();
    }
}

enum MessageType{
		CONNECT((byte)1),
		CONNACK((byte)2),
		PUBLISH((byte)3),
		PUBACK((byte)4),
		SUBSCRIBE((byte)8),
		SUBACK((byte)9),
		UNSUBSCRIBE((byte)10),
		UNSUBACK((byte)11),
		PINGREQ((byte)12),
		PINGRESP((byte)13),
		DISCONNECT((byte)14);
		private final byte type;

		MessageType(byte type) {
			this.type = type;
		}
		public static MessageType from(byte fromType){
				 switch (fromType) {
					case 1: return CONNECT;
					case 2: return CONNACK;
					case 3: return PUBLISH;
					case 4: return PUBACK;
					case 8: return SUBSCRIBE;
					case 9: return SUBACK;
					case 10: return UNSUBSCRIBE;
					case 11: return UNSUBACK;
					case 12: return PINGREQ;
					case 13: return PINGRESP;
					case 14: return DISCONNECT;
					default: return DISCONNECT;
			}
		}
		byte getType() {
			return type;
		}
	
}