package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, String> {
}
