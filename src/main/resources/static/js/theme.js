// Dark mode toggle

(function() {
  const toggle = document.getElementById('dark-mode-toggle');

  // Checkong for saved preference in localStorage
  const currentTheme = localStorage.getItem('theme');

  if (currentTheme === 'dark') {
    document.body.classList.add('dark-mode');
    if (toggle) toggle.checked = true;
  }

  // Toggle dark mode on checkbox change
  if (toggle) {
    toggle.addEventListener('change', function() {
      if (this.checked) {
        document.body.classList.add('dark-mode');
        localStorage.setItem('theme', 'dark');
      } else {
        document.body.classList.remove('dark-mode');
        localStorage.setItem('theme', 'light');
      }
    });
  }
})();

