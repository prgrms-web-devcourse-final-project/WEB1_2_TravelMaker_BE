package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

}
