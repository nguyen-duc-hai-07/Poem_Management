package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.exception.PoemNotFoundException;
import org.oplearn.project.repository.PoemRepository;
import org.oplearn.project.service.PoemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoemServiceImpl implements PoemService {
  private final PoemRepository repository;

  @Override
  public PageResponse<PoemResponse> list(String keyword, Long genreId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);

    Page<Poem> poems;
    if (StringUtils.hasText(keyword)) {
      poems = repository.search(keyword, pageable);
    } else if (Objects.nonNull(genreId)) {
      poems = repository.findAllByGenreIdAndIsDeletedFalse(genreId, pageable);
    } else {
      poems = repository.findAllByIsDeletedFalse(pageable);
    }

    return PageResponse.of(
          poems.map(PoemResponse::fromSummary).getContent(),
          (int) poems.getTotalElements()
    );
  }

  @Override
  public PoemResponse detail(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
          .map(PoemResponse::from)
          .orElseThrow(PoemNotFoundException::new);
  }
}
