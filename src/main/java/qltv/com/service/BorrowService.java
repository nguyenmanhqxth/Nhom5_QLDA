package qltv.com.service;
import qltv.com.entity.*;
import qltv.com.entity.BorrowRecord.BorrowStatus;
import qltv.com.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class BorrowService {
    private final BorrowRecordRepository records;
    private final BookRepository books;
    private final ReaderRepository readers;
    private final UserRepository users;
    private final jakarta.persistence.EntityManager entityManager;
    public BorrowService(BorrowRecordRepository records, BookRepository books, ReaderRepository readers, UserRepository users, jakarta.persistence.EntityManager entityManager) {
        this.records=records; this.books=books; this.readers=readers; this.users=users;
        this.entityManager=entityManager;
    }
    public List<BorrowRecord> findAll() { return records.findAll(); }
    public Optional<BorrowRecord> findById(Long id) { return records.findById(id); }
    public List<BorrowRecord> findByReader(Long id) { return records.findByReaderId(id); }
    public long countPending() { return records.findByStatus(BorrowStatus.PENDING).size(); }
    private boolean active(BorrowRecord r) {
        return r.getStatus()==BorrowStatus.PENDING || r.getStatus()==BorrowStatus.CONFIRMED
            || r.getStatus()==BorrowStatus.BORROWED || r.getStatus()==BorrowStatus.OVERDUE;
    }
    private void eligible(Reader reader) {
        if (!reader.getUser().isEnabled()) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
        if (reader.getMemberExpiry()==null || reader.getMemberExpiry().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Thẻ độc giả đã hết hạn.");
        if (records.findByReaderId(reader.getId()).stream().anyMatch(r -> r.getStatus()==BorrowStatus.OVERDUE
            || (r.getStatus()==BorrowStatus.BORROWED && r.getDueDate()!=null && r.getDueDate().isBefore(LocalDate.now()))))
            throw new IllegalArgumentException("Bạn có sách quá hạn, chưa thể mượn thêm.");
    }
    @Transactional
    public BorrowRecord requestBorrow(Long bookId, Long userId) {
        Reader reader=readers.lockByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thẻ độc giả."));
        eligible(reader);
        Book book=books.lockById(bookId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));
        if (book.getAvailableCopies()<=0 || book.getStatus()==Book.BookStatus.DISCONTINUED)
            throw new IllegalArgumentException("Sách không còn để mượn.");
        List<BorrowRecord> history=records.findByReaderId(reader.getId());
        if (history.stream().anyMatch(r -> active(r) && r.getBook().getId().equals(bookId)))
            throw new IllegalArgumentException("Bạn đã có yêu cầu hoặc đang mượn sách này.");
        if (history.stream().filter(this::active).count()>=reader.getMaxBorrowAllowed())
            throw new IllegalArgumentException("Đã đạt giới hạn số sách được mượn.");
        return records.save(new BorrowRecord(reader,book,LocalDate.now(),LocalDate.now().plusDays(14),BorrowStatus.PENDING));
    }
    private User admin(Long id) {
        User user=users.findById(id).orElseThrow();
        if (!user.isEnabled() || user.getRole()!=User.Role.ADMIN) throw new IllegalArgumentException("Cần quyền Admin.");
        return user;
    }
    private BorrowRecord pending(Long id) {
        BorrowRecord r=records.lockById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu."));
        if (r.getStatus()!=BorrowStatus.PENDING) throw new IllegalArgumentException("Phiếu không còn ở trạng thái chờ duyệt.");
        return r;
    }
    @Transactional
    public BorrowRecord approveBorrow(Long id, Long staffId) {
        admin(staffId);
        BorrowRecord r=pending(id); eligible(r.getReader());
        Book book=books.lockById(r.getBook().getId()).orElseThrow();
        entityManager.refresh(book, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (book.getAvailableCopies()<=0) throw new IllegalArgumentException("Sách đã hết.");
        r.setReceiptCode("PM"+String.format("%06d",id)); r.setBorrowFee(70000);
        r.setStatus(BorrowStatus.CONFIRMED);
        return records.save(r);
    }
    @Transactional
    public void rejectBorrow(Long id, String reason) {
        if (reason==null || reason.isBlank() || reason.length()>255)
            throw new IllegalArgumentException("Cần lý do từ chối từ 1 đến 255 ký tự.");
        BorrowRecord r=pending(id); r.setNotes(reason.trim()); r.setStatus(BorrowStatus.REJECTED); records.save(r);
    }
    @Transactional
    public BorrowRecord confirmPayment(Long id, Long staffId) {
        User actor=admin(staffId);
        // Lock the reader first, consistently with requestBorrow, to serialize account eligibility checks.
        Long readerUserId=records.findReaderUserId(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu."));
        Reader reader=readers.lockByUserId(readerUserId).orElseThrow();
        BorrowRecord r=records.lockById(id).orElseThrow();
        if (r.getStatus()!=BorrowStatus.CONFIRMED) throw new IllegalArgumentException("Chỉ giao sách với phiếu đã duyệt, chưa giao.");
        eligible(reader);
        Book book=books.lockById(r.getBook().getId()).orElseThrow();
        entityManager.refresh(book, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (book.getAvailableCopies()<=0) throw new IllegalArgumentException("Sách đã hết, không thể giao.");
        book.setAvailableCopies(book.getAvailableCopies()-1);
        book.setStatus(book.getAvailableCopies()==0 ? Book.BookStatus.OUT_OF_STOCK : Book.BookStatus.AVAILABLE);
        books.save(book);
        reader.setCurrentBorrowed(reader.getCurrentBorrowed()+1);
        r.setBorrowDate(LocalDate.now()); r.setDueDate(LocalDate.now().plusDays(14));
        r.setStaff(actor); r.setPaymentConfirmedAt(LocalDateTime.now()); r.setStatus(BorrowStatus.BORROWED);
        return records.save(r);
    }
}
