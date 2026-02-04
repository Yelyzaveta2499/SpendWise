const pageTitle = document.getElementById("page-title");
const pageSubtitle = document.getElementById("page-subtitle");
const pageContent = document.getElementById("page-content");
const navLinks = document.querySelectorAll(".nav a");

navLinks.forEach(link => {
    link.addEventListener("click", e => {
        e.preventDefault();
        navLinks.forEach(l => l.classList.remove("active"));
        link.classList.add("active");
        loadPage(link.dataset.page);
    });
});

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

// Initial load
loadPage("dashboard");
