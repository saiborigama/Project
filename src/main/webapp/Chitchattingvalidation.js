function updateMessage() {
    changeStyle();
  }
  
  function changeStyle() {
    const websiteNameInput = document.getElementById('websiteName').value.trim();
    const passwordInput = document.getElementById('password').value;
    const relationship = document.getElementById('relationship').value;
    const messageDiv = document.getElementById('message');
    const websiteLinkDiv = document.getElementById('websiteLink');
    const websiteAnchor = document.getElementById('websiteAnchor');
    
    // Save the password to validate later
    savedPassword = passwordInput;
  
    // Validation: Check if both website name and password are entered
    if (websiteNameInput === "" || passwordInput === "") {
      messageDiv.className = 'message';
      messageDiv.textContent = "Please enter the website name, password, and choose a relationship type.";
      websiteLinkDiv.style.display = 'none';  // Hide the website link
      return;
    }
  
    const websiteName = "bs." + websiteNameInput.toLowerCase().replace(/\s+/g, '') + ".com";  // Add 'bs.' and format
    
    if (relationship === "friend") {
      messageDiv.className = 'message friend';
      messageDiv.textContent = `Website: ${websiteName} - You have chosen: Friend!`;
    } else if (relationship === "lover") {
      messageDiv.className = 'message lover';
      messageDiv.textContent = `Website: ${websiteName} - You have chosen: Lover!`;
    } else if (relationship === "group") {
      messageDiv.className = 'message group';
      messageDiv.textContent = `Website: ${websiteName} - You have chosen: Group Members!`;
    } else if (relationship === "business") {
      messageDiv.className = 'message business';
      messageDiv.textContent = `Website: ${websiteName} - You have chosen: Business!`;
    } else {
      messageDiv.className = 'message';
      messageDiv.textContent = `Website: ${websiteName} - Please choose a relationship type.`;
    }
    
    // Show the website link and set its URL
    websiteAnchor.href = `website.html?name=${websiteName}`;
    websiteAnchor.textContent = `Visit ${websiteName}`;
    websiteLinkDiv.style.display = 'block';  // Display the website link
  }
  
  // Save password to localStorage and navigate to the new website
  function savePasswordAndNavigate() {
    const websiteNameInput = document.getElementById('websiteName').value.trim();
    const passwordInput = document.getElementById('password').value;
    
    // Save password to localStorage
    localStorage.setItem('websitePassword', passwordInput);
    
    // Redirect to the generated URL with custom website name
    const formattedWebsiteName = "bs." + websiteNameInput.toLowerCase().replace(/\s+/g, '');
    
    // Redirect to the new URL dynamically
    window.location.href = `website.html?name=${formattedWebsiteName}`;
  }
  