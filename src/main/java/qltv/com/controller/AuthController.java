package qltv.com.controller;
import qltv.com.entity.User;
import qltv.com.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }
    @GetMapping("/") public String index() { return "redirect:/books/list"; }
    @GetMapping("/auth/login") public String login() { return "auth/login"; }
    @GetMapping("/auth/register") public String register(Model model) {
        model.addAttribute("user", new User()); return "auth/register";
    }
    @PostMapping("/auth/register")
    public String register(@ModelAttribute User user, @RequestParam String confirmPassword,
                           RedirectAttributes ra) {
        try {
            if (!java.util.Objects.equals(user.getPassword(), confirmPassword))
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
            users.registerUser(user);
            ra.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", ex instanceof IllegalArgumentException ? ex.getMessage() : "Tên đăng nhập hoặc email đã tồn tại.");
            return "redirect:/auth/register";
        }
    }
    @GetMapping("/auth/redirect") public String redirect(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "redirect:/admin/dashboard" : "redirect:/user/home";
    }
}
