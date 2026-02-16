function renderExpenses() {

  const expensesSection = document.getElementById('section-expenses');
  if (!expensesSection) return;

  const pageContent = expensesSection.querySelector('.section-content');
  if (!pageContent) return;


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
          <!-- Name dropdown limited to known merchants/sources -->
          <select id="exp-name" required>
            <option value="" disabled selected>Select name</option>
            <option value="Grocery Store">Grocery Store</option>
            <option value="Monthly Salary">Monthly Salary</option>
            <option value="Coffee Shop">Coffee Shop</option>
            <option value="Rent Payment">Rent Payment</option>
            <option value="Gas Station">Gas Station</option>
            <option value="Phone Bill">Phone Bill</option>
            <option value="Amazon Purchase">Amazon Purchase</option>
          </select>

          <!-- Category dropdown limited to existing categories -->
          <select id="exp-category" required>
            <option value="" disabled selected>Select category</option>
            <option value="Food & Dining">Food & Dining</option>
            <option value="Income">Income</option>
            <option value="Coffee">Coffee</option>
            <option value="Housing">Housing</option>
            <option value="Transportation">Transportation</option>
            <option value="Utilities">Utilities</option>
            <option value="Shopping">Shopping</option>
          </select>

          <input id="exp-amount" type="number" step="0.01" placeholder="Amount" required />
          <input id="exp-date" type="date" required />
          <button type="submit" class="btn-submit">Add Expense</button>
        </form>
      </div>
    </div>
  `;

  // state object backed by API -> changed from hardcoded expense categories
  const state = {
    items: []
  };

  const listEl = document.getElementById('expense-list');
  const formEl = document.getElementById('add-expense-form');
  const searchEl = document.getElementById('search-expense');
  const categoryEl = document.getElementById('category-filter');
  const addBtn = document.getElementById('add-expense-btn');
  const modal = document.getElementById('expense-modal');
  const modalClose = document.getElementById('modal-close');


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
    if (!dateStr) return '';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return '';
    const month = date.toLocaleDateString('en-US', { month: 'short' });
    const day = date.getDate();
    return month + ' ' + day;
  }

  // Format a single transaction item
  function formatItem(item) {
    const isIncome = item.category === 'Income';
    const amountValue = Number(item.amount) || 0;
    const amountStr = (isIncome ? '+' : '') + '$' + Math.abs(amountValue).toFixed(2);
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
          <div class="transaction-date">${formatDate(item.expenseDate || item.date)}</div>
        </div>
      </div>
    `;
  }

  // Render the list with search and filter
  function renderList() {
    const q = (searchEl.value || '').toLowerCase();
    const cat = categoryEl.value || '';

    const items = state.items.filter(function (i) {
      const nameMatch = !q || (i.name && i.name.toLowerCase().includes(q));
      const categoryText = i.category ? i.category.toLowerCase() : '';
      const categoryMatchSearch = !q || categoryText.includes(q);
      const categoryMatchFilter = !cat || i.category === cat;
      return (nameMatch || categoryMatchSearch) && categoryMatchFilter;
    });

    listEl.innerHTML = items.map(formatItem).join('');
  }

  // Load expenses from BE
  function loadExpensesFromApi() {
    listEl.innerHTML = '<div style="padding: 16px;">Loading expenses...</div>';

    fetch('/api/expenses')
      .then(function (response) {
        if (!response.ok) {
          throw new Error('Failed to load expenses');
        }
        return response.json();
      })
      .then(function (data) {
        // data is an array of ExpenseEntity objects
        state.items = Array.isArray(data) ? data : [];
        renderList();
      })
      .catch(function () {
        listEl.innerHTML = '<div style="padding: 16px; color: #dc3545;">Could not load expenses. Please try again later.</div>';
      });
  }

  loadExpensesFromApi();

  // Modal handlers
  addBtn.addEventListener('click', function () {
    modal.style.display = 'flex';
  });

  modalClose.addEventListener('click', function () {
    modal.style.display = 'none';
  });

  window.addEventListener('click', function (e) {
    if (e.target === modal) {
      modal.style.display = 'none';
    }
  });

  // Form submit handler - add new expense via API
  formEl.addEventListener('submit', function (e) {
    e.preventDefault();
    const name = document.getElementById('exp-name').value;
    const category = document.getElementById('exp-category').value;
    const amount = Number.parseFloat(document.getElementById('exp-amount').value);
    const date = document.getElementById('exp-date').value || new Date().toISOString().slice(0, 10);

    if (!name || !category || Number.isNaN(amount)) return;

    const payload = {
      name: name,
      category: category,
      amount: Math.abs(amount),
      date: date
    };

    fetch('/api/expenses', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
      .then(function (response) {
        if (!response.ok) {
          // Try to read backend error message
          return response.json().then(function (err) {
            var msg = err && err.error ? err.error : 'Failed to create expense';
            throw new Error(msg);
          }).catch(function () {
            throw new Error('Failed to create expense');
          });
        }
        return response.json();
      })
      .then(function (created) {
        // Prepend new item and re-render
        state.items.unshift(created);
        renderList();
        e.target.reset();
        modal.style.display = 'none';
      })
      .catch(function (err) {
        alert(err.message || 'Could not save expense. Please try again.');
      });
  });

  searchEl.addEventListener('input', renderList);
  categoryEl.addEventListener('change', renderList);
}
