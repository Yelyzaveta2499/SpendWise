const AppState = {
    currentUser: null
};

//delegates to scroll-nav.js
function navigate(page) {
    if (window.pageToSection && window.scrollToSection) {
        const sectionIndex = window.pageToSection[page];
        if (sectionIndex !== undefined) {
            window.scrollToSection(sectionIndex);
        }
    }
}

// Legacy loadPage function for compatibility
function loadPage(page) {
    navigate(page);
}

// Dashboard renderer
function renderDashboard() {
    const dashboardSection = document.getElementById('section-dashboard');
    if (!dashboardSection) return;

    const pageContent = dashboardSection.querySelector('.section-content');
    if (!pageContent) return;

    pageContent.innerHTML = `
    <div style="padding: 20px;">
      
      <div class="cards">
        <div class="card" style="background: white; padding: 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <h3 style="color: #6b7280; font-size: 14px; margin-bottom: 8px;">Total Balance</h3>
          <p style="font-size: 28px; font-weight: 700; color: #064e3b;">$0.00</p>
        </div>
        <div class="card" style="background: white; padding: 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <h3 style="color: #6b7280; font-size: 14px; margin-bottom: 8px;">Monthly Income</h3>
          <p style="font-size: 28px; font-weight: 700; color: #064e3b;">$0.00</p>
        </div>
        <div class="card" style="background: white; padding: 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <h3 style="color: #6b7280; font-size: 14px; margin-bottom: 8px;">Monthly Expenses</h3>
          <p style="font-size: 28px; font-weight: 700; color: #064e3b;">$0.00</p>
        </div>
        <div class="card" style="background: white; padding: 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <h3 style="color: #6b7280; font-size: 14px; margin-bottom: 8px;">Savings Rate</h3>
          <p style="font-size: 28px; font-weight: 700; color: #064e3b;">0%</p>
        </div>
      </div>
    </div>
  `;
}

// Placeholder renderer for incomplete sections
function renderPlaceholder(title, subtitle) {
    return `
    <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column;">
      <h2 style="font-size: 32px; margin-bottom: 16px;">${title}</h2>
      <p style="color: #6b7280;">${subtitle}</p>
    </div>
  `;
}

