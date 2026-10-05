package com.datn.fashion.repository;
import com.datn.fashion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
 Optional<User> findByEmail(String email);
 Optional<User> findByPhoneNumber(String phoneNumber);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
 Optional<User> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
 boolean existsByEmail(String email);
 boolean existsByPhoneNumber(String phoneNumber);
 boolean existsByPhoneNumberAndIdNot(String phoneNumber,Long id);
}
