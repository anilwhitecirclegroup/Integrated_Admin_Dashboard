package com.example.admindashboard.controller;

import com.example.admindashboard.model.ChatMessage;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ChatMessageRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // The "Mailman" for WebSockets

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. LOAD THE CHAT PAGE & CONTACTS
    @GetMapping("/chat")
    public String viewChatPage(Principal principal, Model model) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        // Fetch EMP only colleagues
        List<User> empUsers = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .filter(u -> u.getUsername() != null && u.getUsername().startsWith("EMP"))
                .collect(Collectors.toList());

        List<ContactInfo> colleagues = new ArrayList<>();
        // Variable to track the actual timestamp for sorting
        final Map<Long, LocalDateTime> lastTimestamps = new java.util.HashMap<>();

        // CRUNCH RICH DATA FOR EACH CONTACT
        for (User u : empUsers) {
            List<ChatMessage> history = chatMessageRepository.findConversationHistory(currentUser.getId(), u.getId());

            int unread = (int) history.stream()
                    .filter(m -> m.getRecipientId().equals(currentUser.getId()) && !m.isRead())
                    .count();

            String snippet = "New match! Say hello.";
            LocalDateTime rawTime = null;

            if (!history.isEmpty()) {
                ChatMessage lastMsg = history.get(history.size() - 1);
                rawTime = lastMsg.getTimestamp();

                // Truncate long snippets (e.g., limit to 30 chars)
                snippet = lastMsg.getContent().length() > 30 ?
                        lastMsg.getContent().substring(0, 28) + "..." :
                        lastMsg.getContent();
            }

            String formattedTime = formatLastMessageTime(rawTime);

            lastTimestamps.put(u.getId(), rawTime);

            colleagues.add(new ContactInfo(u.getId(), u.getUsername(), u.getFullName(), unread, snippet, formattedTime));
        }

        // SORT SIDEBAR by most recent message (newest first)
        colleagues.sort((c1, c2) -> {
            LocalDateTime t1 = lastTimestamps.get(c1.getId());
            LocalDateTime t2 = lastTimestamps.get(c2.getId());
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1; // t1 has no messages, push down
            if (t2 == null) return -1; // t2 has no messages, push down
            return t2.compareTo(t1); // Sort descending
        });

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("colleagues", colleagues);
        return "chat";
    }

    // 2. HANDLE LIVE INCOMING MESSAGES
    @MessageMapping("/chat.send")
    public void processMessage(@Payload Map<String, Object> payload) {

        // 1. Extract raw data manually to prevent strict parsing crashes
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(Long.valueOf(payload.get("senderId").toString()));
        chatMessage.setRecipientId(Long.valueOf(payload.get("recipientId").toString()));
        chatMessage.setContent(payload.get("content").toString());

        // 2. Auto-stamp the exact server time securely
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setDate(LocalDate.now());

        // 3. Save the message permanently to the database
        ChatMessage savedMsg = chatMessageRepository.save(chatMessage);

        // 4. Forward the message directly to the exact queue the frontend is subscribed to!
        messagingTemplate.convertAndSend(
                "/user/" + chatMessage.getRecipientId() + "/queue/messages",
                savedMsg
        );
    }

    // 3. FETCH CHAT HISTORY & MARK AS READ
    @GetMapping("/messages/{currentUserId}/{contactId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long currentUserId, @PathVariable Long contactId) {
        List<ChatMessage> history = chatMessageRepository.findConversationHistory(currentUserId, contactId);

        // Mark any unread messages sent TO the current user as "Read" now that they opened the chat
        boolean needsSave = false;
        for (ChatMessage msg : history) {
            if (msg.getRecipientId().equals(currentUserId) && !msg.isRead()) {
                msg.setRead(true);
                needsSave = true;
            }
        }
        if (needsSave) {
            chatMessageRepository.saveAll(history);
        }
        return ResponseEntity.ok(history);
    }

    // 4. SEARCH FOR COLLEAGUES (EMP ONLY)
    @GetMapping("/chat/search")
    @ResponseBody
    public ResponseEntity<List<User>> searchColleagues(@RequestParam String query, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        // Search only applies to EMPs
        List<User> searchResults = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .filter(u -> u.getUsername() != null && u.getUsername().startsWith("EMP"))
                .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResults);
    }

    // HELPER CLASS FOR THE SIDEBAR
    public static class ContactInfo {
        private Long id;
        private String username;
        private String fullName;
        private int unreadCount;
        private String lastMessageSnippet;
        private String formattedLastTime;

        public ContactInfo(Long id, String username, String fullName, int unreadCount, String lastMessageSnippet, String formattedLastTime) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.unreadCount = unreadCount;
            this.lastMessageSnippet = lastMessageSnippet;
            this.formattedLastTime = formattedLastTime;
        }

        // Standard Getters for all fields
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public int getUnreadCount() { return unreadCount; }
        public String getLastMessageSnippet() { return lastMessageSnippet; }
        public String getFormattedLastTime() { return formattedLastTime; }
    }

    private String formatLastMessageTime(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        LocalDate msgDate = timestamp.toLocalDate();
        LocalDate today = LocalDate.now();

        if (msgDate.equals(today)) {
            // If today, show time: "10:30 AM"
            return timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else if (msgDate.equals(today.minusDays(1))) {
            return "Yesterday";
        } else if (msgDate.isAfter(today.minusWeeks(1))) {
            // Within a week, show day: "Mon"
            return timestamp.format(DateTimeFormatter.ofPattern("E"));
        } else {
            // Older, show full date: "28 Feb"
            return timestamp.format(DateTimeFormatter.ofPattern("dd MMM"));
        }
    }

}