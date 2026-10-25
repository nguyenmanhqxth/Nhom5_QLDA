package qltv.com.controller;
import qltv.com.entity.*;
import qltv.com.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BorrowController {
    private final BorrowService borrows; private final UserService users; private final ReaderService readers;
    public BorrowController(BorrowService borrows, UserService users, ReaderService readers) {
        this.borrows=borrows; this.users=users; this.readers=readers;
    }
    @GetMapping("/admin/borrows") public String list(Model m,@RequestParam(required=false) String status) {
        m.addAttribute("borrows",borrows.findAll().stream().filter(b -> status==null || status.isBlank() || b.getStatus().name().equals(status)).toList());
        return "admin/borrows";
    }
    @PostMapping("/admin/borrows/approve/{id}") public String approve(@PathVariable Long id,Authentication auth,RedirectAttributes ra) {
        try { borrows.approveBorrow(id,users.findByUsername(auth.getName()).orElseThrow().getId()); ra.addFlashAttribute("success","Đã duyệt phiếu."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/admin/borrows";
    }
    @PostMapping("/admin/borrows/reject/{id}") public String reject(@PathVariable Long id,@RequestParam String reason,RedirectAttributes ra) {
        try { borrows.rejectBorrow(id,reason); ra.addFlashAttribute("success","Đã từ chối phiếu."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/admin/borrows";
    }
    @PostMapping("/admin/borrows/confirm-payment/{id}") public String payment(@PathVariable Long id,Authentication auth,RedirectAttributes ra) {
        try { borrows.confirmPayment(id,users.findByUsername(auth.getName()).orElseThrow().getId()); ra.addFlashAttribute("success","Đã nhận phí và giao sách."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/admin/borrows";
    }
    @PostMapping("/user/borrow/{bookId}") public String request(@PathVariable Long bookId,Authentication auth,RedirectAttributes ra) {
        try { borrows.requestBorrow(bookId,users.findByUsername(auth.getName()).orElseThrow().getId()); ra.addFlashAttribute("success","Đã gửi yêu cầu mượn."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/books/detail/"+bookId;
    }
    @GetMapping("/user/my-borrows") public String mine(Authentication auth,Model m) {
        Reader reader=readers.findByUserId(users.findByUsername(auth.getName()).orElseThrow().getId()).orElseThrow();
        m.addAttribute("borrows",borrows.findByReader(reader.getId())); return "user/my-borrows";
    }
    @GetMapping("/admin/borrows/receipt/{id}") public String adminReceipt(@PathVariable Long id,Model m) {
        m.addAttribute("borrow",borrows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "admin/receipt";
    }
    @GetMapping("/user/receipt/{id}") public String receipt(@PathVariable Long id,Authentication auth,Model m) {
        BorrowRecord r=borrows.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!r.getReader().getUser().getUsername().equals(auth.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.addAttribute("borrow",r); return "user/receipt";
    }
}
