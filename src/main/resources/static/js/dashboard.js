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
          <label class="dash-period-label" for="dashPeriod">Time period</label>
          <select id="dashPeriod" class="dash-period-select">
            <option value="this_month">This month</option>
            <option value="last_month">Last month</option>
            <option value="last_30">Last 30 days</option>
            <option value="this_year">This year</option>
          </select>
        </div>
      </div>

      <div class="dash-cards">
        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Total Balance</div>
            <div class="dash-card-icon" style="background:#eef2ff;color:#1e3a8a;">💳</div>
          </div>
          <div class="dash-card-value" id="dashTotalBalance">—</div>
          <div class="dash-card-foot" id="dashBalanceHint">Net (income - expenses)</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Income</div>
            <div class="dash-card-icon" style="background:#e7f7ef;color:#198754;">📈</div>
          </div>
          <div class="dash-card-value" id="dashIncome">—</div>
          <div class="dash-card-foot" id="dashIncomeHint">For selected period</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Expenses</div>
            <div class="dash-card-icon" style="background:#fde8e8;color:#dc3545;">📉</div>
          </div>
          <div class="dash-card-value" id="dashExpenses">—</div>
          <div class="dash-card-foot" id="dashExpensesHint">For selected period</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Savings Rate</div>
            <div class="dash-card-icon" style="background:#f3e8ff;color:#6f42c1;">🐷</div>
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

  const periodSelect = document.getElementById('dashPeriod');
  const txList = document.getElementById('dashTxList');
  const emptyEl = document.getElementById('dashEmpty');
  const chartHost = document.getElementById('dashIncomeExpenseChart');

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
      'Food & Dining': '🛒',
      'Income': '💰',
      'Coffee': '☕',
      'Housing': '🏠',
      'Transportation': '🚗',
      'Utilities': '📱',
      'Shopping': '🛍️',
      'Grocery': '🛒'
    };
    return icons[category] || '💸';
  }

  function iconBgForCategory(category) {
    const colors = {
      'Food & Dining': '#e0f2e9',
      'Income': '#d4f4dd',
      'Coffee': '#fff4e6',
      'Housing': '#e8e8f0',
      'Transportation': '#ede9fe',
      'Utilities': '#e3f2fd',
      'Shopping': '#fce4ec',
      'Grocery': '#e0f2e9'
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

    const w = 760;
    const h = 260;
    const pad = 28;

    const incomes = data.map(p => Number(p.income) || 0);
    const expenses = data.map(p => Number(p.expenses) || 0);

    const maxVal = Math.max(1, ...incomes, ...expenses);

    function x(i) {
      if (data.length === 1) return pad;
      const innerW = w - pad * 2;
      return pad + (innerW * (i / (data.length - 1)));
    }

    function y(v) {
      const innerH = h - pad * 2;
      const t = (Number(v) || 0) / maxVal;
      return (h - pad) - (innerH * t);
    }

    function polyline(values) {
      return values.map(function (v, i) {
        return x(i).toFixed(1) + ',' + y(v).toFixed(1);
      }).join(' ');
    }

    const incomeLine = polyline(incomes);
    const expenseLine = polyline(expenses);

    const labels = data.map(function(p, i) {
      const lbl = p.label || (p.month || '').slice(5);
      return `<text x="${x(i)}" y="${h - 8}" text-anchor="middle" font-size="11" fill="#64748b">${lbl}</text>`;
    }).join('');

    chartHost.innerHTML = `
      <svg viewBox="0 0 ${w} ${h}" width="100%" height="100%" aria-label="Income vs Expenses chart">
        <defs>
          <linearGradient id="incomeFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(25,135,84,0.25)" />
            <stop offset="100%" stop-color="rgba(25,135,84,0)" />
          </linearGradient>
        </defs>

        <!-- grid -->
        <g stroke="rgba(15,23,42,0.10)" stroke-width="1">
          <line x1="${pad}" y1="${pad}" x2="${pad}" y2="${h - pad}" />
          <line x1="${pad}" y1="${h - pad}" x2="${w - pad}" y2="${h - pad}" />
          <line x1="${pad}" y1="${pad}" x2="${w - pad}" y2="${pad}" />
        </g>

        <!-- income area fill -->
        <polygon points="${incomeLine} ${w - pad},${h - pad} ${pad},${h - pad}" fill="url(#incomeFill)" />

        <!-- lines -->
        <polyline points="${incomeLine}" fill="none" stroke="#198754" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
        <polyline points="${expenseLine}" fill="none" stroke="#0f172a" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" opacity="0.85" />

        <!-- month labels -->
        ${labels}

        <!-- legend -->
        <g font-size="12" fill="#64748b">
          <circle cx="${pad + 10}" cy="${pad + 12}" r="6" fill="#198754"></circle>
          <text x="${pad + 22}" y="${pad + 16}">Income</text>
          <circle cx="${pad + 90}" cy="${pad + 12}" r="6" fill="#0f172a" opacity="0.85"></circle>
          <text x="${pad + 102}" y="${pad + 16}">Expenses</text>
        </g>
      </svg>
    `;
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
            <div class="dash-tx-icon" style="background:${iconBg};">${icon}</div>
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

  function setLoading() {
    elBalance.textContent = 'Loading…';
    elIncome.textContent = 'Loading…';
    elExpenses.textContent = 'Loading…';
    elSavings.textContent = 'Loading…';
    txList.innerHTML = '<div class="dash-loading">Loading transactions...</div>';
    if (chartHost) chartHost.innerHTML = '<div class="dash-chart-empty">Loading chart...</div>';
    emptyEl.style.display = 'none';
  }

  function loadOverview() {
    setLoading();

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
        emptyEl.style.display = hasData ? 'none' : 'block';
      })
      .catch(function () {
        // fallback empty
        renderTotalsFromOverview({ income: 0, expenses: 0, balance: 0, savingsRate: 0 });
        renderIncomeExpenseChart([]);
        txList.innerHTML = '';
        emptyEl.style.display = 'block';
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

  loadOverview();
}

// Auto-init on load
document.addEventListener('DOMContentLoaded', function () {
  renderDashboardOverview();
});
