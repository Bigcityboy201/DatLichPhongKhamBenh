package truonggg.chat.application.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.chat.application.ChatCommandService;
import truonggg.chat.application.ChatQueryService;
import truonggg.chat.domain.model.ChatMessage;
import truonggg.chat.domain.model.ChatMessage.SenderType;
import truonggg.chat.infrastructure.ChatMessageRepository;
import truonggg.chat.mapper.ChatMessageMapper;
import truonggg.dto.reponseDTO.ChatMessageResponseDTO;
import truonggg.dto.requestDTO.ChatMessageRequestDTO;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatCommandService, ChatQueryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessageResponseDTO sendMessage(ChatMessageRequestDTO request, String username) {
        String effectiveUsername = username;
        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            // Thử lấy từ SecurityContext (đã được set bởi WebSocketAuthChannelInterceptor)
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                effectiveUsername = auth.getName();
            }
        }

        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            throw new truonggg.Exception.ForbiddenBusinessException("Người dùng chưa đăng nhập, không thể gửi tin nhắn");
        }

        User sender = userRepository.findByUserName(effectiveUsername)
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người dùng cho username hiện tại"));

        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người nhận với id = " + request.getReceiverUserId()));

        // Ràng buộc ai được chat với ai dựa trên role
        enforceChatPermission(sender, receiver);

        // Map role -> SenderType trong domain dựa trên role của sender
        SenderType senderType = resolveSenderTypeFromUser(sender);

        // Application layer: xử lý logic tạo message, set thời gian, trạng thái đọc,...
        ChatMessage message = ChatMessage.builder()
                .senderUserId(sender.getUserId())
                .receiverUserId(receiver.getUserId())
                .senderType(senderType)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponseDTO dto = chatMessageMapper.toDto(saved);
        dto.setSenderName(buildDisplayName(sender));
        dto.setReceiverName(buildDisplayName(receiver));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getConversation(Integer partnerUserId, String username) {
        String effectiveUsername = username;
        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                effectiveUsername = auth.getName();
            }
        }
        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            throw new truonggg.Exception.ForbiddenBusinessException("Người dùng chưa đăng nhập, không thể xem hội thoại");
        }

        User current = userRepository.findByUserName(effectiveUsername)
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người dùng hiện tại"));

        User partner = userRepository.findById(partnerUserId)
                .orElseThrow(() -> new truonggg.Exception.NotFoundException("User",
                        "Không tìm thấy người dùng với id = " + partnerUserId));

        // Áp cùng rule như khi gửi tin nhắn: chỉ xem hội thoại nếu được phép chat
        enforceChatPermission(current, partner);

        String currentLabel = buildDisplayName(current);
        String partnerLabel = buildDisplayName(partner);

        return chatMessageRepository.findConversation(current.getUserId(), partner.getUserId())
                .stream()
                .map(m -> {
                    ChatMessageResponseDTO dto = chatMessageMapper.toDto(m);
                    // Trong 1 conversation chỉ có current <-> partner
                    if (m.getSenderUserId().equals(current.getUserId())) {
                        dto.setSenderName(currentLabel);
                        dto.setReceiverName(partnerLabel);
                    } else {
                        dto.setSenderName(partnerLabel);
                        dto.setReceiverName(currentLabel);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void enforceChatPermission(User sender, User receiver) {
        String senderRole = sender.getRole() != null ? sender.getRole().getRoleName() : null;
        String receiverRole = receiver.getRole() != null ? receiver.getRole().getRoleName() : null;

        if (senderRole == null || receiverRole == null) {
            throw new truonggg.Exception.ForbiddenBusinessException("Không xác định được quyền chat của người dùng");
        }

        boolean allowed = false;

        switch (senderRole) {
        case "USER":
            // Bệnh nhân chỉ được chat với bác sĩ hoặc nhân viên
            allowed = "DOCTOR".equals(receiverRole) || "EMPLOYEE".equals(receiverRole);
            break;
        case "DOCTOR":
            // Bác sĩ được chat với bệnh nhân hoặc nhân viên
            allowed = "USER".equals(receiverRole) || "EMPLOYEE".equals(receiverRole);
            break;
        case "EMPLOYEE":
            // Nhân viên được chat với bệnh nhân và bác sĩ
            allowed = "USER".equals(receiverRole) || "DOCTOR".equals(receiverRole);
            break;
        case "ADMIN":
            // Admin cho phép chat với tất cả
            allowed = true;
            break;
        default:
            allowed = false;
        }

        if (!allowed) {
            throw new truonggg.Exception.ForbiddenBusinessException("Bạn không có quyền chat với người dùng này");
        }
    }

    private SenderType resolveSenderTypeFromUser(User user) {
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            return SenderType.SYSTEM;
        }
        String roleName = user.getRole().getRoleName();
        if ("USER".equals(roleName)) {
            return SenderType.PATIENT;
        }
        if ("DOCTOR".equals(roleName)) {
            return SenderType.DOCTOR;
        }
        if ("EMPLOYEE".equals(roleName) || "ADMIN".equals(roleName)) {
            return SenderType.SYSTEM;
        }
        return SenderType.SYSTEM;
    }

    private String buildDisplayName(User user) {
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            return user.getFullName();
        }
        String roleName = user.getRole().getRoleName();
        return switch (roleName) {
        case "DOCTOR" -> "Bác sĩ " + user.getFullName();
        case "EMPLOYEE" -> "Nhân viên " + user.getFullName();
        case "USER" -> "Bệnh nhân " + user.getFullName();
        case "ADMIN" -> "Admin " + user.getFullName();
        default -> user.getFullName();
        };
    }
}


