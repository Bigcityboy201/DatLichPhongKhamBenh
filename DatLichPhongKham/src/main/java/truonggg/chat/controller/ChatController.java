package truonggg.chat.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.chat.application.ChatCommandService;
import truonggg.chat.application.ChatQueryService;
import truonggg.dto.reponseDTO.ChatMessageResponseDTO;
import truonggg.dto.reponseDTO.ChatPartnerResponseDTO;
import truonggg.dto.requestDTO.ChatMessageRequestDTO;
import truonggg.reponse.SuccessReponse;

/**
 * Presentation layer:
 * - REST: lấy lịch sử chat
 * - WebSocket/STOMP: gửi/nhận message real-time
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private final truonggg.user.infrastructure.UserRepository userRepository;
    private final truonggg.chat.infrastructure.ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/history/{partnerUserId}")
    @PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'ADMIN', 'EMPLOYEE')")
    public SuccessReponse<List<ChatMessageResponseDTO>> getChatHistory(Principal principal,
            @PathVariable Integer partnerUserId) {
        String username = principal != null ? principal.getName() : null;
        return SuccessReponse.of(this.chatQueryService.getConversation(partnerUserId, username));
    }

    /**
     * Endpoint WebSocket: client gửi message tới /app/chat.sendMessage
     * server push ra topic /topic/chat.{userId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Principal principal, @Payload @Valid ChatMessageRequestDTO request) {

        String username = principal != null ? principal.getName() : null;

        ChatMessageResponseDTO msg = chatCommandService.sendMessage(request, username);

        // gửi cho người nhận
        var receiver = userRepository.findById(msg.getReceiverUserId()).orElseThrow();

        messagingTemplate.convertAndSendToUser(
                receiver.getUserName(),
                "/queue/messages",
                msg
        );

        // gửi lại cho người gửi
        var sender = userRepository.findById(msg.getSenderUserId()).orElseThrow();

        messagingTemplate.convertAndSendToUser(
                sender.getUserName(),
                "/queue/messages",
                msg
        );
    }

    /**
     * Trả về danh sách đối tượng mà user hiện tại có thể chat cùng, tuỳ theo role:
     * - USER: DOCTOR + EMPLOYEE
     * - DOCTOR: USER + EMPLOYEE
     * - EMPLOYEE: USER + DOCTOR
     * - ADMIN: USER + DOCTOR + EMPLOYEE
     */
    @GetMapping("/partners")
    @PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'ADMIN', 'EMPLOYEE')")
    public SuccessReponse<List<ChatPartnerResponseDTO>> getChatPartners(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        if (username == null || username.isBlank()) {
            throw new truonggg.Exception.ForbiddenBusinessException("Người dùng chưa đăng nhập");
        }

        var current = userRepository.findByUserName(username)
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người dùng hiện tại"));

        String roleName = current.getRole() != null ? current.getRole().getRoleName() : null;
        if (roleName == null) {
            throw new truonggg.Exception.ForbiddenBusinessException("Người dùng hiện tại chưa được gán quyền");
        }

        java.util.Set<String> targetRoles = new java.util.HashSet<>();
        switch (roleName) {
        case "USER":
            targetRoles.add("DOCTOR");
            targetRoles.add("EMPLOYEE");
            break;
        case "DOCTOR":
            targetRoles.add("USER");
            targetRoles.add("EMPLOYEE");
            break;
        case "EMPLOYEE":
            targetRoles.add("USER");
            targetRoles.add("DOCTOR");
            break;
        case "ADMIN":
            targetRoles.add("USER");
            targetRoles.add("DOCTOR");
            targetRoles.add("EMPLOYEE");
            break;
        default:
            throw new truonggg.Exception.ForbiddenBusinessException("Role không được phép chat");
        }

        java.util.List<ChatPartnerResponseDTO> partners = new java.util.ArrayList<>();
        for (String targetRole : targetRoles) {
            for (var user : userRepository.findByRoleName(targetRole)) {
                // Không thêm chính mình vào danh sách
                if (user.getUserId().equals(current.getUserId())) {
                    continue;
                }
                ChatPartnerResponseDTO dto = new ChatPartnerResponseDTO();
                dto.setUserId(user.getUserId());
                dto.setFullName(user.getFullName());
                dto.setRoleName(targetRole);
                partners.add(dto);
            }
        }

        return SuccessReponse.of(partners);
    }

    /**
     * Trả về danh sách các đối tượng đã có hội thoại (conversation) với user hiện tại.
     * Dùng cho màn hình:
     * - Bác sĩ: danh sách bệnh nhân + nhân viên đã nhắn tin.
     * - Nhân viên: danh sách bệnh nhân + bác sĩ đã nhắn tin.
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'ADMIN', 'EMPLOYEE')")
    public SuccessReponse<List<ChatPartnerResponseDTO>> getConversations(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        if (username == null || username.isBlank()) {
            throw new truonggg.Exception.ForbiddenBusinessException("Người dùng chưa đăng nhập");
        }

        var current = userRepository.findByUserName(username)
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người dùng hiện tại"));

        Integer currentId = current.getUserId();
        String currentRoleName = current.getRole() != null ? current.getRole().getRoleName() : null;

        // Trường hợp nhân viên (EMPLOYEE): hiển thị được chat với toàn bộ user + doctor + employee, ngoại trừ admin và chính mình.
        if ("EMPLOYEE".equals(currentRoleName)) {
            java.util.List<ChatPartnerResponseDTO> all = new java.util.ArrayList<>();

            // Lấy toàn bộ USER, DOCTOR, EMPLOYEE
            for (var u : userRepository.findByRoleName("USER")) {
                if (!u.getUserId().equals(currentId)) {
                    ChatPartnerResponseDTO dto = new ChatPartnerResponseDTO();
                    dto.setUserId(u.getUserId());
                    dto.setFullName(u.getFullName());
                    dto.setRoleName("USER");
                    all.add(dto);
                }
            }
            for (var u : userRepository.findByRoleName("DOCTOR")) {
                if (!u.getUserId().equals(currentId)) {
                    ChatPartnerResponseDTO dto = new ChatPartnerResponseDTO();
                    dto.setUserId(u.getUserId());
                    dto.setFullName(u.getFullName());
                    dto.setRoleName("DOCTOR");
                    all.add(dto);
                }
            }
            for (var u : userRepository.findByRoleName("EMPLOYEE")) {
                if (!u.getUserId().equals(currentId)) {
                    ChatPartnerResponseDTO dto = new ChatPartnerResponseDTO();
                    dto.setUserId(u.getUserId());
                    dto.setFullName(u.getFullName());
                    dto.setRoleName("EMPLOYEE");
                    all.add(dto);
                }
            }

            return SuccessReponse.of(all);
        }

        // Các role khác: dựa theo lịch sử hội thoại (conversation) thực tế, sort theo tin nhắn gần nhất.
        java.util.List<Integer> orderedPartnerIds = chatMessageRepository.findConversationPartnerIds(currentId);
        java.util.Set<Integer> partnerIds = new java.util.LinkedHashSet<>(orderedPartnerIds);

        if (partnerIds.isEmpty()) {
            return SuccessReponse.of(List.of());
        }

        List<ChatPartnerResponseDTO> result = new java.util.ArrayList<>();
        for (Integer pid : partnerIds) {
            var user = userRepository.findById(pid)
                    .orElse(null);
            if (user == null || user.getRole() == null) {
                continue;
            }
            String partnerRole = user.getRole().getRoleName();

            // Lọc đối tượng theo role hiện tại:
            // - DOCTOR: chỉ hiển thị bệnh nhân (USER) + nhân viên
            // - USER, ADMIN: giữ nguyên danh sách
            boolean include = true;
            if ("DOCTOR".equals(currentRoleName)) {
                include = "USER".equals(partnerRole) || "EMPLOYEE".equals(partnerRole);
            }

            if (!include) {
                continue;
            }

            ChatPartnerResponseDTO dto = new ChatPartnerResponseDTO();
            dto.setUserId(user.getUserId());
            dto.setFullName(user.getFullName());
            dto.setRoleName(partnerRole);
            result.add(dto);
        }

        return SuccessReponse.of(result);
    }
}


