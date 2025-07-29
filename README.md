# 🛰️ MQTT — Java Broker & Client Implementation

A lightweight, modular **Java** implementation of the **MQTT** protocol, featuring a broker, client library, and shared message definitions.
---


## 📝 Introduction

This repository provides:

* **Broker**: Manages client connections, subscriptions, and message dispatch.
* **Client**: Connects to the broker, publishes messages, and subscribes to topics.
* **Message Module**: Defines MQTT control packet structures and enums for encoding/decoding.

Built with **Java 21** and **Gradle**, no external server is required—just start the broker and connect clients.

---

## 🚀 Features

* **MQTT Compliance**: Full support for core control packets.
* **Broker**

  * Handles multiple concurrent client sessions.
  * Topic-based publish/subscribe routing.
* **Client**

  * Connect, PUBLISH, SUBSCRIBE, UNSUBSCRIBE, DISCONNECT.
  * QoS 0 and QoS 1 message delivery.
  * Configurable client ID, keep-alive interval, and Last Will.
* **Message Module**

  * `MessageType.java` enum mapping control packet types.
  * Encoding/decoding utilities with unit tests.
* **Extensible**: Designed for future QoS 2.

---

## 📂 Project Structure

```
MQT/
├── broker/        # Broker module (src/main/java/...)
├── client/        # Client module (src/main/java/...)
├── message/       # Shared message definitions & tests
├── guide.txt      # Step-by-step usage guide
├── build.gradle   # Root Gradle configuration
├── settings.gradle
└── gradle/        # Gradle wrapper files
```

Each module is a separate Gradle subproject. The `message` module includes unit tests verifying packet encoding/decoding.

---

## ⚡ Quickstart

1. **Clone and Build**

   ```bash
   git clone https://github.com/Addy897/MQTT.git
   cd MQTT
   ./gradlew build
   ```

2. **Run the Broker**

   ```bash
   ./gradlew :broker:run
   ```

   The broker listens on port **1883** by default.

3. **Run a Client**

   ```bash
   ./gradlew :client:run'
   ```
---

## 📖 Usage Examples

### 1. Publishing a Message

```bash
# From console client:
> PUB sensors/temperature 23.5
```

### 2. Subscribing to a Topic

```bash
# From console client:
> SUB sensors/temperature
```
## ✅ Testing

Run unit tests for the `message` module:

```bash
./gradlew :message:test
```
---