package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {

    Optional<Room> findByHostEmail(String hostEmail);

}
