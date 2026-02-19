// Sidebar toggle functionality

(function() {
  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('sidebar-toggle');

  // Check for saved preference in localStorage
  const sidebarState = localStorage.getItem('sidebarCollapsed');

  if (sidebarState === 'true') {
    sidebar.classList.add('collapsed');
  }

  // Toggle sidebar on button click
  if (toggleBtn) {
    toggleBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      sidebar.classList.toggle('collapsed');

      // Save state to localStorage
      const isCollapsed = sidebar.classList.contains('collapsed');
      localStorage.setItem('sidebarCollapsed', isCollapsed);
    });
  }
})();

