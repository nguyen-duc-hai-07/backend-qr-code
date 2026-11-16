package org.oplearn.project.repository;

import org.oplearn.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByIdAndIsDeletedFalse(Long id);

  Optional<User> findByUsernameAndIsDeletedFalse(String username);

  boolean existsByUsernameAndIsDeletedFalse(String username);

  boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);

  Optional<User> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

  Page<User> findAllByIsDeletedFalse(Pageable pageable);

  @Query("""
        select u from User u
        where u.isDeleted = false
          and (lower(u.fullName) like lower(concat('%', :keyword, '%'))
           or lower(u.username) like lower(concat('%', :keyword, '%'))
           or u.phoneNumber like concat('%', :keyword, '%'))
        """)
  Page<User> search(@Param("keyword") String keyword, Pageable pageable);

  @Modifying
  @Query("update User u set u.isDeleted = true where u.id = :id")
  void softDeleteById(@Param("id") Long id);
}
