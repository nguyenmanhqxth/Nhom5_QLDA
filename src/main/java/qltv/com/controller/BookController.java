package qltv.com.controller;

import qltv.com.entity.*;
import qltv.com.repository.CategoryRepository;
import qltv.com.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {
    private final BookService bookService;
    private final CategoryRepository categoryRepository;

    public BookController(BookService bookService,
                          CategoryRepository categoryRepository) {
        this.bookService = bookService;
        this.categoryRepository = categoryRepository;

    }

    // ===== ADMIN =====
    @GetMapping("/admin/books")
    public String adminBooks(Model model,
                             @RequestParam(required = false) String search) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("books", bookService.searchByTitle(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("books", bookService.findAll());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/books";
    }

    @GetMapping("/admin/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/book-form";
    }

    @GetMapping("/admin/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/book-form";
    }

    @PostMapping("/admin/books/save")
    public String saveBook(@ModelAttribute Book book,
                           @RequestParam(name = "categoryId", required = false) Long categoryId,
                           RedirectAttributes ra) {
        try { bookService.save(book, categoryId); }
        catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error",e.getMessage()); return "redirect:/admin/books";
        }
        ra.addFlashAttribute("success",
            book.getId() != null ? "Cập nhật sách thành công!" : "Thêm sách thành công!");
        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.delete(id);
            ra.addFlashAttribute("success", "Xóa sách thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // ===== TRANG CHỦ — chỉ hiện 6 sách mới nhất =====
    @GetMapping({"/user/home"})
    public String home(Model model) {
        model.addAttribute("books", bookService.findLatest6());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("totalBooks", bookService.count());
        return "user/home";
    }

    // ===== KHO SÁCH — đầy đủ tìm kiếm + lọc + sắp xếp =====
    @GetMapping("/books/list")
    public String bookList(Model model,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {

        model.addAttribute("books",
            bookService.findWithFilters(search, categoryId, sort, availableOnly));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("availableOnly", availableOnly);
        return "user/book-list";
    }

    // ===== CHI TIẾT SÁCH =====
    @GetMapping("/books/detail/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        model.addAttribute("book", book);
        return "user/book-detail";
    }
}
