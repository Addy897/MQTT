package MQTT.Broker;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

public class Subscribers {
    public static final Map<String, Set<Socket>> map = new ConcurrentHashMap<>();

    public static void add(String topic, Socket client) {
        map.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(client);
		InetAddress addr = client.getInetAddress();
		String remoteIp = addr.getHostAddress();
		System.out.printf("Added %s to %s\n",remoteIp,topic);
    }

    public static Set<Socket> get(String topic) {
        return map.get(topic);
    }

    public static void remove(String topic, Socket client) {
        Set<Socket> existingSet = map.get(topic);
        if (existingSet != null) {
            existingSet.remove(client);
            if (existingSet.isEmpty()) {
                map.remove(topic, existingSet);
            }
        }
    }
}
