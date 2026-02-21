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
            <div class="dash-card-label">Monthly Income</div>
            <div class="dash-card-icon" style="background:#e7f7ef;color:#198754;">📈</div>
          </div>
          <div class="dash-card-value" id="dashIncome">—</div>
          <div class="dash-card-foot" id="dashIncomeHint">For selected period</div>
        </div>

        <div class="dash-card">
          <div class="dash-card-top">
            <div class="dash-card-label">Monthly Expenses</div>
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
          <div class="dash-chart" id="dashChart">
            <div class="dash-chart-empty">Chart coming soon</div>
          </div>
        </div>
        
        <div class="dash-panel">
          <div class="dash-panel-head">
            <div>
              <div class="dash-panel-title">Spending by Category</div>
            </div>
          </div>
          <div class="dash-chart" id="dashChart">
            <div class="dash-chart-empty">Chart coming soon</div>
          </div>
        </div>
        
        <div class="dash-panel">
          <div class="dash-panel-head">
            <div>
              <div class="dash-panel-title">Budget Status</div>
            </div>
          </div>
          <div class="dash-chart" id="dashChart">
            <div class="dash-chart-empty">Chart coming soon</div>
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
    if (isNaN(dt.getTime())) return null;
    return dt;
  }

  function periodRange(periodValue) {
    const now = new Date();
    const start = new Date(now);
    const end = new Date(now);

    // normalize end to end-of-day
    end.setHours(23, 59, 59, 999);

    if (periodValue === 'this_month') {
      start.setDate(1);
      start.setHours(0, 0, 0, 0);
      return { start, end };
    }

    if (periodValue === 'last_month') {
      const y = now.getFullYear();
      const m = now.getMonth(); // 0-based current month
      const lastMonth = new Date(y, m - 1, 1);
      const lastMonthEnd = new Date(y, m, 0);
      lastMonth.setHours(0, 0, 0, 0);
      lastMonthEnd.setHours(23, 59, 59, 999);
      return { start: lastMonth, end: lastMonthEnd };
    }

    if (periodValue === 'this_year') {
      const y = now.getFullYear();
      const yearStart = new Date(y, 0, 1);
      yearStart.setHours(0, 0, 0, 0);
      return { start: yearStart, end };
    }

    // last_30 default
    start.setDate(now.getDate() - 30);
    start.setHours(0, 0, 0, 0);
    return { start, end };
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

  const state = {
    all: [],
    filtered: []
  };

  function filterByPeriod(items, periodValue) {
    const range = periodRange(periodValue);
    return (Array.isArray(items) ? items : []).filter(function (e) {
      const d = safeDate(e.expenseDate || e.date);
      if (!d) return false;
      return d.getTime() >= range.start.getTime() && d.getTime() <= range.end.getTime();
    });
  }

  function computeTotals(items) {
    let income = 0;
    let expenses = 0;

    (Array.isArray(items) ? items : []).forEach(function (e) {
      const amt = Number(e.amount) || 0;
      if ((e.category || '') === 'Income') {
        income += Math.abs(amt);
      } else {
        expenses += Math.abs(amt);
      }
    });

    const balance = income - expenses;
    const savingsRate = income > 0 ? ((balance / income) * 100) : 0;

    return {
      income: income,
      expenses: expenses,
      balance: balance,
      savingsRate: savingsRate
    };
  }

  function renderTotals() {
    const totals = computeTotals(state.filtered);

    elBalance.textContent = money(totals.balance);
    elIncome.textContent = money(totals.income);
    elExpenses.textContent = money(totals.expenses);
    elSavings.textContent = totals.income > 0 ? (totals.savingsRate.toFixed(1) + '%') : '0%';
  }

  function renderTransactions() {
    // newest first
    const sorted = state.filtered.slice().sort(function (a, b) {
      const da = safeDate(a.expenseDate || a.date);
      const db = safeDate(b.expenseDate || b.date);
      return (db ? db.getTime() : 0) - (da ? da.getTime() : 0);
    });

    const top = sorted.slice(0, 7);

    if (top.length === 0) {
      txList.innerHTML = '';
      emptyEl.style.display = 'block';
      return;
    }

    emptyEl.style.display = 'none';

    txList.innerHTML = top.map(function (t) {
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

  function refresh() {
    const p = periodSelect.value;
    state.filtered = filterByPeriod(state.all, p);
    renderTotals();
    renderTransactions();
  }

  function load() {
    // loading placeholders
    elBalance.textContent = 'Loading…';
    elIncome.textContent = 'Loading…';
    elExpenses.textContent = 'Loading…';
    elSavings.textContent = 'Loading…';
    txList.innerHTML = '<div class="dash-loading">Loading transactions...</div>';
    emptyEl.style.display = 'none';

    fetch('/api/expenses')
      .then(function (resp) {
        if (!resp.ok) throw new Error('Failed to load expenses');
        return resp.json();
      })
      .then(function (data) {
        state.all = Array.isArray(data) ? data : [];
        refresh();
      })
      .catch(function () {
        // treat as empty state
        state.all = [];
        refresh();
      });
  }

  periodSelect.addEventListener('change', function () {
    refresh();
  });

  if (btnGoExpenses) {
    btnGoExpenses.addEventListener('click', function () {
      if (window.navigate) window.navigate('expenses');
      else if (window.loadPage) window.loadPage('expenses');
    });
  }

  if (viewAll) {
    viewAll.addEventListener('click', function (e) {
      e.preventDefault();
      if (window.navigate) window.navigate('expenses');
      else if (window.loadPage) window.loadPage('expenses');
    });
  }

  load();
}

// Auto-init on load
document.addEventListener('DOMContentLoaded', function () {
  try {
    renderDashboardOverview();
  } catch (e) {

  }
});

