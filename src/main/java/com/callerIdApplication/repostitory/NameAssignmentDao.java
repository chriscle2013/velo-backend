package com.callerIdApplication.repostitory;

import com.callerIdApplication.entity.NameAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NameAssignmentDao extends JpaRepository<NameAssignment, Long> {
    List<NameAssignment> findByPhoneNumberOrderByIdDesc(String phoneNumber);
}
