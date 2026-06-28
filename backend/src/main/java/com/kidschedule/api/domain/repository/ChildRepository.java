package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.Child;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, UUID> {

	List<Child> findByFamilyIdOrderByNicknameAsc(UUID familyId);
}
