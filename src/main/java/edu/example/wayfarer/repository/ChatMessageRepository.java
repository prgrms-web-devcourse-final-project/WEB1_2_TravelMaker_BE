package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
}
