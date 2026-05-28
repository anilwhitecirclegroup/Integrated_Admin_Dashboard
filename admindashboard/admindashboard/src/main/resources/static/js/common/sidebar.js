/* ==========================================
   Sidebar Active Link Detection
   ========================================== */
document.addEventListener('DOMContentLoaded', function () {

    var currentPath = window.location.pathname;
    var links = document.querySelectorAll('.sidebar-link[data-page]');

    links.forEach(function (link) {
        link.classList.remove('active');

        var pagePath = link.getAttribute('data-page');

        if (pagePath && currentPath === pagePath) {
            link.classList.add('active');
        }
    });

});
