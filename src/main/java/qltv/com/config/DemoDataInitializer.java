package qltv.com.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import qltv.com.entity.Book;
import qltv.com.entity.Category;
import qltv.com.entity.User;
import qltv.com.entity.Reader;
import qltv.com.repository.ReaderRepository;
import java.time.LocalDate;
import qltv.com.repository.BookRepository;
import qltv.com.repository.CategoryRepository;
import qltv.com.repository.UserRepository;

/** Sample data is created only when the demo profile is explicitly enabled. */
@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {
    private final ReaderRepository readers;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final BookRepository books;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository users, CategoryRepository categories,
                               BookRepository books, PasswordEncoder passwordEncoder, ReaderRepository readers) {
        this.readers = readers;
        this.users = users;
        this.categories = categories;
        this.books = books;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        createUser("reader01", "Reader123456", "reader01@example.test", "Độc giả mẫu", User.Role.USER);

        User reader = users.findByUsername("reader01").orElseThrow();
        if (readers.findByUserId(reader.getId()).isEmpty()) {
            readers.save(new Reader(reader, "DG"+String.format("%05d",reader.getId()),LocalDate.now(),LocalDate.now().plusYears(1)));
        }
        Category technology = category("Công nghệ thông tin");
        Category literature = category("Văn học");
        Category skills = category("Kỹ năng sống");

        createBook("DEMO-BOOK-001", "Nhập môn lập trình Java", technology, 5, 120000);
        createBook("DEMO-BOOK-002", "Cơ sở dữ liệu cho người mới", technology, 4, 95000);
        createBook("DEMO-BOOK-003", "Những câu chuyện bên cửa sổ", literature, 6, 75000);
        createBook("DEMO-BOOK-004", "Hành trình tuổi trẻ", literature, 3, 85000);
        createBook("DEMO-BOOK-005", "Quản lý thời gian hiệu quả", skills, 5, 90000);
        createBook("DEMO-BOOK-006", "Kỹ năng làm việc nhóm", skills, 4, 80000);
        System.out.println("Demo data ready: reader01 and 6 sample books (existing records preserved).");
    }

    private void createUser(String username, String password, String email, String name, User.Role role) {
        if (users.existsByUsername(username)) {
            return;
        }
        if (users.existsByEmail(email)) {
            throw new IllegalStateException("Cannot create demo account; email already exists: " + email);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(name);
        user.setPhone("0900000001");
        user.setRole(role);
        user.setEnabled(true);
        users.save(user);
    }

    private Category category(String name) {
        return categories.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription("Thể loại cho dữ liệu dùng thử.");
            return categories.save(category);
        });
    }

    private void createBook(String code, String title, Category category, int copies, double price) {
        if (books.existsByIsbn(code)) {
            return;
        }
        Book book = new Book();
        book.setIsbn(code);
        book.setTitle(title);
        book.setAuthor("Tác giả mẫu");
        book.setPublisher("Nhà xuất bản mẫu");
        book.setDescription("Sách giả lập để thử tìm kiếm, xem chi tiết và đăng ký mượn trên website.");
        book.setCategory(category);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        book.setPrice(price);
        book.setStatus(Book.BookStatus.AVAILABLE);
        books.save(book);
    }
}
