package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;

public interface PoemService {
  PageResponse<PoemResponse> list(String keyword, Long genreId, int size, int page);

  PoemResponse detail(Long id);
}
