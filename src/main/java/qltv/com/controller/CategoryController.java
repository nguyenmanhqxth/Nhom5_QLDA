package qltv.com.controller;
import qltv.com.entity.Category;
import qltv.com.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryRepository categories; private final BookRepository books;
    public CategoryController(CategoryRepository categories,BookRepository books) { this.categories=categories; this.books=books; }
    @GetMapping public String list(Model m) { m.addAttribute("categories",categories.findAll()); return "admin/categories"; }
    @PostMapping("/save") @Transactional
    public String save(@RequestParam(required=false) Long id,@RequestParam String name,@RequestParam(defaultValue="") String description,RedirectAttributes ra) {
        if (name.isBlank() || name.length()>100 || description.length()>255) { ra.addFlashAttribute("error","Tên thể loại cần 1–100 ký tự; mô tả tối đa 255 ký tự."); return "redirect:/admin/categories"; }
        var duplicate=categories.findByName(name.trim());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) { ra.addFlashAttribute("error","Thể loại đã tồn tại."); return "redirect:/admin/categories"; }
        Category c=id==null ? new Category() : categories.findById(id).orElseThrow();
        c.setName(name.trim()); c.setDescription(description); categories.save(c); ra.addFlashAttribute("success","Đã lưu thể loại.");
        return "redirect:/admin/categories";
    }
    @PostMapping("/delete/{id}") @Transactional public String delete(@PathVariable Long id,RedirectAttributes ra) {
        if (books.existsByCategoryId(id)) ra.addFlashAttribute("error","Thể loại đang có sách, không thể xóa.");
        else { categories.deleteById(id); ra.addFlashAttribute("success","Đã xóa thể loại."); }
        return "redirect:/admin/categories";
    }
}
