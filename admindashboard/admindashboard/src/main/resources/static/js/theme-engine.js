// === UNIVERSAL THEME ENGINE (BOOTSTRAP 5.3 NATIVE) ===

// 1. Check local storage immediately (Prevents light/dark flashing on load)
const savedTheme = localStorage.getItem('whitecircle-theme') || 'light';
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

// 4. Safely bind events once the DOM is ready (THE FIX)
document.addEventListener('DOMContentLoaded', () => {
    // Sync the toggle switch to match the saved theme
    updateToggleUI(document.documentElement.getAttribute('data-bs-theme'));

    // Dynamically attach the event listener.
    // This removes the need for an inline 'onchange' attribute in your HTML!
    const toggleCheckbox = document.getElementById('darkModeToggle');
    if (toggleCheckbox) {
        // Remove any old listeners just in case, then add the new one
        toggleCheckbox.removeEventListener('change', toggleDarkMode);
        toggleCheckbox.addEventListener('change', toggleDarkMode);
    }
});