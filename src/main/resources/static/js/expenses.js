// Simple Expenses tab renderer

function renderExpenses() {
  const pageTitle = document.querySelector('.header-left h2');
  const pageSubtitle = document.querySelector('.header-left p');
  const pageContent = document.querySelector('.page-content');

  if (!pageTitle || !pageSubtitle || !pageContent) return;

  pageTitle.textContent = 'Expenses';
  pageSubtitle.textContent = 'Track and manage your transactions';

  pageContent.innerHTML = `
    <div class="expenses-container">
      <div class="expenses-toolbar">
        <button class="btn-add-expense" id="add-expense-btn">
          <span style="font-size: 18px; margin-right: 8px;">+</span> Add Expense
        </button>
        <div class="search-filter-group">
          <input id="search-expense" type="text" class="search-input" placeholder="Search transactions..." />
          <select id="category-filter" class="category-select">
            <option value="">All Categories</option>
            <option value="Grocery">Grocery</option>
            <option value="Food & Dining">Food & Dining</option>
            <option value="Housing">Housing</option>
            <option value="Transportation">Transportation</option>
            <option value="Utilities">Utilities</option>
            <option value="Shopping">Shopping</option>
            <option value="Income">Income</option>
            <option value="Coffee">Coffee</option>
          </select>
        </div>
      </div>

      <div class="expense-list-card" id="expense-list"></div>
    </div>

    <!-- Modal for Add Expense -->
    <div id="expense-modal" class="expense-modal" style="display: none;">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Add New Expense</h3>
          <button class="modal-close" id="modal-close">&times;</button>
        </div>
        <form id="add-expense-form" class="expense-form">
          <input id="exp-name" type="text" placeholder="Name (e.g., Grocery Store)" required />
          <input id="exp-category" type="text" placeholder="Category (e.g., Food & Dining)" required />
          <input id="exp-amount" type="number" step="0.01" placeholder="Amount" required />
          <input id="exp-date" type="date" required />
          <button type="submit" class="btn-submit">Add Expense</button>
        </form>
      </div>
    </div>
  `;

  // Hardcoded expense categories
  const state = window.ExpensesState || { items: [
    { name: 'Grocery Store', category: 'Food & Dining', amount: 0, date: '2026-02-15', icon: '🍴' },
    { name: 'Monthly Salary', category: 'Income', amount: 0, date: '2026-02-15', icon: '💼' },
    { name: 'Coffee Shop', category: 'Coffee', amount: 0, date: '2026-02-15', icon: '☕' },
    { name: 'Rent Payment', category: 'Housing', amount: 0, date: '2026-02-15', icon: '🏠' },
    { name: 'Gas Station', category: 'Transportation', amount: 0, date: '2026-02-15', icon: '🚗' },
    { name: 'Phone Bill', category: 'Utilities', amount: 0, date: '2026-02-15', icon: '📱' },
    { name: 'Amazon Purchase', category: 'Shopping', amount: 0, date: '2026-02-15', icon: '🛍️' },
    { name: 'Restaurant', category: 'Food & Dining', amount: 0, date: '2026-02-15', icon: '🍴' },
    { name: 'Uber Ride', category: 'Transportation', amount: 0, date: '2026-02-15', icon: '🚗' },
    { name: 'Freelance Payment', category: 'Income', amount: 0, date: '2026-02-15', icon: '💼' },
  ] };
  window.ExpensesState = state;

  const listEl = document.getElementById('expense-list');
  const formEl = document.getElementById('add-expense-form');
  const searchEl = document.getElementById('search-expense');
  const categoryEl = document.getElementById('category-filter');
  const addBtn = document.getElementById('add-expense-btn');
  const modal = document.getElementById('expense-modal');
  const modalClose = document.getElementById('modal-close');

  // Get icon based on category
  function getCategoryIcon(category) {
    const icons = {
      'Food & Dining': '🍴',
      'Income': '💼',
      'Coffee': '☕',
      'Housing': '🏠',
      'Transportation': '🚗',
      'Utilities': '📱',
      'Shopping': '🛍️',
      'Grocery': '🍴'
    };
    return icons[category] || '💰';
  }

  // Get icon background color based on category
  function getIconBg(category) {
    const colors = {
      'Food & Dining': '#e0f2e9',
      'Income': '#d4f4dd',
      'Coffee': '#fff4e6',
      'Housing': '#e8e8f0',
      'Transportation': '#fff4e0',
      'Utilities': '#e3f2fd',
      'Shopping': '#fce4ec',
      'Grocery': '#e0f2e9'
    };
    return colors[category] || '#f0f0f0';
  }

  // Format date as "Feb 15"
  function formatDate(dateStr) {
    const date = new Date(dateStr);
    const month = date.toLocaleDateString('en-US', { month: 'short' });
    const day = date.getDate();
    return month + ' ' + day;
  }

  // Format a single transaction item
  function formatItem(item) {
    // Income categories show in green with +, others show in red
    const isIncome = item.category === 'Income';
    const amountStr = (isIncome ? '+' : '') + '$' + Math.abs(item.amount).toFixed(2);
    const amountClass = isIncome ? 'amount-income' : 'amount-expense';
    const icon = item.icon || getCategoryIcon(item.category);
    const iconBg = getIconBg(item.category);

    return `
      <div class="transaction-item">
        <div class="transaction-left">
          <div class="category-icon" style="background-color: ${iconBg};">
            ${icon}
          </div>
          <div class="transaction-details">
            <div class="transaction-name">${item.name}</div>
            <div class="transaction-category">${item.category}</div>
          </div>
        </div>
        <div class="transaction-right">
          <div class="${amountClass}">${amountStr}</div>
          <div class="transaction-date">${formatDate(item.date)}</div>
        </div>
      </div>
    `;
  }

  // Render the list with search and filter
  function renderList() {
    const q = (searchEl.value || '').toLowerCase();
    const cat = categoryEl.value || '';
    const items = state.items.filter(i =>
      (!q || i.name.toLowerCase().includes(q) || i.category.toLowerCase().includes(q)) &&
      (!cat || i.category === cat)
    );
    listEl.innerHTML = items.map(formatItem).join('');
  }

  renderList();

  // Modal handlers
  addBtn.addEventListener('click', function() {
    modal.style.display = 'flex';
  });

  modalClose.addEventListener('click', function() {
    modal.style.display = 'none';
  });

  window.addEventListener('click', function(e) {
    if (e.target === modal) {
      modal.style.display = 'none';
    }
  });

  // Form submit handler - add new expense
  formEl.addEventListener('submit', function (e) {
    e.preventDefault();
    const name = document.getElementById('exp-name').value.trim();
    const category = document.getElementById('exp-category').value.trim();
    const amount = Number.parseFloat(document.getElementById('exp-amount').value);
    const date = document.getElementById('exp-date').value || new Date().toISOString().slice(0, 10);

    if (!name || Number.isNaN(amount)) return;

    const newItem = {
      name,
      category: category || 'General',
      amount: Math.abs(amount), // Store as positive
      date,
      icon: getCategoryIcon(category)
    };

    state.items.unshift(newItem);
    renderList();
    e.target.reset();
    modal.style.display = 'none';
  });

  searchEl.addEventListener('input', renderList);
  categoryEl.addEventListener('change', renderList);
}

