# Sakny – Message Module Integration Guide

## 1. Register the module in the root `pom.xml`

```xml
<modules>
    <module>common</module>
    <module>auth</module>
    <module>user</module>
    <module>property</module>
    <module>message</module>   <!-- add this line -->
    <module>sakny-server</module>
</modules>
```

## 2. Add `message` as a dependency of `sakny-server`

In `sakny-server/pom.xml`:

```xml
<dependency>
    <groupId>com.sakny</groupId>
    <artifactId>message</artifactId>
    <version>${project.version}</version>
</dependency>
```

## 3. Register the Liquibase changelog

In your `db/changelog/db.changelog-master.xml` (inside `sakny-server`):

```xml
<include file="db/changelog/db.changelog-message.xml"
         relativeToChangelogFile="false"/>
```

Make sure the `message` module's `resources/db/changelog/` folder is on
the classpath (it will be automatically because `sakny-server` depends on it).

---

## 4. Two-line adaptations required

### 4a. `MessageMapper.java` — match your `User` field names

```java
// Change these source field names to whatever your User entity uses:
@Mapping(source = "sender.fullName",   target = "senderName")
@Mapping(source = "receiver.fullName", target = "receiverName")
```

Common alternatives: `firstName`, `username`, `name`.
If you need to concatenate first + last name, switch from MapStruct to a
manual mapping method:

```java
@Named("toFullName")
default String toFullName(User user) {
    return user.getFirstName() + " " + user.getLastName();
}

@Mapping(source = "sender",   target = "senderName",   qualifiedByName = "toFullName")
@Mapping(source = "receiver", target = "receiverName", qualifiedByName = "toFullName")
```

### 4b. `MessageService.java` — same field at line `buildConversationResponse`

```java
.otherUserName(other.getFullName())   // ← change getFullName() to your getter
```

### 4c. `WebSocketSecurityConfig.java` — fix the import

```java
// Change this import to match your actual JWT service package:
import com.sakny.auth.service.JwtService;
```

---

## 5. WebSocket Security — allow the `/ws` handshake endpoint

In your existing Spring Security HTTP config (`SecurityFilterChain`), permit
the SockJS handshake URL so it is not blocked before the STOMP-level JWT
check runs:

```java
.requestMatchers("/ws/**").permitAll()
```

---

## 6. REST API reference

| Method  | Path                              | Description                                      |
|---------|-----------------------------------|--------------------------------------------------|
| `GET`   | `/v1/messages/conversations`      | List all conversations + last message + unread   |
| `GET`   | `/v1/messages/{otherUserId}`      | Paginated history with a user (`?page=0&size=20`) |
| `POST`  | `/v1/messages`                    | Send a message (REST fallback)                   |
| `PATCH` | `/v1/messages/{messageId}/read`   | Mark a single message as read                    |

All endpoints return `ApiResponse<T>` and require a valid JWT
(`Authorization: Bearer <token>`).

---

## 7. WebSocket / STOMP reference

### Client connection

```javascript
const stompClient = new StompJs.Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    connectHeaders: { Authorization: `Bearer ${token}` },
});
stompClient.activate();
```

### Subscriptions (after `onConnect`)

| Destination               | Payload type          | When triggered                            |
|---------------------------|-----------------------|-------------------------------------------|
| `/user/queue/messages`    | `MessageResponse`     | A new message arrives for you             |
| `/user/queue/unread-count`| `UnreadCountResponse` | Unread count changes in any conversation  |

### Send a message

```javascript
stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({ receiverId: 42, content: 'Hello!' }),
});
```

---

## 8. File structure

```
message/
├── pom.xml
├── chat-client-example.js                         ← frontend usage reference
└── src/main/
    ├── java/com/sakny/message/
    │   ├── config/
    │   │   ├── WebSocketConfig.java               ← STOMP broker setup
    │   │   ├── WebSocketSecurityConfig.java        ← JWT auth over STOMP CONNECT
    │   │   └── WebSocketEventListener.java         ← connect/disconnect logging
    │   ├── controller/
    │   │   ├── MessageController.java              ← REST endpoints
    │   │   └── ChatController.java                 ← STOMP @MessageMapping
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── SendMessageRequest.java
    │   │   │   └── ChatMessageRequest.java
    │   │   └── response/
    │   │       ├── MessageResponse.java
    │   │       ├── ConversationResponse.java
    │   │       └── UnreadCountResponse.java
    │   ├── entity/
    │   │   ├── Conversation.java
    │   │   └── Message.java
    │   ├── exception/
    │   │   └── MessageExceptionHandler.java
    │   ├── mapper/
    │   │   └── MessageMapper.java
    │   ├── repository/
    │   │   ├── ConversationRepository.java
    │   │   └── MessageRepository.java
    │   └── service/
    │       └── MessageService.java
    └── resources/db/changelog/
        └── db.changelog-message.xml               ← Liquibase migration
```

---

## 9. Production checklist

- [ ] Replace `setAllowedOriginPatterns("*")` in `WebSocketConfig` with your actual frontend origin(s)
- [ ] Switch from the in-memory STOMP broker to RabbitMQ/ActiveMQ (`enableStompBrokerRelay`) for multi-instance deployments
- [ ] Add rate limiting on `/app/chat.send` to prevent message flooding
- [ ] Consider soft-delete (`deleted_at` column) on messages instead of hard-delete
- [ ] Add a `typing indicator` channel: `/user/queue/typing` with a short TTL
