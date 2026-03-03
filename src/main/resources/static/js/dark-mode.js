/**
 * ZakiBooking - Dark Mode Manager
 * - Tự động detect OS preference (prefers-color-scheme)
 * - Lưu lựa chọn thủ công vào localStorage
 * - Không có flash màu trắng (FOUC) nhờ script inline trong layout
 */
(function () {
    const STORAGE_KEY = 'zakibooking-theme';
    const DARK = 'dark';
    const LIGHT = 'light';

    /**
     * Lấy theme hiện tại:
     * 1. Ưu tiên localStorage (user đã chọn)
     * 2. Fallback về OS preference
     */
    function getPreferredTheme() {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (saved) return saved;
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? DARK : LIGHT;
    }

    /**
     * Áp dụng theme lên <html>
     */
    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        updateToggleIcon(theme);
    }

    /**
     * Cập nhật icon nút toggle
     */
    function updateToggleIcon(theme) {
        const btn = document.getElementById('darkModeToggle');
        if (!btn) return;
        if (theme === DARK) {
            btn.innerHTML = '☀️';
            btn.title = 'Chuyển sang chế độ sáng';
        } else {
            btn.innerHTML = '🌙';
            btn.title = 'Chuyển sang chế độ tối';
        }
    }

    /**
     * Toggle dark/light và lưu vào localStorage
     */
    function toggleTheme() {
        const current = document.documentElement.getAttribute('data-theme') || LIGHT;
        const next = current === DARK ? LIGHT : DARK;
        localStorage.setItem(STORAGE_KEY, next);
        applyTheme(next);
    }

    // === INIT ===

    // Áp dụng theme ngay khi script chạy (tránh flash)
    applyTheme(getPreferredTheme());

    // Khi DOM sẵn sàng, gắn event listener vào nút toggle
    document.addEventListener('DOMContentLoaded', function () {
        const btn = document.getElementById('darkModeToggle');
        if (btn) {
            btn.addEventListener('click', toggleTheme);
        }

        // Cập nhật icon lần đầu
        updateToggleIcon(getPreferredTheme());
    });

    // Lắng nghe OS thay đổi preference (chỉ khi user CHƯA chọn thủ công)
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
        const userOverride = localStorage.getItem(STORAGE_KEY);
        if (!userOverride) {
            applyTheme(e.matches ? DARK : LIGHT);
        }
    });

})();
