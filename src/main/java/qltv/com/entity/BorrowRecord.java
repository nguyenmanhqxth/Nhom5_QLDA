package qltv.com.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    private String notes;
    private double borrowFee = 0;
    private String receiptCode;

    private LocalDateTime paymentConfirmedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum BorrowStatus { PENDING, CONFIRMED, BORROWED, RETURNED, OVERDUE, REJECTED }

    public BorrowRecord() {}

    public BorrowRecord(Reader reader, Book book, LocalDate borrowDate,
                        LocalDate dueDate, BorrowStatus status) {
        this.reader = reader;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public double getBorrowFee() { return borrowFee; }
    public void setBorrowFee(double borrowFee) { this.borrowFee = borrowFee; }
    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }
    public LocalDateTime getPaymentConfirmedAt() { return paymentConfirmedAt; }
    public void setPaymentConfirmedAt(LocalDateTime value) { paymentConfirmedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
