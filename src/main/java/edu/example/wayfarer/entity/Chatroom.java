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
public class Chatroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatroomId;

}
