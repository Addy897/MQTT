package MQTT.Message;

public enum MessageType{
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