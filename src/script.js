
function goToCreateAccount() {
    window.location.href = "index.html";  // Path to account creation page
}

// Redirects to login page for existing users
function goToLogin() {
    window.location.href = "login.html";  // Path to login page
}

// Validate login inputs
function validateLogin() {
    const websiteName = document.getElementById("websiteName").value;
    const password = document.getElementById("password").value;
    
    // Perform basic validation here, like checking if fields are filled
    if (websiteName && password) {
        return true; // Allow form submission
    } else {
        alert("Please fill in all fields.");
        return false; // Prevent form submission
    }
}
