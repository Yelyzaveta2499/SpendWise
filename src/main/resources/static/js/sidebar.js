// Sidebar toggle functionality

(function() {
  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('sidebar-toggle');

  // Check for saved preference in localStorage
  const sidebarState = localStorage.getItem('sidebarCollapsed');

  if (sidebarState === 'true' && sidebar) {
    sidebar.classList.add('collapsed');
  }

  // Toggle sidebar on button click
  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      sidebar.classList.toggle('collapsed');

      // Save state to localStorage
      const isCollapsed = sidebar.classList.contains('collapsed');
      localStorage.setItem('sidebarCollapsed', isCollapsed);
    });
  }

  // --- Total wealth circle wiring ---
  function formatWealth(value) {
    if (value == null) return '--';
    let num = Number(value);
    if (!isFinite(num)) return '--';

    const abs = Math.abs(num);
    let suffix = '';
    if (abs >= 1_000_000_000) {
      num = num / 1_000_000_000;
      suffix = 'B';
    } else if (abs >= 1_000_000) {
      num = num / 1_000_000;
      suffix = 'M';
    } else if (abs >= 1_000) {
      num = num / 1_000;
      suffix = 'K';
    }

    const fixed = abs >= 100 ? num.toFixed(0) : num.toFixed(1);
    return fixed.replace(/\.0$/, '') + suffix;
  }

  async function loadTotalWealth() {
    const el = document.getElementById('circle-total-wealth');
    if (!el) return;

    el.textContent = '...';

    try {
      const resp = await fetch('/api/dashboard/total-wealth', {
        credentials: 'same-origin'
      });
      if (!resp.ok) {
        el.textContent = '--';
        return;
      }
      const data = await resp.json();
      const tw = data.totalWealth ?? data.totalwealth ?? data.balance;
      el.textContent = formatWealth(tw);
      if (tw != null) {
        el.title = new Intl.NumberFormat(undefined, {
          style: 'currency',
          currency: 'USD',
          maximumFractionDigits: 2
        }).format(Number(tw));
      }
    } catch (e) {
      el.textContent = '--';
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadTotalWealth);
  } else {
    loadTotalWealth();
  }
})();

