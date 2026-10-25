package qltv.com.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "readers")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String readerCode;
    private LocalDate memberSince;
    private LocalDate memberExpiry;
    private int maxBorrowAllowed = 5;
    private int currentBorrowed = 0;

    public Reader() {}

    public Reader(User user, String readerCode, LocalDate memberSince, LocalDate memberExpiry) {
        this.user = user;
        this.readerCode = readerCode;
        this.memberSince = memberSince;
        this.memberExpiry = memberExpiry;
        this.maxBorrowAllowed = 5;
        this.currentBorrowed = 0;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getReaderCode() { return readerCode; }
    public void setReaderCode(String readerCode) { this.readerCode = readerCode; }

    public LocalDate getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDate memberSince) { this.memberSince = memberSince; }

    public LocalDate getMemberExpiry() { return memberExpiry; }
    public void setMemberExpiry(LocalDate memberExpiry) { this.memberExpiry = memberExpiry; }

    public int getMaxBorrowAllowed() { return maxBorrowAllowed; }
    public void setMaxBorrowAllowed(int maxBorrowAllowed) { this.maxBorrowAllowed = maxBorrowAllowed; }

    public int getCurrentBorrowed() { return currentBorrowed; }
    public void setCurrentBorrowed(int currentBorrowed) { this.currentBorrowed = currentBorrowed; }
}