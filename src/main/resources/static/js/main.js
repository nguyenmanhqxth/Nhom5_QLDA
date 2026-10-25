document.addEventListener('DOMContentLoaded', function () {
    const sidebarBtn = document.getElementById('sidebarCollapse');
    const sidebar = document.getElementById('sidebar');

    // Tạo overlay
    const overlay = document.createElement('div');
    overlay.id = 'sidebarOverlay';
    overlay.style.cssText = 'display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;';
    document.body.appendChild(overlay);

    if (sidebarBtn && sidebar) {
        sidebarBtn.addEventListener('click', function () {
            if (window.innerWidth <= 768) {
                sidebar.classList.toggle('show');
                overlay.style.display = sidebar.classList.contains('show') ? 'block' : 'none';
            } else {
                sidebar.classList.toggle('collapsed');
            }
        });
    }

    overlay.addEventListener('click', function () {
        sidebar.classList.remove('show');
        overlay.style.display = 'none';
    });

    // Auto-hide alerts after 4 seconds
    document.querySelectorAll('.alert').forEach(function (alert) {
        setTimeout(function () {
            if (alert && alert.classList.contains('show')) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 4000);
    });
});