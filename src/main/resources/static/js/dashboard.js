function renderDashboardOverview() {
  const section = document.getElementById('section-dashboard');
  if (!section) return;

  const pageContent = section.querySelector('.section-content');
  if (!pageContent) return;

  // Layout
  pageContent.innerHTML = `
    <div class="dash-wrap">
      <div class="dash-top">

        <div class="dash-controls">
          <select id="dashPeriod" class="dash-period-select">
            <option value="this_month">This month</option>
            <option value="last_month">Last month</option>
            <option value="last_6_months">Last 6 months</option>
            <option value="this_year">This year</option>
          </select>
        </div>
      </div>

      <div class="dash-cards">
        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Total Balance</div>
            <div class="dash-card-icon dash-card-icon-dark">TB</div>
          </div>
          <div class="dash-card-value" id="dashTotalBalance">—</div>
          <div class="dash-card-foot" id="dashBalanceHint">Net (income - expenses)</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Income</div>
            <div class="dash-card-icon dash-card-icon-dark dash-card-icon-pos">IN</div>
          </div>
          <div class="dash-card-value" id="dashIncome">—</div>
          <div class="dash-card-foot" id="dashIncomeHint">For selected period</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Expenses</div>
            <div class="dash-card-icon dash-card-icon-dark dash-card-icon-neg">EX</div>
          </div>
          <div class="dash-card-value" id="dashExpenses">—</div>
          <div class="dash-card-foot" id="dashExpensesHint">For selected period</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Savings Rate</div>
            <div class="dash-card-icon dash-card-icon-dark dash-card-icon-accent">SR</div>
          </div>
          <div class="dash-card-value" id="dashSavingsRate">—</div>
          <div class="dash-card-foot" id="dashSavingsHint">Income vs expenses</div>
        </div>
      </div>

      <div class="dash-grid">
        <div class="dash-panel">
          <div class="dash-panel-head">
            <div>
              <div class="dash-panel-title">Income vs Expenses</div>
              <div class="dash-panel-sub">Last 6 months overview</div>
            </div>
          </div>
          <div class="dash-chart" id="dashIncomeExpenseChart">
            <div class="dash-chart-empty">Loading chart...</div>
          </div>
        </div>

        <div class="dash-panel">
          <div class="dash-panel-head dash-panel-head-row">
            <div>
              <div class="dash-panel-title">Recent Transactions</div>
              <div class="dash-panel-sub">Your latest activity</div>
            </div>
            <a class="dash-view-all" href="#" id="dashViewAll">View All</a>
          </div>

          <div id="dashEmpty" class="dash-empty" style="display:none;">
            <div class="dash-empty-title">No financial data yet</div>
            <div class="dash-empty-sub">Add an expense or income to see your dashboard totals.</div>
            <button type="button" class="dash-empty-btn" id="dashGoExpenses">Go to Expenses</button>
          </div>

          <div class="dash-tx-list" id="dashTxList"></div>
        </div>
      </div>
    </div>
  `;

  // Trigger dashboard box animations on load/render
  const dashWrap = pageContent.querySelector('.dash-wrap');
  if (dashWrap) {
    dashWrap.classList.remove('dash-animate');
    // Wait a frame so the browser paints the initial state, then add the class to start animations
    requestAnimationFrame(function () {
      dashWrap.classList.add('dash-animate');
    });
  }

  const periodSelect = document.getElementById('dashPeriod');
  const txList = document.getElementById('dashTxList');
  const emptyEl = document.getElementById('dashEmpty');
  const chartHost = document.getElementById('dashIncomeExpenseChart');

  // inline status area for network/errors
  const dashTop = pageContent.querySelector('.dash-top');
  if (dashTop) {
    dashTop.insertAdjacentHTML('beforeend', '<div id="dashStatus" class="dash-status" style="display:none;"></div>');
  }
  const statusEl = document.getElementById('dashStatus');

  const elBalance = document.getElementById('dashTotalBalance');
  const elIncome = document.getElementById('dashIncome');
  const elExpenses = document.getElementById('dashExpenses');
  const elSavings = document.getElementById('dashSavingsRate');

  const btnGoExpenses = document.getElementById('dashGoExpenses');
  const viewAll = document.getElementById('dashViewAll');

  function money(n) {
    const v = Number(n) || 0;
    return '$' + v.toFixed(2);
  }

  function safeDate(d) {
    if (!d) return null;
    const dt = new Date(d);
    if (Number.isNaN(dt.getTime())) return null;
    return dt;
  }

  function iconForCategory(category) {
    const icons = {
      'Food & Dining': 'FD',
      'Income': 'IN',
      'Coffee': 'CF',
      'Housing': 'HO',
      'Transportation': 'TR',
      'Utilities': 'UT',
      'Shopping': 'SH',
      'Grocery': 'GR'
    };
    return icons[category] || '$$';
  }

  function iconBgForCategory(category) {
    const colors = {
      'Food & Dining': '#021009',
      'Income': '#021b09',
      'Coffee': '#022201',
      'Housing': '#012501',
      'Transportation': '#031e04',
      'Utilities': '#031e04',
      'Shopping': '#031e04',
      'Grocery': '#031e04'
    };
    return colors[category] || '#f0f0f0';
  }

  function formatShortDate(dateStr) {
    const dt = safeDate(dateStr);
    if (!dt) return '';
    const month = dt.toLocaleDateString('en-US', { month: 'short' });
    const day = dt.getDate();
    return month + ' ' + day;
  }

  function sameDay(dt) {
    const now = new Date();
    return dt.getFullYear() === now.getFullYear() && dt.getMonth() === now.getMonth() && dt.getDate() === now.getDate();
  }

  function dayLabel(dateStr) {
    const dt = safeDate(dateStr);
    if (!dt) return '';
    if (sameDay(dt)) return 'Today';

    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    if (dt.getFullYear() === yesterday.getFullYear() && dt.getMonth() === yesterday.getMonth() && dt.getDate() === yesterday.getDate()) {
      return 'Yesterday';
    }

    return formatShortDate(dateStr);
  }

  // ---- Chart ( SVG lines) ----
  function renderIncomeExpenseChart(points) {
    if (!chartHost) return;

    const data = Array.isArray(points) ? points : [];
    if (data.length === 0) {
      chartHost.innerHTML = '<div class="dash-chart-empty">No chart data</div>';
      return;
    }


    const rect = chartHost.getBoundingClientRect();
    const w = Math.max(320, rect.width || 760);

    const containerHeight = rect.height && rect.height > 0 ? rect.height : 720;
    const h = Math.max(580, containerHeight);


    const padT = 10;   // smaller top padding
    const padB = 26;   // just enough for x-axis labels + legend
    const padL = 50;   // room for y-axis labels with more steps
    const padR = 20;

    function x(i) {
      if (data.length === 1) return padL;
      const innerW = w - padL - padR;
      return padL + (innerW * (i / (data.length - 1)));
    }

    const incomes = data.map(p => Number(p.income) || 0);
    const expenses = data.map(p => Number(p.expenses) || 0);

    const rawMax = Math.max(1, ...incomes, ...expenses);

    // max with base step
    let chartMax = rawMax;
    if (rawMax >= 1000) {
      const step = 1500;
      chartMax = Math.ceil(rawMax / step) * step;
      if (chartMax < step * 8) chartMax = step * 8;
    }

    function y(v) {
      const innerH = h - padT - padB;
      const t = (Number(v) || 0) / chartMax;
      return (h - padB) - (innerH * t);
    }

    // Smooth path builder (cubic curves between points)
    function smoothPath(values) {
      const pts = values.map(function (v, i) {
        return { x: x(i), y: y(v) };
      });

      if (pts.length === 1) {
        return 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
      }

      let d = 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
      for (let i = 1; i < pts.length; i++) {
        const prev = pts[i - 1];
        const curr = pts[i];
        const midX = (prev.x + curr.x) / 2;
        d += ' C ' + midX.toFixed(1) + ' ' + prev.y.toFixed(1) + ', ' + midX.toFixed(1) + ' ' + curr.y.toFixed(1) + ', ' + curr.x.toFixed(1) + ' ' + curr.y.toFixed(1);
      }
      return d;
    }

    function areaPath(values) {
      const line = smoothPath(values);
      const baseY = h - padB;
      const firstX = x(0);
      const lastX = x(values.length - 1);
      return line + ' L ' + lastX.toFixed(1) + ' ' + baseY.toFixed(1) + ' L ' + firstX.toFixed(1) + ' ' + baseY.toFixed(1) + ' Z';
    }

    const incomeD = smoothPath(incomes);
    const expenseD = smoothPath(expenses);
    const incomeAreaD = areaPath(incomes);
    const expenseAreaD = areaPath(expenses);

    const labels = data.map(function (p, i) {
      const lbl = p.label || (p.month || '').slice(5);
      return `<text class="chart-label" x="${x(i).toFixed(1)}" y="${(h - 8)}" text-anchor="middle">${lbl}</text>`;
    }).join('');

    // Fixed higher tick count so we always have more steps.
    const ticks = 8; // 8 intervals => 9 labels (0k..max)
    const innerH = h - padT - padB;

    const yLines = [];
    const yLabels = [];
    for (let i = 0; i <= ticks; i++) {
      const t = i / ticks;
      const value = chartMax * (1 - t);
      const yy = padT + innerH * t;

      yLines.push(`<line class="chart-grid" x1="${padL}" y1="${yy.toFixed(1)}" x2="${(w - padR)}" y2="${yy.toFixed(1)}" />`);

      const k = value / 1000;
      const label = '$' + (k === 0 ? '0' : (k % 1 === 0 ? k.toFixed(0) : k.toFixed(1))) + 'k';
      yLabels.push(`<text class="chart-ylabel" x="${(padL - 14)}" y="${(yy + 4).toFixed(1)}" text-anchor="end">${label}</text>`);
    }

    // vertical dotted grid for each month
    const xLines = data.map(function (p, i) {
      const xx = x(i);
      return `<line class="chart-grid" x1="${xx.toFixed(1)}" y1="${padT}" x2="${xx.toFixed(1)}" y2="${(h - padB)}" />`;
    }).join('');

    chartHost.innerHTML = `
      <svg viewBox="0 0 ${w} ${h}" width="100%" height="100%" aria-label="Income vs Expenses chart">
        <defs>
          <linearGradient id="incomeFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(16,185,129,0.22)" />
            <stop offset="100%" stop-color="rgba(16,185,129,0)" />
          </linearGradient>
          <linearGradient id="expensesFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(15,23,42,0.18)" />
            <stop offset="100%" stop-color="rgba(15,23,42,0)" />
          </linearGradient>
        </defs>

        <!-- grid: horizontal + vertical (dotted) -->
        <g>
          ${yLines.join('')}
          ${xLines}
        </g>

        <!-- area fills -->
        <path class="chart-fill-income paint-fill" d="${incomeAreaD}" fill="url(#incomeFill)" />
        <path class="chart-fill-expenses paint-fill" d="${expenseAreaD}" fill="url(#expensesFill)" />

        <!-- lines (paint animation) -->
        <path id="incomeLine" class="chart-line-income paint-line" d="${incomeD}" />
        <path id="expenseLine" class="chart-line-expenses paint-line" d="${expenseD}" />

        <!-- labels -->
        ${labels}
        ${yLabels.join('')}

        <!-- legend -->
        <g font-size="12" fill="#64748b">
          <circle cx="${padL + 120}" cy="${h - 6}" r="6" fill="#10b981"></circle>
          <text x="${padL + 132}" y="${h - 2}">Income</text>
          <circle cx="${padL + 210}" cy="${h - 6}" r="6" fill="rgba(15,23,42,0.90)"></circle>
          <text x="${padL + 222}" y="${h - 2}">Expenses</text>
        </g>
      </svg>
    `;

    // Apply dash lengths so the paint animation draws the full path
    const incomePathEl = chartHost.querySelector('#incomeLine');
    const expensePathEl = chartHost.querySelector('#expenseLine');

    if (incomePathEl && incomePathEl.getTotalLength) {
      const len = Math.ceil(incomePathEl.getTotalLength());
      incomePathEl.style.setProperty('--dash', String(len));
    }

    if (expensePathEl && expensePathEl.getTotalLength) {
      const len = Math.ceil(expensePathEl.getTotalLength());
      expensePathEl.style.setProperty('--dash', String(len));
      expensePathEl.style.animationDelay = '80ms';
    }
  }

  function renderTotalsFromOverview(overview) {
    const income = Number(overview && overview.income) || 0;
    const expenses = Number(overview && overview.expenses) || 0;
    const balance = Number(overview && overview.balance) || (income - expenses);
    const rate = Number(overview && overview.savingsRate) || 0;

    elBalance.textContent = money(balance);
    elIncome.textContent = money(income);
    elExpenses.textContent = money(expenses);
    elSavings.textContent = income > 0 ? (rate.toFixed(1) + '%') : '0%';
  }

  function renderTransactionsFromOverview(overview) {
    const items = (overview && Array.isArray(overview.recentTransactions)) ? overview.recentTransactions : [];

    if (!items.length) {
      txList.innerHTML = '';
      return;
    }

    txList.innerHTML = items.slice(0, 7).map(function (t) {
      const isIncome = (t.category || '') === 'Income';
      const v = Number(t.amount) || 0;
      const amountStr = (isIncome ? '+' : '-') + money(Math.abs(v));
      const amountClass = isIncome ? 'amount-income' : 'amount-expense';

      const icon = iconForCategory(t.category);
      const iconBg = iconBgForCategory(t.category);

      return `
        <div class="dash-tx-item">
          <div class="dash-tx-left">
            <div class="category-icon" style="background-color: ${iconBg};">
              ${icon}
            </div>
            <div>
              <div class="dash-tx-name">${t.name || 'Transaction'}</div>
              <div class="dash-tx-cat">${t.category || 'Other'}</div>
            </div>
          </div>
          <div class="dash-tx-right">
            <div class="${amountClass}">${amountStr}</div>
            <div class="dash-tx-date">${dayLabel(t.expenseDate || t.date)}</div>
          </div>
        </div>
      `;
    }).join('');
  }

  function setStatus(msg, type) {
    if (!statusEl) return;
    if (!msg) {
      statusEl.style.display = 'none';
      statusEl.textContent = '';
      statusEl.className = 'dash-status';
      return;
    }
    statusEl.style.display = 'block';
    statusEl.textContent = msg;
    statusEl.className = 'dash-status ' + (type || '');
  }

  function setLoading(isLoading) {
    if (periodSelect) periodSelect.disabled = !!isLoading;
  }

  function setLoadingUI() {
    setStatus('', '');
    setLoading(true);

    // show placeholders
    elBalance.textContent = 'Loading…';
    elIncome.textContent = 'Loading…';
    elExpenses.textContent = 'Loading…';
    elSavings.textContent = 'Loading…';

    txList.innerHTML = '<div class="dash-loading">Loading transactions...</div>';
    if (chartHost) chartHost.innerHTML = '<div class="dash-chart-empty">Loading chart...</div>';

    emptyEl.style.display = 'none';
  }

  // replace old setLoading with the improved one
  function setLoadingLegacy() {
    setLoadingUI();
  }

  function showEmptyState() {
    // totals show zeros, not "Loading"
    renderTotalsFromOverview({ income: 0, expenses: 0, balance: 0, savingsRate: 0 });

    // chart empty
    if (chartHost) chartHost.innerHTML = '<div class="dash-chart-empty">No data for this period</div>';

    // transactions empty: we keep empty list, and show the main empty card
    txList.innerHTML = '';
    emptyEl.style.display = 'block';
  }

  function loadOverview() {
    setLoadingUI();

    const p = periodSelect.value;
    fetch('/api/dashboard/overview?period=' + encodeURIComponent(p))
      .then(function (resp) {
        if (!resp.ok) {
          return resp.json().then(function (err) {
            throw new Error((err && err.error) || 'Failed to load dashboard');
          }).catch(function () {
            throw new Error('Failed to load dashboard');
          });
        }
        return resp.json();
      })
      .then(function (overview) {
        renderTotalsFromOverview(overview);
        renderIncomeExpenseChart(overview && overview.chart);
        renderTransactionsFromOverview(overview);

        const hasData = !!(overview && overview.hasData);
        if (!hasData) {
          // If no data for this period, show empty state
          showEmptyState();
        } else {
          emptyEl.style.display = 'none';
        }

        setLoading(false);
      })
      .catch(function (err) {
        // show error but still keep UX friendly
        setStatus((err && err.message) ? err.message : 'Failed to load dashboard', 'error');
        showEmptyState();
        setLoading(false);
      });
  }

  periodSelect.addEventListener('change', function () {
    loadOverview();
  });

  if (btnGoExpenses) {
    btnGoExpenses.addEventListener('click', function () {
      if (globalThis.navigate) globalThis.navigate('expenses');
      else if (globalThis.loadPage) globalThis.loadPage('expenses');
    });
  }

  if (viewAll) {
    viewAll.addEventListener('click', function (e) {
      e.preventDefault();
      if (globalThis.navigate) globalThis.navigate('expenses');
      else if (globalThis.loadPage) globalThis.loadPage('expenses');
    });
  }

  // swap to new function name to avoid confusion
  // (keep old call sites intact)
  window.__dashSetLoading = setLoadingLegacy;

  loadOverview();
}

// Auto-init on load
document.addEventListener('DOMContentLoaded', function () {
  renderDashboardOverview();
});
