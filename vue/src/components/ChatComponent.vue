<template>
  <div class="chat-container">
    <input
      type="text"
      v-model="userMessage"
      placeholder="Type your message"
      @keydown.enter="sendMessage"
    />
    <button @click="sendMessage">Send</button>
    <div class="chat-response">
      <p
        v-for="(message, index) in chatHistory"
        :key="index"
        :class="message.role"
      >
        {{ message.content }}
      </p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userMessage: "", // 用户输入的消息
      chatHistory: [], // 聊天历史记录
      socket: null, // WebSocket 实例
      reconnectInterval: null, // 重连定时器
    };
  },
  mounted() {
    // 创建 WebSocket 连接
    this.connectWebSocket();

    // 监听 WebSocket 连接成功
    this.socket.onopen = () => {
      console.log("Connected to WebSocket");
      if (this.reconnectInterval) {
        clearInterval(this.reconnectInterval); // 连接成功时停止重连
        this.reconnectInterval = null;
      }
    };

    // 监听接收到的消息
    this.socket.onmessage = (event) => {
      console.log("AI Response: ", event.data);
      try {
        const response = JSON.parse(event.data);
        this.chatHistory.push(response); // 将 AI 回复添加到聊天历史
      } catch (error) {
        console.error("Error parsing response:", error);
        this.chatHistory.push({
          role: "error",
          content: "Failed to parse response.",
        });
      }
    };

    // 监听 WebSocket 连接关闭
    this.socket.onclose = () => {
      console.log("WebSocket connection closed");
      this.attemptReconnect();
    };

    // 监听 WebSocket 错误
    this.socket.onerror = (error) => {
      console.error("WebSocket error:", error);
      this.chatHistory.push({
        role: "error",
        content: "WebSocket connection error.",
      });
      this.attemptReconnect();
    };
  },
  beforeUnmount() {
    // 组件销毁时关闭 WebSocket 连接
    if (this.socket) {
      this.socket.close();
    }
  },
  methods: {
    // 创建 WebSocket 连接
    connectWebSocket() {
      this.socket = new WebSocket("ws://localhost:8080/chat");
    },

    // 尝试重新连接
    attemptReconnect() {
      if (!this.reconnectInterval) {
        this.reconnectInterval = setInterval(() => {
          console.log("Attempting to reconnect...");
          this.connectWebSocket();
        }, 5000); // 每 5 秒尝试一次重连
      }
    },

    // 发送消息
    sendMessage() {
      if (this.userMessage.trim()) {
        this.socket.send(
          JSON.stringify({ role: "user", content: this.userMessage })
        ); // 发送消息
        this.chatHistory.push({ role: "user", content: this.userMessage }); // 将用户消息添加到聊天历史
        this.userMessage = ""; // 清空输入框
      }
    },
  },
};
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  max-width: 400px;
  margin: 0 auto;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

input {
  width: 100%;
  padding: 10px 0;
  margin-bottom: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

.chat-response {
  margin-top: 20px;
  overflow-y: auto;
  max-height: 300px;
  width: 100%;
}

.chat-response p {
  margin: 5px 0;
  padding: 5px;
  border-radius: 5px;
  max-width: 70%;
}

.chat-response p.user {
  background-color: #e1ffc1;
  align-self: flex-end;
}

.chat-response p.error {
  background-color: #ffcccc;
  align-self: center;
}

.chat-response p.ai {
  background-color: #e1e1ff;
  align-self: flex-start;
}
</style>
