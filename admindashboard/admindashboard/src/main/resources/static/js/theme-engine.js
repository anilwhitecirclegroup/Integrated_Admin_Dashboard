// === UNIVERSAL THEME ENGINE (State-Based Fix) ===

// 1. Apply theme immediately on load
const savedTheme = localStorage.getItem('whitecircle-theme') || 'light';
document.documentElement.setAttribute('data-bs-theme', savedTheme);

// 2. Updated Function: Now it's "Double-Trigger Proof"
function toggleDarkMode() {
    const toggleCheckbox = document.getElementById('darkModeToggle');
    let newTheme;

    if (toggleCheckbox) {
        // IMPORTANT: We look at the checkbox state, not the current theme.
        // If it's checked, it's dark. If not, it's light.
        // Even if this runs 10 times, the result is the same!
        newTheme = toggleCheckbox.checked ? 'dark' : 'light';
    } else {
        // Fallback for pages without a checkbox (e.g., a simple button)
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    }

    document.documentElement.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem('whitecircle-theme', newTheme);

    // Sync any other UI elements if needed
    updateToggleUI(newTheme);
}

function updateToggleUI(theme) {
    const toggleCheckbox = document.getElementById('darkModeToggle');
    if (toggleCheckbox) {
        toggleCheckbox.checked = (theme === 'dark');
    }
}

// 3. Re-enable this block! It handles the Admin Dashboard.
document.addEventListener('DOMContentLoaded', () => {
    const currentTheme = document.documentElement.getAttribute('data-bs-theme');
    updateToggleUI(currentTheme);

    const toggleCheckbox = document.getElementById('darkModeToggle');
    if (toggleCheckbox) {
        // We add the listener for pages like Admin that don't have 'onchange'
        toggleCheckbox.addEventListener('change', toggleDarkMode);
    }
});