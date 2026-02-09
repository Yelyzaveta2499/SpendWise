    const registerBtn = document.getElementById('register-btn');
    if (registerBtn) registerBtn.addEventListener('click', () => {
        // simple mock register - replace with real registration later
        const name = document.getElementById('reg-name').value || 'NewUser';
        const email = document.getElementById('reg-email').value || 'new@example.com';
        const accountType = document.getElementById('account-type').value || 'individual';
        AppState.currentUser = { name, accountType, email };
        navigate('dashboard');
    });

const navLinks = document.querySelectorAll(".nav a");

navLinks.forEach(link => {
    link.addEventListener("click", e => {
        e.preventDefault();
        navLinks.forEach(l => l.classList.remove("active"));
        link.classList.add("active");
        loadPage(link.dataset.page);
    });
});

// App state (simple, easy to explain)
const AppState = {
    currentUser: null
};

// Simple navigation helper used by SPA links/buttons
function navigate(page) {
    // try to update nav active link if present
    navLinks.forEach(l => {
        if (l.dataset.page === page) {
            l.classList.add('active');
        } else {
            l.classList.remove('active');
        }
    });
    loadPage(page);
}

function loadPage(page) {
    switch (page) {
        case "dashboard":
            renderDashboard();
            break;
        case "expenses":
            renderPlaceholder("Expenses", "Track and manage your transactions.");
            break;
        case "budgets":
            renderPlaceholder("Budgets", "Manage your monthly spending limits.");
            break;
        case "goals":
            renderPlaceholder("Goals", "Track your savings and milestones.");
            break;
        case "reports":
            renderPlaceholder("Reports", "Analyze financial trends.");
            break;
        case "settings":
            renderPlaceholder("Settings", "Manage account preferences.");
            break;
        case "login":
            renderLogin();
            break;

        case "register":
            renderRegister();
            break;

    }
}

function renderDashboard() {
    pageTitle.textContent = "Dashboard";
    pageSubtitle.textContent = "Welcome back! Here's your financial overview.";

    pageContent.innerHTML = `
    <div class="cards">
      <div class="card">
        <h3>Total Balance</h3>
        <p>$0.00</p>
      </div>
      <div class="card">
        <h3>Monthly Income</h3>
        <p>$0.00</p>
      </div>
      <div class="card">
        <h3>Monthly Expenses</h3>
        <p>$0.00</p>
      </div>
      <div class="card">
        <h3>Savings Rate</h3>
        <p>0%</p>
      </div>
    </div>
  `;
}

function renderPlaceholder(title, subtitle) {
    pageTitle.textContent = title;
    pageSubtitle.textContent = subtitle;

    pageContent.innerHTML = `
    <div class="card">
      <p>UI placeholder – content will be loaded dynamically.</p>
    </div>
  `;
}

//login and register render
function renderLogin() {
    pageTitle.textContent = "Login";
    pageSubtitle.textContent = "Access your account";

    pageContent.innerHTML = `
    <div class="card auth-card">
      <h3>Login</h3>

      <label>Email</label>
      <input id="login-email" type="email" placeholder="Enter your email" />

      <label>Password</label>
      <input id="login-password" type="password" placeholder="Enter your password" />

      <button class="primary-btn" id="login-btn">Login</button>

      <p class="auth-link">
        Don’t have an account?
        <a href="#" id="go-register">Register here</a>
      </p>
    </div>
  `;

    // attach handlers after DOM insertion
    const goRegister = document.getElementById("go-register");
    if (goRegister) goRegister.addEventListener('click', e => { e.preventDefault(); navigate('register'); });

    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) loginBtn.addEventListener('click', () => {
        // simple mock login - replace with real auth later
        const email = document.getElementById('login-email').value || 'test@example.com';
        AppState.currentUser = {
            name: email.split('@')[0],
            accountType: 'individual'
        };
        navigate('dashboard');
    });
}

function renderRegister() {
    pageTitle.textContent = "Register";
    pageSubtitle.textContent = "Create a new account";

    pageContent.innerHTML = `
    <div class="card auth-card">
      <h3>Register</h3>

      <label>Full Name</label>
      <input id="reg-name" type="text" placeholder="Your name" />

      <label>Email</label>
      <input id="reg-email" type="email" placeholder="Your email" />

      <label>Password</label>
      <input id="reg-password" type="password" placeholder="Create a password" />

      <label>Account Type</label>
      <select id="account-type">
        <option value="">Select account type</option>
        <option value="individual">Individual</option>
        <option value="business">Business</option>
        <option value="kids">Kids</option>
      </select>

      <button class="primary-btn" id="register-btn">Register</button>

      <p class="auth-link">
        Already have an account?
        <a href="#" id="go-login">Login here</a>
      </p>
    </div>
  `;

    // attach handlers after DOM insertion
    const goLogin = document.getElementById("go-login");
    if (goLogin) goLogin.addEventListener('click', e => { e.preventDefault(); navigate('login'); });

    navigate("dashboard");
};


// Initial load
loadPage("dashboard");
