package qltv.com.service;
import qltv.com.entity.*;
import qltv.com.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReaderService {
    private final ReaderRepository readers; private final UserRepository users;
    public ReaderService(ReaderRepository readers,UserRepository users) { this.readers=readers; this.users=users; }
    public List<Reader> findAll() { return readers.findAll(); }
    public Optional<Reader> findById(Long id) { return readers.findById(id); }
    public Optional<Reader> findByUserId(Long id) { return readers.findByUserId(id); }
    @Transactional
    public void update(Long id,String fullName,String email,String phone,String address,String code,LocalDate expiry,int limit) {
        Reader r=readers.findById(id).orElseThrow();
        if (fullName.isBlank() || email.isBlank() || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || phone.isBlank()
            || code.isBlank() || expiry==null || limit<1 || limit>20 || fullName.length()>255 || code.length()>50)
            throw new IllegalArgumentException("Vui lòng nhập đúng hồ sơ, mã thẻ, hạn thẻ và giới hạn mượn (1–20).");
        if (readers.existsByReaderCodeAndIdNot(code,id)) throw new IllegalArgumentException("Mã thẻ đã tồn tại.");
        if (users.findByEmail(email).filter(u -> !u.getId().equals(r.getUser().getId())).isPresent())
            throw new IllegalArgumentException("Email đã được sử dụng.");
        User u=r.getUser(); u.setFullName(fullName); u.setEmail(email); u.setPhone(phone); u.setAddress(address);
        r.setReaderCode(code); r.setMemberExpiry(expiry); r.setMaxBorrowAllowed(limit);
        users.save(u); readers.save(r);
    }
    @Transactional
    public void setEnabled(Long id,boolean enabled) {
        Reader found=readers.findById(id).orElseThrow();
        Reader r=readers.lockByUserId(found.getUser().getId()).orElseThrow();
        r.getUser().setEnabled(enabled); users.save(r.getUser());
    }
}
