package qltv.com;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import qltv.com.entity.*;
import qltv.com.entity.BorrowRecord.BorrowStatus;
import qltv.com.repository.*;
import qltv.com.service.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Sprint2IntegrationTests {
    @Autowired UserService users;
    @Autowired ReaderService readerService;
    @Autowired BorrowService borrows;
    @Autowired UserRepository userRepository;
    @Autowired ReaderRepository readers;
    @Autowired BookRepository books;
    @Autowired CategoryRepository categories;
    @Autowired MockMvc mvc;
    User reader;
    Book book;
    Long adminId;

    User register(String username) {
        User u=new User(); u.setUsername(username); u.setPassword("Reader123456");
        u.setFullName("Test Reader"); u.setPhone("0900000001"); u.setEmail(username+"@example.test");
        return users.registerUser(u);
    }
    @BeforeEach void setup() {
        reader=register("testreader"); adminId=userRepository.findByUsername("admin").orElseThrow().getId();
        book=new Book(); book.setTitle("Test Book"); book.setAuthor("Test Author"); book.setTotalCopies(1);
        book.setAvailableCopies(1); book.setStatus(Book.BookStatus.AVAILABLE); books.save(book);
    }
    @Test void registrationCreatesCardAndRejectsInvalidOrDuplicateData() {
        assertTrue(readers.findByUserId(reader.getId()).isPresent());
        assertNotEquals("Reader123456",reader.getPassword());
        assertThrows(IllegalArgumentException.class,()->register("testreader"));
        User invalid=new User(); invalid.setUsername("newreader"); invalid.setFullName("Test"); invalid.setPhone("09");
        invalid.setEmail("bad-email"); invalid.setPassword("123456");
        assertThrows(IllegalArgumentException.class,()->users.registerUser(invalid));
    }
    @Test void borrowApproveAndDeliverUpdatesStockAndAuditOnlyOnce() {
        BorrowRecord r=borrows.requestBorrow(book.getId(),reader.getId());
        assertEquals(BorrowStatus.PENDING,r.getStatus()); assertEquals(1,book.getAvailableCopies());
        borrows.approveBorrow(r.getId(),adminId); assertEquals(70000,r.getBorrowFee());
        assertEquals(BorrowStatus.CONFIRMED,r.getStatus());
        borrows.confirmPayment(r.getId(),adminId);
        assertEquals(BorrowStatus.BORROWED,r.getStatus()); assertEquals(0,book.getAvailableCopies());
        assertEquals(adminId,r.getStaff().getId()); assertNotNull(r.getPaymentConfirmedAt());
        assertEquals(LocalDate.now().plusDays(14),r.getDueDate());
        assertThrows(IllegalArgumentException.class,()->borrows.confirmPayment(r.getId(),adminId));
        assertEquals(0,book.getAvailableCopies());
    }
    @Test void rejectsDuplicateOutOfStockAndInvalidStateTransitions() {
        BorrowRecord r=borrows.requestBorrow(book.getId(),reader.getId());
        assertThrows(IllegalArgumentException.class,()->borrows.requestBorrow(book.getId(),reader.getId()));
        assertThrows(IllegalArgumentException.class,()->borrows.confirmPayment(r.getId(),adminId));
        book.setAvailableCopies(0); books.save(book);
        assertThrows(IllegalArgumentException.class,()->borrows.approveBorrow(r.getId(),adminId));
        assertThrows(IllegalArgumentException.class,()->borrows.requestBorrow(book.getId(),register("otherreader").getId()));
    }
    @Test void rejectionRequiresReasonAndPreservesStock() {
        BorrowRecord r=borrows.requestBorrow(book.getId(),reader.getId());
        assertThrows(IllegalArgumentException.class,()->borrows.rejectBorrow(r.getId()," "));
        borrows.rejectBorrow(r.getId(),"Thẻ cần cập nhật");
        assertEquals(BorrowStatus.REJECTED,r.getStatus()); assertEquals("Thẻ cần cập nhật",r.getNotes());
        assertEquals(1,book.getAvailableCopies());
        assertThrows(IllegalArgumentException.class,()->borrows.approveBorrow(r.getId(),adminId));
    }
    @Test void refusesOverdueExpiredAndLockedReader() {
        BorrowRecord r=borrows.requestBorrow(book.getId(),reader.getId());
        r.setStatus(BorrowStatus.BORROWED); r.setDueDate(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class,()->borrows.requestBorrow(book.getId(),reader.getId()));
        r.setStatus(BorrowStatus.REJECTED);
        Reader card=readers.findByUserId(reader.getId()).orElseThrow(); card.setMemberExpiry(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class,()->borrows.requestBorrow(book.getId(),reader.getId()));
        card.setMemberExpiry(LocalDate.now().plusYears(1)); reader.setEnabled(false);
        assertThrows(IllegalArgumentException.class,()->borrows.requestBorrow(book.getId(),reader.getId()));
    }
    @Test void lastCopyCannotBeDeliveredToSecondReader() {
        User other=register("otherreader");
        BorrowRecord one=borrows.requestBorrow(book.getId(),reader.getId());
        BorrowRecord two=borrows.requestBorrow(book.getId(),other.getId());
        borrows.approveBorrow(one.getId(),adminId); borrows.approveBorrow(two.getId(),adminId);
        borrows.confirmPayment(one.getId(),adminId);
        assertThrows(IllegalArgumentException.class,()->borrows.confirmPayment(two.getId(),adminId));
        assertEquals(0,book.getAvailableCopies()); assertEquals(BorrowStatus.CONFIRMED,two.getStatus());
    }
    @Test void readerProfileUpdateAndLockAreSaved() {
        Reader card=readers.findByUserId(reader.getId()).orElseThrow();
        readerService.update(card.getId(),"Updated Reader","updated@example.test","0912345678","New address","DG-TEST",LocalDate.now().plusYears(2),4);
        assertEquals("Updated Reader",reader.getFullName()); assertEquals("DG-TEST",card.getReaderCode());
        readerService.setEnabled(card.getId(),false); assertFalse(reader.isEnabled());
        readerService.setEnabled(card.getId(),true); assertTrue(reader.isEnabled());
    }
    @Test void publicAndProtectedPagesRenderAndEnforceRoles() throws Exception {
        mvc.perform(get("/books/list")).andExpect(status().isOk());
        mvc.perform(get("/books/detail/"+book.getId())).andExpect(status().isOk());
        mvc.perform(get("/auth/register")).andExpect(status().isOk());
        mvc.perform(get("/admin/readers")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/readers").with(user("testreader").roles("USER"))).andExpect(status().isForbidden());
        for (String path:new String[]{"/admin/dashboard","/admin/books","/admin/books/new","/admin/books/edit/"+book.getId(),"/admin/categories","/admin/readers","/admin/borrows"})
            mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        Long card=readers.findByUserId(reader.getId()).orElseThrow().getId();
        mvc.perform(get("/admin/readers/edit/"+card).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        for (String path:new String[]{"/user/home","/user/profile","/user/my-borrows"})
            mvc.perform(get(path).with(user("testreader").roles("USER"))).andExpect(status().isOk());
    }
    @Test void readersCannotReadEachOthersReceiptsAndLockedSessionsAreStopped() throws Exception {
        BorrowRecord r=borrows.requestBorrow(book.getId(),reader.getId()); register("otherreader");
        mvc.perform(get("/user/receipt/"+r.getId()).with(user("otherreader").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/user/receipt/"+r.getId()).with(user("testreader").roles("USER"))).andExpect(status().isOk());
        mvc.perform(get("/admin/borrows/receipt/"+r.getId()).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        reader.setEnabled(false);
        mvc.perform(get("/user/home").with(user("testreader").roles("USER"))).andExpect(status().is3xxRedirection());
    }
    @Test void categoryCrudAndInUseProtectionWork() throws Exception {
        mvc.perform(post("/admin/categories/save").with(user("admin").roles("ADMIN")).with(csrf()).param("name","Test Category")).andExpect(status().is3xxRedirection());
        Category c=categories.findByName("Test Category").orElseThrow();
        mvc.perform(post("/admin/categories/save").with(user("admin").roles("ADMIN")).with(csrf()).param("id",c.getId().toString()).param("name","Renamed")).andExpect(status().is3xxRedirection());
        assertEquals("Renamed",c.getName()); book.setCategory(c); books.saveAndFlush(book);
        mvc.perform(post("/admin/categories/delete/"+c.getId()).with(user("admin").roles("ADMIN")).with(csrf())).andExpect(flash().attributeExists("error"));
        book.setCategory(null); books.saveAndFlush(book);
        mvc.perform(post("/admin/categories/delete/"+c.getId()).with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(categories.existsById(c.getId()));
    }
    @Test void registrationConfirmationCsrfAndLoginAreChecked() throws Exception {
        mvc.perform(post("/auth/register").with(csrf()).param("confirmPassword","different").param("password","123456")).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/auth/login").with(csrf()).param("username","testreader").param("password","Reader123456")).andExpect(redirectedUrl("/auth/redirect"));
        mvc.perform(post("/auth/login").with(csrf()).param("username","testreader").param("password","wrong")).andExpect(redirectedUrl("/auth/login?error=true"));
        mvc.perform(post("/user/borrow/"+book.getId()).with(user("testreader").roles("USER"))).andExpect(status().isForbidden());
    }
    @Test void laterSprintEndpointsAreUnavailable() throws Exception {
        for (String path:new String[]{"/admin/statistics","/admin/staff"})
            mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isNotFound());
        mvc.perform(post("/admin/borrows/return/1").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/user/renew/1").with(user("testreader").roles("USER")).with(csrf())).andExpect(status().isNotFound());
    }
}
