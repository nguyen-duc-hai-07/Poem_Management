package org.oplearn.project.repository;

import org.oplearn.project.entity.Poem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PoemRepository extends JpaRepository<Poem, Long> {

  @EntityGraph(attributePaths = {"author", "genre"})
  Optional<Poem> findByIdAndIsDeletedFalse(Long id);

  @EntityGraph(attributePaths = {"author", "genre"})
  Page<Poem> findAllByIsDeletedFalse(Pageable pageable);

  @EntityGraph(attributePaths = {"author", "genre"})
  @Query("""
        select p from Poem p
        where p.isDeleted = false
          and (lower(p.title) like lower(concat('%', :keyword, '%'))
           or lower(p.author.name) like lower(concat('%', :keyword, '%')))
        """)
  Page<Poem> search(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "genre"})
  Page<Poem> findAllByGenreIdAndIsDeletedFalse(Long genreId, Pageable pageable);
}
