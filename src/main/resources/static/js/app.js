window.AppState = window.AppState || { currentUser: null };
const AppState = window.AppState;

function updateSidebarFromSettings(settings) {
    var nameEl = document.getElementById('sidebar-user-name');
    var typeEl = document.getElementById('sidebar-user-type');
    var avatarEl = document.getElementById('sidebar-avatar');

    if (!settings) {
        return; // leave defaults as-is when nothing is loaded
    }

    if (nameEl) {
        var first = settings.firstName || '';
        var last  = settings.lastName  || '';
        var fullName = (first + ' ' + last).trim();
        if (fullName.length === 0) {
            fullName = 'John Doe';
        }
        nameEl.textContent = fullName;
    }

    if (typeEl) {
        var typeText = 'Personal Account';
        if (settings.accountType === 'BUSINESS') {
            typeText = 'Business Account';
        } else if (settings.accountType === 'PERSONAL') {
            typeText = 'Personal Account';
        }
        typeEl.textContent = typeText;
    }

    if (avatarEl) {
        var initials = 'JD';
        var f = settings.firstName || '';
        var l = settings.lastName  || '';
        var chars = (f.charAt(0) + l.charAt(0)).trim();
        if (chars.length > 0) {
            initials = chars.toUpperCase();
        }
        avatarEl.textContent = initials;
    }
}

// Expose for other scripts (like settings.js)
window.updateSidebarFromSettings = updateSidebarFromSettings;

function loadUserSettingsForSidebar() {
    fetch('/api/settings', { credentials: 'same-origin' })
        .then(function (response) {
            if (!response.ok) {
                return null;
            }
            return response.json();
        })
        .then(function (data) {
            if (data) {
                AppState.currentUser = data;
                console.log('Loaded settings for sidebar', data);
                updateSidebarFromSettings(data);
            }
        })
        .catch(function (err) {
            console.error('Could not load user settings for sidebar', err);
        });
}

window.loadUserSettingsForSidebar = loadUserSettingsForSidebar;

// Navigation helper delegates to scroll-based navigation (scroll-nav.js)
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

// load settings to populate sidebar when script loads
loadUserSettingsForSidebar();

function renderLogin() {
    const email = prompt('Enter email for demo login:', 'test@example.com');
    const name = (email || 'user').split('@')[0];
    AppState.currentUser = { name, accountType: 'individual' };
    navigate('dashboard');
}


navigate('dashboard');
