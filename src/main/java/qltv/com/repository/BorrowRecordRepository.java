package qltv.com.repository;
import qltv.com.entity.BorrowRecord;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByReaderId(Long readerId);
    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);
    boolean existsByBookId(Long bookId);
    @Query("select b.reader.user.id from BorrowRecord b where b.id = :id")
    Optional<Long> findReaderUserId(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BorrowRecord b where b.id = :id") Optional<BorrowRecord> lockById(@Param("id") Long id);
}
