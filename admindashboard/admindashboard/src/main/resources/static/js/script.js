/**
 * Toggles the visibility of the Chat Window
 */
function toggleChat() {
    const chatWindow = document.getElementById('chat-window');

    if (chatWindow.classList.contains('d-none')) {
        // Open Chat
        chatWindow.classList.remove('d-none');
        chatWindow.classList.add('fade-in-up'); // Add animation
    } else {
        // Close Chat
        chatWindow.classList.add('d-none');
        chatWindow.classList.remove('fade-in-up');
    }
}

/**
 * Simulates the API call with Strict Validation
 * @param {string} type - Either 'gst' or 'pan'
             "currently using dummy data to show a fake verification process which will be replaced later"
 */
function simulateVerification(type) {
    const inputField = document.getElementById(type + 'Input');
    const resultBox = document.getElementById(type + 'Result');

    // Convert input to uppercase automatically for better UX
    const value = inputField.value.toUpperCase();
    inputField.value = value;

    // --- 1. Define Strict Regex Patterns ---

    // PAN: 5 Letters, 4 Numbers, 1 Letter (Total 10)
    // ^ = Start, [A-Z]{5} = 5 letters, [0-9]{4} = 4 numbers, [A-Z]{1} = 1 letter, $ = End
    const panRegex = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;

    // GSTIN: 2 Numbers (State), 10 Chars (PAN), 1 Char (Entity), 'Z', 1 Char (Check) (Total 15)
    // ^[0-9]{2} = State Code
    // [A-Z]{5}[0-9]{4}[A-Z]{1} = Linked PAN
    // [1-9A-Z]{1} = Entity Number
    // Z = Default Z
    // [0-9A-Z]{1} = Check Code
    const gstRegex = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;

    let isValid = false;
    let errorMessage = "";

    // --- 2. Check Validation based on Type ---

    if (type === 'pan') {
        if (panRegex.test(value)) {
            isValid = true;
        } else {
            errorMessage = "Invalid PAN format! Required: 5 Letters, 4 Numbers, 1 Letter (e.g., ABCDE1234F)";
        }
    } else if (type === 'gst') {
        if (gstRegex.test(value)) {
            isValid = true;
        } else {
            errorMessage = "Invalid GSTIN! Format: 2 Digits + PAN + Entity + 'Z' + Check Code.";
        }
    }

    // --- 3. Show Error or Proceed ---

    if (!isValid) {
        alert(errorMessage);
        inputField.classList.add('is-invalid'); // Highlights box in red (Bootstrap feature)
        return;
    } else {
        inputField.classList.remove('is-invalid');
        inputField.classList.add('is-valid'); // Highlights box in green
    }

    // --- 4. Show "Searching..." and then "Success" ---

    resultBox.classList.remove('d-none', 'alert-success', 'alert-danger');
    resultBox.classList.add('alert-info');
    resultBox.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Verifying with Govt Portal...';

    setTimeout(() => {
        resultBox.classList.remove('alert-info');
        resultBox.classList.add('alert-success');

        if (type === 'gst') {
            resultBox.innerHTML = '<i class="fas fa-check-circle me-2"></i> <strong>Active:</strong> M/S SHARMA TRADERS';
        } else {
            resultBox.innerHTML = '<i class="fas fa-check-circle me-2"></i> <strong>Valid:</strong> ABHISEK BHARTI';
        }
    }, 1500);
}

/* === File Upload Logic (Updated) === */
/**
 * 1. Triggers the hidden file input when the button is clicked
 */
function triggerUpload(button) {
    const hiddenInput = button.nextElementSibling;
    hiddenInput.click();
}

/**
 * 2. Handles the file selection (Supports Replace & Remove)
 */
function handleFileUpload(input) {
    if (input.files && input.files[0]) {
        const fileName = input.files[0].name;
        const uploadBtn = input.previousElementSibling; // The main button
        const parentTd = uploadBtn.parentElement;       // The table cell

        // A. Simulate Uploading State
        uploadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
        uploadBtn.classList.remove('btn-outline-secondary', 'btn-success');
        uploadBtn.classList.add('btn-outline-primary');

        setTimeout(() => {
            // B. Update Button to Show Filename (Green)
            // Note: We leave the onclick="triggerUpload" so clicking this AGAIN lets them replace it
            uploadBtn.innerHTML = '<i class="fas fa-file-alt me-1"></i> ' + fileName;
            uploadBtn.classList.remove('btn-outline-primary');
            uploadBtn.classList.add('btn-success');
            uploadBtn.title = "Click to Replace File"; // Tooltip hint

            // C. Add a "Remove" Button (if it doesn't exist yet)
            // We check if a remove button is already there to avoid adding two of them
            let removeBtn = parentTd.querySelector('.btn-remove');
            if (!removeBtn) {
                removeBtn = document.createElement('button');
                removeBtn.className = 'btn btn-sm btn-danger ms-2 btn-remove';
                removeBtn.innerHTML = '<i class="fas fa-trash"></i>'; // 'Dustbin' icon
                removeBtn.onclick = function() { removeFile(input, uploadBtn, removeBtn); };
                parentTd.appendChild(removeBtn);
            }
        }, 1500); // 1.5 second delay
    }
}

/**
 * 3. Removes the file and resets the UI
 */
function removeFile(input, uploadBtn, removeBtn) {
    // A. Clear the actual file input
    input.value = '';

    // B. Reset the Main Button to initial state
    uploadBtn.innerHTML = '<i class="fas fa-paperclip me-1"></i> Upload Proof';
    uploadBtn.classList.remove('btn-success');
    uploadBtn.classList.add('btn-outline-secondary');
    uploadBtn.removeAttribute('title');

    // C. Delete the Remove Button from the screen
    removeBtn.remove();
}