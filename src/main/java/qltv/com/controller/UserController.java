package qltv.com.controller;
import qltv.com.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class UserController {
    private final UserService users; private final ReaderService readers;
    public UserController(UserService users,ReaderService readers) { this.users=users; this.readers=readers; }
    @GetMapping("/user/profile") public String profile(Authentication auth,Model m) {
        var user=users.findByUsername(auth.getName()).orElseThrow();
        m.addAttribute("user",user); m.addAttribute("reader",readers.findByUserId(user.getId()).orElseThrow());
        return "user/profile";
    }
}
