// === UNIVERSAL THEME ENGINE (BOOTSTRAP 5.3 NATIVE) ===

// 1. Check local storage immediately
const savedTheme = localStorage.getItem('whitecircle-theme') || 'light';

// MAGIC LINE: This tells Bootstrap 5 to switch its entire color palette!
document.documentElement.setAttribute('data-bs-theme', savedTheme);

// 2. Function to toggle the theme
function toggleDarkMode() {
    const currentTheme = document.documentElement.getAttribute('data-bs-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

    // Apply to HTML tag
    document.documentElement.setAttribute('data-bs-theme', newTheme);

    // Save to browser memory
    localStorage.setItem('whitecircle-theme', newTheme);

    // Sync UI
    updateToggleUI(newTheme);
}

// 3. Keep the toggle switch UI in sync
function updateToggleUI(theme) {
    const toggleCheckbox = document.getElementById('darkModeToggle');
    if (toggleCheckbox) {
        toggleCheckbox.checked = (theme === 'dark');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    updateToggleUI(document.documentElement.getAttribute('data-bs-theme'));
});