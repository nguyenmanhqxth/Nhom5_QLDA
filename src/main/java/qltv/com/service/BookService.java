package qltv.com.service;

import qltv.com.entity.Book;
import qltv.com.entity.BorrowRecord;
import qltv.com.repository.BookRepository;
import qltv.com.repository.BorrowRecordRepository;
import qltv.com.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public List<Book> findAll() { return bookRepository.findAll(); }

    public Optional<Book> findById(Long id) { return bookRepository.findById(id); }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchByTitleOrAuthor(String keyword) {
        return bookRepository.searchByTitleOrAuthor(keyword);
    }

    public List<Book> findAvailable() { return bookRepository.findAll().stream().filter(b -> b.getAvailableCopies() > 0).toList(); }

    public List<Book> findByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    // Lấy 6 sách mới nhất cho trang chủ
    public List<Book> findLatest6() {
        List<Book> all = bookRepository.findTop6Latest();
        return all.stream().limit(6).collect(Collectors.toList());
    }

    // Tìm kiếm + lọc + sắp xếp cho Kho sách
    public List<Book> findWithFilters(String keyword, Long categoryId,
                                      String sort, boolean availableOnly) {
        List<Book> books;

        // Lấy dữ liệu theo filter
        if (keyword != null && !keyword.isEmpty() && categoryId != null) {
            books = bookRepository.searchByCategoryAndKeyword(categoryId, keyword);
        } else if (keyword != null && !keyword.isEmpty()) {
            books = bookRepository.searchByTitleOrAuthor(keyword);
        } else if (categoryId != null) {
            books = bookRepository.findByCategoryId(categoryId);
        } else {
            books = bookRepository.findAll();
        }

        // Lọc còn sách
        if (availableOnly) {
            books = books.stream()
                .filter(b -> b.getAvailableCopies() > 0)
                .collect(Collectors.toList());
        }

        // Sắp xếp
        if (sort != null) {
            switch (sort) {
                case "az" -> books.sort(Comparator.comparing(Book::getTitle));
                case "za" -> books.sort(Comparator.comparing(Book::getTitle).reversed());
                case "newest" -> books.sort(Comparator.comparing(Book::getId).reversed());
                case "oldest" -> books.sort(Comparator.comparing(Book::getId));
            }
        } else {
            books.sort(Comparator.comparing(Book::getId).reversed());
        }

        return books;
    }

    public Book save(Book book, Long categoryId) {
        if (book.getTitle()==null || book.getTitle().isBlank() || book.getTotalCopies()<0
            || book.getAvailableCopies()<0 || book.getAvailableCopies()>book.getTotalCopies()
            || !Double.isFinite(book.getPrice()) || book.getPrice()<0)
            throw new IllegalArgumentException("Thông tin sách hoặc số lượng không hợp lệ.");
        if (categoryId != null && categoryId > 0) {
            categoryRepository.findById(categoryId).ifPresent(book::setCategory);
        } else {
            book.setCategory(null);
        }
        book.setStatus(book.getAvailableCopies() > 0
            ? Book.BookStatus.AVAILABLE
            : Book.BookStatus.OUT_OF_STOCK);
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        if (borrowRecordRepository.existsByBookId(id))
            throw new IllegalArgumentException("Không thể xóa sách đã có phiếu mượn.");
        bookRepository.deleteById(id);
    }

    public long count() { return bookRepository.count(); }
}
