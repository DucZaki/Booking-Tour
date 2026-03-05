/**
 * ZakiBooking - Main Script
 */
document.addEventListener('DOMContentLoaded', function () {

    // =============================================
    // Splide Carousel - nếu có element #main-slider
    // =============================================
    if (document.getElementById('main-slider')) {
        new Splide('#main-slider', {
            type: 'loop',
            perPage: 1,
            autoplay: true,
            interval: 4000,
            pauseOnHover: true,
        }).mount();
    }

    // =============================================
    // Auto-dismiss alerts sau 5 giây
    // =============================================
    document.querySelectorAll('.alert.alert-dismissible').forEach(function (alert) {
        setTimeout(function () {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // =============================================
    // Active nav-link highlight dựa trên URL
    // =============================================
    var currentPath = window.location.pathname;
    document.querySelectorAll('.navbar .nav-link').forEach(function (link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
});
