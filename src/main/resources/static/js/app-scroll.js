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

    if (window.renderDashboardOverview) {
        window.renderDashboardOverview();
        return;
    }

    // Fallback
    const dashboardSection = document.getElementById('section-dashboard');
    if (!dashboardSection) return;

    const pageContent = dashboardSection.querySelector('.section-content');
    if (!pageContent) return;

    pageContent.innerHTML = `
    <div style="padding: 20px;">
      <h2 style="font-size: 28px; margin-bottom: 8px;">Dashboard</h2>
      <p style="color:#6b7280; margin-bottom: 0;">Loading dashboard...</p>
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
