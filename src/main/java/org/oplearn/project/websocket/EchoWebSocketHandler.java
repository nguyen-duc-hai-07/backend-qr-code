package org.oplearn.project.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.ProductResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class EchoWebSocketHandler extends TextWebSocketHandler {

  private final ObjectMapper objectMapper;

  // Danh sách lưu toàn bộ các kết nối đang online
  private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

  // Ánh xạ userId -> Danh sách các session của user đó (1 user có thể mở nhiều tab cùng lúc)
  private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessions.add(session);

    Long userId = extractUserId(session);
    if (userId != null) {
      userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
      log.info("🟢 [WebSocket] User {} kết nối (sessionId = {}). Tổng session của user: {}",
          userId, session.getId(), userSessions.get(userId).size());
    } else {
      log.info("🟢 [WebSocket] Khách vãng lai kết nối (sessionId = {})", session.getId());
    }

    log.info("📊 [WebSocket] Tổng số client online: {}", sessions.size());
    broadcastOnlineCount();
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    try {
      // Cho phép khách vãng lai tại bàn gửi yêu cầu gọi nhân viên (CALL_STAFF)
      @SuppressWarnings("unchecked")
      Map<String, Object> map = objectMapper.readValue(message.getPayload(), Map.class);
      String action = (String) map.get("action");
      if ("CALL_STAFF".equals(action)) {
        map.put("timestamp", System.currentTimeMillis());
        String alertPayload = objectMapper.writeValueAsString(map);
        TextMessage alertMsg = new TextMessage(alertPayload);
        for (WebSocketSession s : sessions) {
          safeSendMessage(s, alertMsg);
        }
        return;
      }
    } catch (Exception ignored) {
    }

    Long userId = extractUserId(session);
    String username = (String) session.getAttributes().get("username");

    if (userId == null || username == null) {
      String errorJson = objectMapper.writeValueAsString(Map.of(
          "action", "ERROR",
          "message", "Bạn cần đăng nhập tài khoản để gửi tin nhắn!"
      ));
      safeSendMessage(session, new TextMessage(errorJson));
      return;
    }

    String chatPayload = objectMapper.writeValueAsString(Map.of(
        "action", "GLOBAL_CHAT",
        "senderId", userId,
        "senderName", username,
        "content", message.getPayload(),
        "timestamp", System.currentTimeMillis()
    ));

    TextMessage textMessage = new TextMessage(chatPayload);

    for (WebSocketSession s : sessions) {
      safeSendMessage(s, textMessage);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    sessions.remove(session);

    Long userId = extractUserId(session);
    if (userId != null) {
      Set<WebSocketSession> userSessionSet = userSessions.get(userId);
      if (userSessionSet != null) {
        userSessionSet.remove(session);
        if (userSessionSet.isEmpty()) {
          userSessions.remove(userId);
        }
      }
      int remaining = (userSessions.get(userId) != null) ? userSessions.get(userId).size() : 0;
      log.info("🔴 [WebSocket] User {} ngắt kết nối session {}. Còn lại {} session của user này.",
          userId, session.getId(), remaining);
    } else {
      log.info("🔴 [WebSocket] Khách vãng lai ngắt kết nối session {}", session.getId());
    }

    log.info("📊 [WebSocket] Tổng số client online: {}", sessions.size());
    broadcastOnlineCount();
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("⚠️ [WebSocket] Lỗi kết nối ở session {}: {}", session.getId(), exception.getMessage());
  }

  public void sendToUser(Long targetUserId, String action, Map<String, Object> data) {
    if (targetUserId == null) {
      return;
    }

    Set<WebSocketSession> targetSessions = userSessions.get(targetUserId);
    if (targetSessions == null || targetSessions.isEmpty()) {
      log.info("ℹ️ [WebSocket] User {} hiện đang offline, không gửi gói tin WebSocket", targetUserId);
      return;
    }

    try {
      Map<String, Object> messageMap = new HashMap<>();
      if (data != null) {
        messageMap.putAll(data);
      }
      messageMap.put("action", action);
      messageMap.put("timestamp", System.currentTimeMillis());

      String json = objectMapper.writeValueAsString(messageMap);
      TextMessage textMessage = new TextMessage(json);

      for (WebSocketSession session : targetSessions) {
        safeSendMessage(session, textMessage);
      }
      log.info("🎯 [WebSocket] Đã gửi đích danh tới user {} (action: {}, số session nhận: {})",
          targetUserId, action, targetSessions.size());
    } catch (Exception e) {
      log.error("⚠️ [WebSocket] Lỗi serialize gói tin gửi tới user {}: {}", targetUserId, e.getMessage(), e);
    }
  }

  public boolean isUserOnline(Long userId) {
    if (userId == null) return false;
    Set<WebSocketSession> set = userSessions.get(userId);
    return set != null && !set.isEmpty();
  }

  public void broadcastOnlineCount() {
    broadcast("ONLINE_COUNT", Map.of("onlineCount", sessions.size()));
  }

  public void broadcast(String action, Map<String, Object> data) {
    if (sessions.isEmpty()) {
      return;
    }

    try {
      Map<String, Object> messageMap = new HashMap<>();
      if (data != null) {
        messageMap.putAll(data);
      }
      messageMap.put("action", action);
      messageMap.put("timestamp", System.currentTimeMillis());

      String json = objectMapper.writeValueAsString(messageMap);
      TextMessage textMessage = new TextMessage(json);

      for (WebSocketSession session : sessions) {
        safeSendMessage(session, textMessage);
      }
      log.info("📢 [WebSocket] Đã broadcast sự kiện {} tới {} client", action, sessions.size());
    } catch (Exception e) {
      log.error("⚠️ [WebSocket] Lỗi serialize thông báo: {}", e.getMessage(), e);
    }
  }

  /**
   * Bắn sự kiện đơn hàng (Đơn mới, Cập nhật trạng thái, Hủy món) theo thời gian thực:
   * 1. Gửi tới toàn bộ các màn hình Quản trị / Quầy thu ngân / Bếp (KDS) đang mở WebSocket.
   */
  public void broadcastOrderEvent(String action, Object order, String message) {
    Map<String, Object> data = new HashMap<>();
    data.put("order", order);
    if (message != null) {
      data.put("message", message);
    }
    broadcast(action, data);
  }

  /**
   * Bắn sự kiện cập nhật trạng thái còn món / hết món (isAvailable) theo thời gian thực
   * tới tất cả khách hàng đang quét mã QR xem thực đơn và màn hình thu ngân/quản lý.
   */
  public void broadcastProductAvailability(ProductResponse product) {
    Map<String, Object> data = new HashMap<>();
    data.put("productId", product.getId());
    data.put("productName", product.getName());
    data.put("isAvailable", product.getIsAvailable());
    data.put("product", product);
    String statusText = Boolean.TRUE.equals(product.getIsAvailable()) ? "Còn hàng" : "Hết hàng";
    data.put("statusText", statusText);
    data.put("message", "Món '" + product.getName() + "' hiện đã chuyển sang trạng thái: " + statusText);
    broadcast("PRODUCT_AVAILABILITY_CHANGED", data);
  }

  public void broadcastToAdmins(String action, Map<String, Object> data) {
    if (sessions.isEmpty()) {
      return;
    }

    try {
      Map<String, Object> messageMap = new HashMap<>();
      if (data != null) {
        messageMap.putAll(data);
      }
      messageMap.put("action", action);
      messageMap.put("timestamp", System.currentTimeMillis());

      String json = objectMapper.writeValueAsString(messageMap);
      TextMessage textMessage = new TextMessage(json);

      int count = 0;
      for (WebSocketSession session : sessions) {
        if (isAdminSession(session)) {
          safeSendMessage(session, textMessage);
          count++;
        }
      }
      log.info("📢 [WebSocket] Đã gửi thông báo Admin sự kiện {} tới {} admin session", action, count);
    } catch (Exception e) {
      log.error("⚠️ [WebSocket] Lỗi serialize thông báo admin: {}", e.getMessage(), e);
    }
  }

  private void safeSendMessage(WebSocketSession session, TextMessage message) {
    if (session == null || !session.isOpen()) {
      return;
    }
    try {
      synchronized (session) {
        if (session.isOpen()) {
          session.sendMessage(message);
        }
      }
    } catch (Exception e) {
      log.warn("⚠️ [WebSocket] Không thể gửi tin tới session {}: {}", session.getId(), e.getMessage());
    }
  }

  private boolean isAdminSession(WebSocketSession session) {
    Object rolesObj = session.getAttributes().get("roles");
    if (rolesObj instanceof List<?> list) {
      return list.contains("ADMIN") || list.contains("ROLE_ADMIN");
    } else if (rolesObj instanceof String str) {
      return str.contains("ADMIN");
    }
    return false;
  }

  private Long extractUserId(WebSocketSession session) {
    Object userIdObj = session.getAttributes().get("userId");
    if (userIdObj instanceof Long l) {
      return l;
    } else if (userIdObj instanceof Number n) {
      return n.longValue();
    }
    return null;
  }
}
