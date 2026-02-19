// Scroll-based section navigation

(function() {
  const sectionsContainer = document.getElementById('sections-container');
  const navLinks = document.querySelectorAll('.sidebar-nav a');
  const sections = document.querySelectorAll('.page-section');

  let isScrolling = false;
  let currentSectionIndex = 0;

  // Map page names to section indices
  const pageToSection = {
    'dashboard': 0,
    'expenses': 1,
    'budgets': 2,
    'goals': 3,
    'reports': 4,
    'settings': 5
  };

  // Scroll to specific section smoothly
  function scrollToSection(index) {
    if (index >= 0 && index < sections.length) {
      const section = sections[index];
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
      currentSectionIndex = index;
      updateActiveSidebar(index);
      loadSectionContent(index);
    }
  }

  // Update active state in sidebar
  function updateActiveSidebar(index) {
    navLinks.forEach((link, i) => {
      if (i === index) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }

  // Load content for the current section
  function loadSectionContent(index) {
    const section = sections[index];
    const page = section.dataset.page;
    const contentDiv = section.querySelector('.section-content');

    // Mark section as visible for animations
    section.classList.add('visible');

    // Call appropriate render function based on page
    switch(page) {
      case 'dashboard':
        if (typeof renderDashboard === 'function') {
          contentDiv.innerHTML = '';
          const tempDiv = document.createElement('div');
          document.body.appendChild(tempDiv);
          const oldPageContent = document.querySelector('.page-content');
          const fakePageContent = tempDiv;
          fakePageContent.className = 'page-content';

          // Temporarily swap
          const originalQuery = document.querySelector.bind(document);
          document.querySelector = function(selector) {
            if (selector === '.page-content') return contentDiv;
            return originalQuery(selector);
          };

          renderDashboard();
          document.querySelector = originalQuery;
          tempDiv.remove();
        }
        break;
      case 'expenses':
        if (typeof renderExpenses === 'function') {
          renderExpenses();
        }
        break;
      case 'budgets':
        if (typeof renderBudgets === 'function') {
          renderBudgets();
        }
        break;
      case 'goals':
        contentDiv.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column;">
            <h2 style="font-size: 32px; margin-bottom: 16px;">Goals</h2>
            <p style="color: #6b7280;">Track your savings and milestones.</p>
          </div>
        `;
        break;
      case 'reports':
        contentDiv.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column;">
            <h2 style="font-size: 32px; margin-bottom: 16px;">Reports</h2>
            <p style="color: #6b7280;">Analyze financial trends.</p>
          </div>
        `;
        break;
      case 'settings':
        contentDiv.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column;">
            <h2 style="font-size: 32px; margin-bottom: 16px;">Settings</h2>
            <p style="color: #6b7280;">Manage account preferences.</p>
          </div>
        `;
        break;
    }
  }

  // Sidebar click navigation
  navLinks.forEach((link, index) => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      scrollToSection(index);
    });
  });

  // Intersection Observer to detect which section is in view
  const observerOptions = {
    root: sectionsContainer,
    threshold: 0.5
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && !isScrolling) {
        const index = Array.from(sections).indexOf(entry.target);
        currentSectionIndex = index;
        updateActiveSidebar(index);
        entry.target.classList.add('visible');
      }
    });
  }, observerOptions);

  sections.forEach(section => observer.observe(section));

  // Mousewheel navigation enhancement
  let wheelTimeout;
  sectionsContainer.addEventListener('wheel', (e) => {
    clearTimeout(wheelTimeout);
    wheelTimeout = setTimeout(() => {
      isScrolling = false;
    }, 150);
    isScrolling = true;
  });

  // Load initial section
  scrollToSection(0);

  // scrollToSection available globally for other scripts
  window.scrollToSection = scrollToSection;
  window.pageToSection = pageToSection;
})();

