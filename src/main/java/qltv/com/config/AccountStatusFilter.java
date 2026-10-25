package qltv.com.config;
import qltv.com.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
@Component
public class AccountStatusFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public AccountStatusFilter(UserRepository users) { this.users=users; }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth!=null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")
            && users.findByUsername(auth.getName()).map(u -> !u.isEnabled()).orElse(true)) {
            SecurityContextHolder.clearContext();
            var session=req.getSession(false); if (session!=null) session.invalidate();
            res.sendRedirect(req.getContextPath()+"/auth/login?error=true"); return;
        }
        chain.doFilter(req,res);
    }
}
