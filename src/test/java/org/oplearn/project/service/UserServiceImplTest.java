package org.oplearn.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oplearn.project.dto.request.UserRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.UserResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.UserNotFoundException;
import org.oplearn.project.exception.UsernameAlreadyExistedException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.service.impl.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock
  private UserRepository repository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl service;

  private UserRequest request;

  @BeforeEach
  void setUp() {
    request = new UserRequest("tu.nguyen", "password123", "Tu Nguyen", "tu@example.com");
  }

  @Test
  void create_shouldEncodePasswordAndSave() {
    when(repository.existsByUsernameAndIsDeletedFalse("tu.nguyen")).thenReturn(false);
    when(repository.existsByEmailAndIsDeletedFalse("tu@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded");
    when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse response = service.create(request);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(repository).save(captor.capture());
    assertEquals("encoded", captor.getValue().getPassword());
    assertEquals("tu.nguyen", response.getUsername());
  }

  @Test
  void create_shouldThrowConflict_whenUsernameExists() {
    when(repository.existsByUsernameAndIsDeletedFalse("tu.nguyen")).thenReturn(true);

    assertThrows(UsernameAlreadyExistedException.class, () -> service.create(request));
    verify(repository, never()).save(any());
  }

  @Test
  void detail_shouldThrowNotFound_whenUserMissing() {
    when(repository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.detail(99L));
  }

  @Test
  void list_shouldReturnPagedContentWithTotal() {
    User user = User.builder().username("tu.nguyen").password("x").build();
    Page<User> page = new PageImpl<>(List.of(user));
    when(repository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(page);

    PageResponse<UserResponse> response = service.list(null, 10, 0, false);

    assertEquals(1, response.getAmount());
    assertEquals("tu.nguyen", response.getContent().get(0).getUsername());
  }

  @Test
  void delete_shouldSoftDelete_whenUserExists() {
    User user = User.builder().username("tu.nguyen").password("x").build();
    when(repository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(user));

    service.delete(1L);

    verify(repository).softDeleteById(1L);
  }

  @Test
  void delete_shouldThrowNotFound_whenUserMissing() {
    when(repository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.delete(1L));
    verify(repository, never()).softDeleteById(anyLong());
  }
}
