package com.example.shop.repository;

import com.example.shop.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles; // profil testowy

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ReturnsUser_WhenExists() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPass");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByUsername("testuser");

        // then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("encodedPass", found.get().getPassword());
        assertEquals("ROLE_USER", found.get().getRole());
    }

    @Test
    void findByUsername_ReturnsEmpty_WhenNotExists() {
        // given - brak usera

        // when
        Optional<User> found = userRepository.findByUsername("noSuchUser");

        // then
        assertTrue(found.isEmpty());
    }

    @Test
    void save_PersistsUser_AndGeneratesId() {
        // given
        User user = new User("anotherUser", "somePass");
        user.setRole("ROLE_ADMIN");

        // when
        User saved = userRepository.save(user);

        // then
        assertNotNull(saved.getId());
        assertEquals("anotherUser", saved.getUsername());
        assertEquals("somePass", saved.getPassword());
        assertEquals("ROLE_ADMIN", saved.getRole());
    }

    @Test
    void uniqueUsername_ThrowsException_IfDuplicate() {
        // given
        User user1 = new User("duplicateUser", "pass1");
        user1.setRole("ROLE_USER");
        userRepository.save(user1);

        User user2 = new User("duplicateUser", "pass2");
        user2.setRole("ROLE_USER");

        // when + then
        // w zależności od konfiguracji,
        // JPA może rzucić DataIntegrityViolationException lub inną
        // (np. ConstraintViolationException) - zależnie od bazy i dialektu

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
