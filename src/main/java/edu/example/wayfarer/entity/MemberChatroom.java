package edu.example.wayfarer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class MemberChatroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberChatroomId;

    @ManyToOne
    @JoinColumn(name = "email")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

}
