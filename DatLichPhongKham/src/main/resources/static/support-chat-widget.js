(function () {
  // Cache token theo từng tab để tránh bị ghi đè khi đăng nhập account khác ở tab khác (localStorage dùng chung).
  const token =
    sessionStorage.getItem("chat_jwt") ||
    sessionStorage.getItem("token") ||
    (function () {
      const t =
        localStorage.getItem("chat_jwt") || localStorage.getItem("token") || "";
      if (t) sessionStorage.setItem("chat_jwt", t);
      return t;
    })();

  if (!token) {
    // Không có token thì không render widget (chỉ dành cho user đã đăng nhập)
    return;
  }

  // Chỉ hiển thị widget hỗ trợ cho BỆNH NHÂN và BÁC SĨ.
  // Nhân viên/Admin sẽ dùng màn chat chính với conversation list.
  let currentUserRole = null;
  let currentUserId = null;

  async function loadCurrentUserRole() {
    try {
      const res = await fetch("/api/users/me", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
      });
      const data = await res.json();
      if (!res.ok || data.code !== "OK") return null;
      const user = data.data || data;
      currentUserId = typeof user.userId === "number" ? user.userId : null;
      return user.role || null;
    } catch (e) {
      console.error("Lỗi lấy thông tin user hiện tại (widget hỗ trợ):", e);
      return null;
    }
  }

  let stompClient = null;
  let currentSupportId = null;
  let currentSupportName = "Nhân viên hỗ trợ";
  let isOpen = false;

  // Tạo DOM widget
  const container = document.createElement("div");
  container.id = "support-chat-widget";
  container.style.position = "fixed";
  container.style.right = "16px";
  container.style.bottom = "16px";
  container.style.zIndex = "9999";
  container.style.fontFamily = "Arial, sans-serif";

  const button = document.createElement("button");
  button.textContent = "Chat hỗ trợ";
  button.style.padding = "8px 14px";
  button.style.borderRadius = "999px";
  button.style.border = "none";
  button.style.background = "#2563eb";
  button.style.color = "#fff";
  button.style.fontSize = "13px";
  button.style.fontWeight = "600";
  button.style.cursor = "pointer";
  button.onclick = toggle;

  const panel = document.createElement("div");
  panel.style.width = "280px";
  panel.style.maxHeight = "380px";
  panel.style.background = "#ffffff";
  panel.style.borderRadius = "12px";
  panel.style.boxShadow = "0 10px 30px rgba(15,23,42,0.32)";
  panel.style.display = "none";
  panel.style.flexDirection = "column";
  panel.style.overflow = "hidden";

  const header = document.createElement("div");
  header.style.background = "#1d4ed8";
  header.style.color = "#fff";
  header.style.padding = "8px 10px";
  header.style.display = "flex";
  header.style.alignItems = "center";
  header.style.justifyContent = "space-between";
  header.style.fontSize = "13px";
  header.style.fontWeight = "600";
  header.innerHTML =
    '<span>Hỗ trợ khách hàng</span><button id="support-chat-close" style="margin-left:8px;border:none;background:transparent;color:#e5e7eb;cursor:pointer;font-size:14px;">×</button>';

  const subtitle = document.createElement("div");
  subtitle.style.fontSize = "11px";
  subtitle.style.color = "#e5e7eb";
  subtitle.style.marginTop = "2px";
  subtitle.textContent = "Nhân viên đang sẵn sàng hỗ trợ bạn.";
  header.appendChild(subtitle);

  const body = document.createElement("div");
  body.style.display = "flex";
  body.style.flexDirection = "column";
  body.style.padding = "6px 8px 8px";

  const status = document.createElement("div");
  status.id = "support-chat-status";
  status.style.fontSize = "11px";
  status.style.color = "#16a34a";
  status.style.marginBottom = "4px";
  status.textContent = "Đang kết nối...";

  const messages = document.createElement("ul");
  messages.id = "support-chat-messages";
  messages.style.listStyle = "none";
  messages.style.margin = "0";
  messages.style.padding = "0";
  messages.style.minHeight = "140px";
  messages.style.maxHeight = "200px";
  messages.style.overflowY = "auto";
  messages.style.background = "#f9fafb";
  messages.style.borderRadius = "8px";

  const inputRow = document.createElement("div");
  inputRow.style.display = "flex";
  inputRow.style.marginTop = "6px";
  inputRow.style.gap = "4px";

  const input = document.createElement("input");
  input.id = "support-chat-input";
  input.type = "text";
  input.placeholder = "Nhập tin nhắn...";
  input.style.flex = "1";
  input.style.padding = "6px 8px";
  input.style.borderRadius = "999px";
  input.style.border = "1px solid #d1d5db";
  input.style.fontSize = "12px";

  const sendBtn = document.createElement("button");
  sendBtn.textContent = "Gửi";
  sendBtn.style.padding = "6px 10px";
  sendBtn.style.borderRadius = "999px";
  sendBtn.style.border = "none";
  sendBtn.style.background = "#2563eb";
  sendBtn.style.color = "#fff";
  sendBtn.style.fontSize = "12px";
  sendBtn.style.fontWeight = "600";
  sendBtn.style.cursor = "pointer";
  sendBtn.onclick = send;

  const error = document.createElement("div");
  error.id = "support-chat-error";
  error.style.display = "none";
  error.style.marginTop = "4px";
  error.style.fontSize = "11px";
  error.style.color = "#b91c1c";

  inputRow.appendChild(input);
  inputRow.appendChild(sendBtn);

  body.appendChild(status);
  body.appendChild(messages);
  body.appendChild(inputRow);
  body.appendChild(error);

  panel.appendChild(header);
  panel.appendChild(body);

  container.appendChild(panel);
  container.appendChild(button);
  document.body.appendChild(container);

  header.querySelector("#support-chat-close").onclick = toggle;

  function toggle() {
    isOpen = !isOpen;
    panel.style.display = isOpen ? "flex" : "none";
    if (isOpen && !stompClient) {
      initSupportChat();
    }
  }

  function appendMessage(text, type) {
    const li = document.createElement("li");
    li.style.margin = "3px 6px";
    li.style.maxWidth = "80%";
    li.style.padding = "4px 8px";
    li.style.borderRadius = "10px";
    li.style.fontSize = "12px";
    li.style.wordWrap = "break-word";

    if (type === "system") {
      li.style.background = "#f3f4f6";
      li.style.color = "#6b7280";
      li.style.textAlign = "center";
      li.style.margin = "4px auto";
    } else if (type === "me") {
      li.style.background = "#dbeafe";
      li.style.marginLeft = "auto";
    } else {
      li.style.background = "#ffffff";
      li.style.border = "1px solid #e5e7eb";
      li.style.marginRight = "auto";
    }

    li.textContent = text;
    messages.appendChild(li);
    messages.scrollTop = messages.scrollHeight;
  }

  async function initSupportChat() {
    const statusEl = status;
    const errorEl = error;

    errorEl.style.display = "none";
    appendMessage("Đang tìm nhân viên hỗ trợ...", "system");

    try {
      const res = await fetch("/api/chat/partners", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
      });
      const body = await res.json();
      if (!res.ok || body.code !== "OK") {
        errorEl.textContent = body.message || "Không tải được danh sách nhân viên hỗ trợ.";
        errorEl.style.display = "block";
        statusEl.textContent = "";
        return;
      }
      const partners = (body.data || []).filter(
        (p) => p.roleName === "EMPLOYEE" || p.roleName === "ADMIN"
      );
      if (!partners.length) {
        appendMessage("Hiện chưa có nhân viên hỗ trợ nào khả dụng.", "system");
        statusEl.textContent = "";
        return;
      }
      const p = partners[0];
      currentSupportId = p.userId;
      currentSupportName = p.fullName || "Nhân viên hỗ trợ";
      appendMessage("Đã kết nối tới " + currentSupportName + ".", "system");

      connectWebSocket();
    } catch (e) {
      console.error(e);
      errorEl.textContent = "Lỗi khi gọi API đối tượng hỗ trợ.";
      errorEl.style.display = "block";
      statusEl.textContent = "";
    }
  }

  function connectWebSocket() {
    const statusEl = status;
    const errorEl = error;

    const socket = new SockJS("/ws-chat");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
      { Authorization: "Bearer " + token },
      function () {
        statusEl.textContent = "Đã kết nối WebSocket.";

        stompClient.subscribe("/user/queue/messages", function (message) {
          const msg = JSON.parse(message.body);
          // Backend đã gửi message chỉ cho sender và receiver, nên chỉ cần kiểm tra có liên quan tới nhân viên hỗ trợ hiện tại
          if (
            msg &&
            currentSupportId &&
            (msg.senderUserId === currentSupportId || msg.receiverUserId === currentSupportId)
          ) {
            const isMine = currentUserId && msg.senderUserId === currentUserId;
            appendMessage(msg.content, isMine ? "me" : "other");
          }
        });

        if (currentSupportId) {
          loadHistory();
        }
      },
      function (err) {
        console.error(err);
        statusEl.textContent = "Lỗi kết nối WebSocket.";
        errorEl.textContent = "Không thể mở kênh chat hỗ trợ.";
        errorEl.style.display = "block";
      }
    );
  }

  async function loadHistory() {
    const statusEl = status;
    try {
      const res = await fetch("/api/chat/history/" + currentSupportId, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
      });
      const body = await res.json();
      if (!res.ok || body.code !== "OK") {
        console.warn("Không load được lịch sử chat hỗ trợ:", body);
        return;
      }
      const list = body.data || [];
      messages.innerHTML = "";
      list.forEach((m) => {
        // Tin nhắn nào có sender là currentUser -> mình gửi ("me"), còn lại là "other"
        const isMine = currentUserId && m.senderUserId === currentUserId;
        const type = isMine ? "me" : "other";
        appendMessage(m.content, type);
      });
      if (!list.length) {
        appendMessage("Bạn hãy đặt câu hỏi để nhân viên hỗ trợ.", "system");
      }
      statusEl.textContent = "Đang chat với " + currentSupportName + ".";
    } catch (e) {
      console.error("Lỗi load lịch sử chat hỗ trợ:", e);
    }
  }

  function send() {
    const errorEl = error;
    errorEl.style.display = "none";

    if (!stompClient || !stompClient.connected) {
      errorEl.textContent = "Chưa kết nối WebSocket.";
      errorEl.style.display = "block";
      return;
    }
    if (!currentSupportId) {
      errorEl.textContent = "Chưa chọn được nhân viên hỗ trợ.";
      errorEl.style.display = "block";
      return;
    }

    const inputEl = input;
    const content = inputEl.value.trim();
    if (!content) return;

    const payload = {
      receiverUserId: currentSupportId,
      content: content,
    };
    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(payload));
    inputEl.value = "";
  }

  // Khởi tạo: kiểm tra role, nếu là EMPLOYEE/ADMIN thì không hiển thị widget
  loadCurrentUserRole().then((role) => {
    currentUserRole = role;
    if (currentUserRole === "EMPLOYEE" || currentUserRole === "ADMIN") {
      // Ẩn hoàn toàn widget cho nhân viên/admin
      if (container && container.parentNode) {
        container.parentNode.removeChild(container);
      }
      return;
    }
    // Với USER / DOCTOR thì giữ widget, chờ người dùng bấm nút "Chat hỗ trợ"
  });
})();


