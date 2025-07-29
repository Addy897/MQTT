package MQTT.Client;
import java.util.HashSet;
import java.util.Set;


public class PacketIdGenerator {
    private int nextPacketId = 1;
    private final Set<Integer> inFlightPacketIds = new HashSet<>();

    public synchronized int getNextPacketId(){
        int startId = nextPacketId;
        do {
            if (!inFlightPacketIds.contains(nextPacketId)) {
                inFlightPacketIds.add(nextPacketId);
                int assignedId = nextPacketId;
                nextPacketId = (nextPacketId % 65535) + 1;
                return assignedId;
            }
            nextPacketId = (nextPacketId % 65535) + 1;
        } while (nextPacketId != startId);
		return -1;
    }

    public synchronized void remove(int packetId) {
        inFlightPacketIds.remove(packetId);
    }

    public synchronized void clear() {
        nextPacketId = 1;
        inFlightPacketIds.clear();
    }
}
