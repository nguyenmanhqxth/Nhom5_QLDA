package qltv.com.service;
import qltv.com.entity.*;
import qltv.com.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final ReaderRepository readers;
    private final PasswordEncoder encoder;
    public UserService(UserRepository users, ReaderRepository readers, PasswordEncoder encoder) {
        this.users=users; this.readers=readers; this.encoder=encoder;
    }
    public Optional<User> findByUsername(String username) { return users.findByUsername(username); }
    public boolean existsByUsername(String value) { return users.existsByUsername(value); }
    public boolean existsByEmail(String value) { return users.existsByEmail(value); }
    @Transactional
    public User registerUser(User input) {
        if (input.getUsername()==null || !input.getUsername().matches("[A-Za-z0-9_]{3,50}"))
            throw new IllegalArgumentException("Tên đăng nhập cần 3–50 chữ, số hoặc dấu gạch dưới.");
        if (input.getFullName()==null || input.getFullName().isBlank() || input.getPhone()==null || input.getPhone().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập họ tên và số điện thoại.");
        if (input.getEmail()==null || !input.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (input.getPassword()==null || input.getPassword().length()<6 || input.getPassword().length()>72)
            throw new IllegalArgumentException("Mật khẩu cần từ 6 đến 72 ký tự.");
        if (users.existsByUsername(input.getUsername()) || users.existsByEmail(input.getEmail()))
            throw new IllegalArgumentException("Tên đăng nhập hoặc email đã tồn tại.");
        User user = new User();
        user.setUsername(input.getUsername()); user.setFullName(input.getFullName());
        user.setEmail(input.getEmail()); user.setPhone(input.getPhone()); user.setAddress(input.getAddress());
        user.setPassword(encoder.encode(input.getPassword())); user.setRole(User.Role.USER); user.setEnabled(true);
        users.save(user);
        readers.save(new Reader(user, "DG"+String.format("%05d",user.getId()),LocalDate.now(),LocalDate.now().plusYears(1)));
        return user;
    }
}
