package qltv.com.repository;
import qltv.com.entity.Reader;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    Optional<Reader> findByUserId(Long userId);
    boolean existsByReaderCodeAndIdNot(String readerCode, Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reader r where r.user.id = :userId") Optional<Reader> lockByUserId(@Param("userId") Long userId);
}
