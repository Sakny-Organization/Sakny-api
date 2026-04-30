/**
 * Sakny – WebSocket Chat Client (vanilla JS / SockJS + STOMP)
 * -----------------------------------------------------------
 * Dependencies (add to your frontend):
 *   <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
 *   <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs/bundles/stomp.umd.js"></script>
 *
 * Or with npm:
 *   npm install sockjs-client @stomp/stompjs
 */

const BASE_URL  = 'http://localhost:8080';   // adjust to your server
let   jwtToken  = null;                      // set after login
let   stompClient = null;

// ─── 1. Connect ────────────────────────────────────────────────────────────

function connect(token) {
  jwtToken = token;

  stompClient = new StompJs.Client({
    webSocketFactory: () => new SockJS(`${BASE_URL}/ws`),

    // Pass JWT in the STOMP CONNECT frame headers
    connectHeaders: {
      Authorization: `Bearer ${jwtToken}`,
    },

    debug: (msg) => console.debug('[STOMP]', msg),
    reconnectDelay: 5000,   // auto-reconnect after 5 s

    onConnect: onConnected,
    onStompError: (frame) => console.error('STOMP error', frame),
    onDisconnect: () => console.log('Disconnected from WebSocket'),
  });

  stompClient.activate();
}

// ─── 2. Subscribe after connect ────────────────────────────────────────────

function onConnected() {
  console.log('WebSocket connected ✓');

  // Receive incoming messages
  stompClient.subscribe('/user/queue/messages', (frame) => {
    const message = JSON.parse(frame.body);
    console.log('New message received:', message);
    renderMessage(message);
  });

  // Receive live unread-count badge updates
  stompClient.subscribe('/user/queue/unread-count', (frame) => {
    const { conversationId, unreadCount } = JSON.parse(frame.body);
    console.log(`Conversation ${conversationId} has ${unreadCount} unread message(s)`);
    updateBadge(conversationId, unreadCount);
  });
}

// ─── 3. Send a message ─────────────────────────────────────────────────────

function sendMessage(receiverId, content) {
  if (!stompClient || !stompClient.connected) {
    console.warn('Not connected – falling back to REST');
    return sendMessageRest(receiverId, content);
  }

  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({ receiverId, content }),
  });
}

// ─── 4. REST fallback ──────────────────────────────────────────────────────

async function sendMessageRest(receiverId, content) {
  const res = await fetch(`${BASE_URL}/v1/messages`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${jwtToken}`,
    },
    body: JSON.stringify({ receiverId, content }),
  });
  return res.json();
}

// ─── 5. REST helpers ───────────────────────────────────────────────────────

async function getConversations() {
  const res = await fetch(`${BASE_URL}/v1/messages/conversations`, {
    headers: { Authorization: `Bearer ${jwtToken}` },
  });
  const data = await res.json();
  return data.data;   // unwrap ApiResponse<List<ConversationResponse>>
}

async function getMessageHistory(otherUserId, page = 0, size = 20) {
  const res = await fetch(
    `${BASE_URL}/v1/messages/${otherUserId}?page=${page}&size=${size}`,
    { headers: { Authorization: `Bearer ${jwtToken}` } }
  );
  const data = await res.json();
  return data.data;   // unwrap ApiResponse<Page<MessageResponse>>
}

async function markAsRead(messageId) {
  const res = await fetch(`${BASE_URL}/v1/messages/${messageId}/read`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${jwtToken}` },
  });
  return res.json();
}

// ─── 6. Disconnect ─────────────────────────────────────────────────────────

function disconnect() {
  if (stompClient) stompClient.deactivate();
}

// ─── 7. UI stubs (replace with your framework's rendering) ─────────────────

function renderMessage(message) {
  // message: { id, conversationId, senderId, senderName,
  //            receiverId, content, isRead, sentAt }
  console.log(`[${message.sentAt}] ${message.senderName}: ${message.content}`);
}

function updateBadge(conversationId, count) {
  const el = document.querySelector(`[data-conv-id="${conversationId}"] .badge`);
  if (el) el.textContent = count > 0 ? count : '';
}

// ─── Usage example ─────────────────────────────────────────────────────────
//
// connect('eyJhbGciOiJIUzI1NiJ9...');
//
// // After connect, open a chat with user 42:
// const history = await getMessageHistory(42);
// history.content.forEach(renderMessage);
//
// // Send a message:
// sendMessage(42, 'Merhaba! Is the room still available?');
//
// // When the user opens the conversation list:
// const conversations = await getConversations();
// conversations.forEach(c => {
//   console.log(`Chat with ${c.otherUserName} – last: "${c.lastMessageContent}" (${c.unreadCount} unread)`);
// });
