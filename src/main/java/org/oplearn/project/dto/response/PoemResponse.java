package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Poem;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoemResponse {
  private static final int EXCERPT_LINES = 4;

  private Long id;
  private String title;
  private String content;
  private String sourceUrl;
  private String period;
  private String specificGenre;
  private String authorName;
  private String genreName;

  public static PoemResponse from(Poem poem) {
    return build(poem, poem.getContent());
  }

  public static PoemResponse fromSummary(Poem poem) {
    return build(poem, excerpt(poem.getContent()));
  }

  private static PoemResponse build(Poem poem, String content) {
    return new PoemResponse(
          poem.getId(),
          poem.getTitle(),
          content,
          poem.getSourceUrl(),
          poem.getPeriod(),
          poem.getSpecificGenre(),
          Objects.isNull(poem.getAuthor()) ? null : poem.getAuthor().getName(),
          Objects.isNull(poem.getGenre()) ? null : poem.getGenre().getName()
    );
  }

  private static String excerpt(String content) {
    String[] lines = content.split("\n");
    if (lines.length <= EXCERPT_LINES) {
      return content;
    }
    return String.join("\n", java.util.Arrays.copyOf(lines, EXCERPT_LINES)) + "\n...";
  }
}
