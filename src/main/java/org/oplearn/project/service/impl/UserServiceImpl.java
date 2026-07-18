package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.UserRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.UserResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.EmailAlreadyExistedException;
import org.oplearn.project.exception.UserNotFoundException;
import org.oplearn.project.exception.UsernameAlreadyExistedException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.ROLE_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse create(UserRequest request) {
    if (repository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(request.getEmail())
          && repository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    User user = User.builder()
          .username(request.getUsername())
          .password(passwordEncoder.encode(request.getPassword()))
          .name(request.getName())
          .email(request.getEmail())
          .role(ROLE_USER)
          .build();

    return UserResponse.from(repository.save(user));
  }

  @Override
  @Transactional
  public UserResponse update(UserRequest request, Long id) {
    User user = repository.findByIdAndIsDeletedFalse(id)
          .orElseThrow(UserNotFoundException::new);

    if (!Objects.equals(user.getUsername(), request.getUsername())
          && repository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(request.getEmail())
          && !Objects.equals(user.getEmail(), request.getEmail())
          && repository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setName(request.getName());
    user.setEmail(request.getEmail());

    return UserResponse.from(repository.save(user));
  }

  @Override
  public PageResponse<UserResponse> list(String keyword, int size, int page, boolean isAll) {
    Pageable pageable = isAll ? Pageable.unpaged() : PageRequest.of(page, size);

    Page<User> users = StringUtils.hasText(keyword)
          ? repository.search(keyword, pageable)
          : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
          users.map(UserResponse::from).getContent(),
          (int) users.getTotalElements()
    );
  }

  @Override
  public UserResponse detail(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
          .map(UserResponse::from)
          .orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (repository.findByIdAndIsDeletedFalse(id).isEmpty()) {
      throw new UserNotFoundException();
    }
    repository.softDeleteById(id);
  }
}
