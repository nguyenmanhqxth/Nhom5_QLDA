package qltv.com.repository;
import qltv.com.entity.Book;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    boolean existsByCategoryId(Long categoryId);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByCategoryId(Long categoryId);
    @Query("select b from Book b order by b.id desc") List<Book> findTop6Latest();
    @Query("select b from Book b where lower(b.title) like lower(concat('%', :keyword, '%')) or lower(b.author) like lower(concat('%', :keyword, '%'))")
    List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);
    @Query("select b from Book b where b.category.id = :categoryId and (lower(b.title) like lower(concat('%', :keyword, '%')) or lower(b.author) like lower(concat('%', :keyword, '%')))")
    List<Book> searchByCategoryAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id") Optional<Book> lockById(@Param("id") Long id);
}
