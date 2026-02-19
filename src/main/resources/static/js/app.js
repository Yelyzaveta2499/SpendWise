const AppState = {
    currentUser: null
};

// Navigation helper now delegates to scroll-based navigation (scroll-nav.js)
function navigate(page) {
    if (window.pageToSection && window.scrollToSection) {
        const index = window.pageToSection[page];
        if (index !== undefined) {
            window.scrollToSection(index);
        }
    }
}

function loadPage(page) {
    navigate(page);
}

// Attach nav click handlers once DOM is ready
const navLinks = document.querySelectorAll('.nav a');
navLinks.forEach(link => {
    link.addEventListener('click', e => {
        e.preventDefault();
        const page = link.dataset.page;
        navigate(page);
    });
});


function renderLogin() {
    const email = prompt('Enter email for demo login:', 'test@example.com');
    const name = (email || 'user').split('@')[0];
    AppState.currentUser = { name, accountType: 'individual' };
    navigate('dashboard');
}


// Initial load: go to dashboard section
navigate('dashboard');
