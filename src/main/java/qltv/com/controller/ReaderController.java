package qltv.com.controller;
import qltv.com.service.ReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Locale;

@Controller @RequestMapping("/admin/readers")
public class ReaderController {
    private final ReaderService readers;
    public ReaderController(ReaderService readers) { this.readers=readers; }
    @GetMapping public String list(@RequestParam(defaultValue="") String search,Model m) {
        String q=search.toLowerCase(Locale.ROOT).trim();
        m.addAttribute("readers",readers.findAll().stream().filter(r ->
            (r.getReaderCode()+" "+r.getUser().getFullName()+" "+r.getUser().getUsername()+" "+r.getUser().getEmail()+" "+r.getUser().getPhone()).toLowerCase(Locale.ROOT).contains(q)).toList());
        m.addAttribute("search",search); return "admin/readers";
    }
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id,Model m) {
        m.addAttribute("reader",readers.findById(id).orElseThrow()); return "admin/reader-form";
    }
    @PostMapping("/save/{id}") public String save(@PathVariable Long id,@RequestParam String fullName,@RequestParam String email,
        @RequestParam String phone,@RequestParam(defaultValue="") String address,@RequestParam String readerCode,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate memberExpiry,@RequestParam int maxBorrowAllowed,RedirectAttributes ra) {
        try { readers.update(id,fullName,email,phone,address,readerCode,memberExpiry,maxBorrowAllowed); ra.addFlashAttribute("success","Đã cập nhật độc giả."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error",e.getMessage()); return "redirect:/admin/readers/edit/"+id; }
        return "redirect:/admin/readers";
    }
    @PostMapping("/enabled/{id}") public String enabled(@PathVariable Long id,@RequestParam boolean enabled,RedirectAttributes ra) {
        readers.setEnabled(id,enabled); ra.addFlashAttribute("success",enabled ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản."); return "redirect:/admin/readers";
    }
}
