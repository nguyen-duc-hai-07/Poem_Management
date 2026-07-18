package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.service.PoemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_KEYWORD;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_PAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_SIZE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.PAGE_DEFAULT;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/poems")
public class PoemController {
  private final PoemService service;

  @GetMapping
  public ResponseGeneral<PageResponse<PoemResponse>> list(
        @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
        @RequestParam(name = "genre_id", required = false) Long genreId,
        @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
        @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) keyword: {}, genreId: {}, size: {}, page: {}", keyword, genreId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(keyword, genreId, size, page));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<PoemResponse> detail(@PathVariable Long id) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }
}
