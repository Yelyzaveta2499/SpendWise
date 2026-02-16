// Budgets tab renderer

function renderBudgets() {
  const pageTitle = document.querySelector('.header-left h2');
  const pageSubtitle = document.querySelector('.header-left p');
  const pageContent = document.querySelector('.page-content');

  if (!pageTitle || !pageSubtitle || !pageContent) return;

  pageTitle.textContent = 'Budgets';
  pageSubtitle.textContent = 'Manage your monthly spending limits';

  // Sample budget data
  const budgetData = {
    totalBudget: 3200,
    totalSpent: 2700,
    remaining: 500,
    categories: [
      {
        name: 'Housing',
        icon: '🏠',
        monthlyLimit: 1200,
        spent: 1200,
        color: '#f59e0b'
      },
      {
        name: 'Food & Dining',
        icon: '🍽️',
        monthlyLimit: 600,
        spent: 450,
        color: '#1e3a8a'
      },
      {
        name: 'Transportation',
        icon: '🚗',
        monthlyLimit: 400,
        spent: 280,
        color: '#1e3a8a'
      },
      {
        name: 'Entertainment',
        icon: '🎬',
        monthlyLimit: 200,
        spent: 180,
        color: '#f59e0b'
      },
      {
        name: 'Shopping',
        icon: '🛍️',
        monthlyLimit: 300,
        spent: 320,
        color: '#dc2626'
      },
      {
        name: 'Utilities',
        icon: '💡',
        monthlyLimit: 250,
        spent: 145,
        color: '#1e3a8a'
      },
      {
        name: 'Healthcare',
        icon: '🏥',
        monthlyLimit: 150,
        spent: 50,
        color: '#1e3a8a'
      },
      {
        name: 'Personal Care',
        icon: '✨',
        monthlyLimit: 100,
        spent: 75,
        color: '#f59e0b'
      }
    ]
  };

  const percentSpent = ((budgetData.totalSpent / budgetData.totalBudget) * 100).toFixed(1);

  pageContent.innerHTML = `
    <div class="budgets-container">
      <!-- Top Summary Cards -->
      <div class="budget-summary-cards">
        <div class="budget-summary-card">
          <div class="summary-label">Total Budget</div>
          <div class="summary-amount">$${budgetData.totalBudget.toLocaleString()}</div>
          <div class="summary-sublabel">Monthly allocation</div>
        </div>
        
        <div class="budget-summary-card spent">
          <div class="summary-label">Total Spent</div>
          <div class="summary-amount spent-amount">$${budgetData.totalSpent.toLocaleString()}</div>
          <div class="summary-sublabel">${percentSpent}% of budget</div>
        </div>
        
        <div class="budget-summary-card remaining">
          <div class="summary-label">Remaining</div>
          <div class="summary-amount remaining-amount">$${budgetData.remaining.toLocaleString()}</div>
          <div class="summary-sublabel">Available to spend</div>
        </div>
      </div>

      <!-- Add Budget Button -->
      <div class="budget-actions">
        <button class="btn-add-budget" id="add-budget-btn">
          <span style="font-size: 18px; margin-right: 8px;">+</span> Add Budget
        </button>
      </div>

      <!-- Budget Category Cards Grid -->
      <div class="budget-cards-grid">
        ${budgetData.categories.map(category => renderBudgetCard(category)).join('')}
      </div>
    </div>

    <!-- Add Budget Modal -->
    <div id="budget-modal" class="budget-modal" style="display: none;">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Add New Budget</h3>
          <button class="modal-close" id="budget-modal-close">&times;</button>
        </div>
        <form id="add-budget-form" class="budget-form">
          <select id="budget-category" required>
            <option value="" disabled selected>Select category</option>
            <option value="Housing">Housing</option>
            <option value="Food & Dining">Food & Dining</option>
            <option value="Transportation">Transportation</option>
            <option value="Entertainment">Entertainment</option>
            <option value="Shopping">Shopping</option>
            <option value="Utilities">Utilities</option>
            <option value="Healthcare">Healthcare</option>
            <option value="Personal Care">Personal Care</option>
          </select>

          <input id="budget-limit" type="number" step="1" placeholder="Monthly limit (e.g., 500)" required />

          <button type="submit" class="btn-submit">Add Budget</button>
        </form>
      </div>
    </div>
  `;

  // Modal handlers
  const addBudgetBtn = document.getElementById('add-budget-btn');
  const modal = document.getElementById('budget-modal');
  const modalClose = document.getElementById('budget-modal-close');
  const budgetForm = document.getElementById('add-budget-form');

  if (addBudgetBtn) {
    addBudgetBtn.addEventListener('click', function () {
      modal.style.display = 'flex';
    });
  }

  if (modalClose) {
    modalClose.addEventListener('click', function () {
      modal.style.display = 'none';
    });
  }

  window.addEventListener('click', function (e) {
    if (e.target === modal) {
      modal.style.display = 'none';
    }
  });

  if (budgetForm) {
    budgetForm.addEventListener('submit', function (e) {
      e.preventDefault();
      const category = document.getElementById('budget-category').value;
      const limit = document.getElementById('budget-limit').value;

      // TODO: Send to backend API
      alert(`Budget added: ${category} - $${limit}`);

      budgetForm.reset();
      modal.style.display = 'none';
    });
  }
}

function renderBudgetCard(category) {
  const percentUsed = ((category.spent / category.monthlyLimit) * 100).toFixed(0);
  const remaining = category.monthlyLimit - category.spent;
  const isOverBudget = category.spent > category.monthlyLimit;

  let statusClass = '';
  let statusText = `${percentUsed}% used`;

  if (isOverBudget) {
    statusClass = 'over-budget';
    statusText = `${Math.abs(percentUsed - 100)}% used`;
  }

  return `
    <div class="budget-card ${statusClass}">
      <div class="budget-card-header">
        <div class="budget-icon">${category.icon}</div>
        <div class="budget-info">
          <div class="budget-name">${category.name}</div>
          <div class="budget-limit-label">Monthly limit</div>
        </div>
      </div>

      <div class="budget-amounts">
        <div class="budget-spent" style="color: ${category.color};">
          $${category.spent.toLocaleString()}
        </div>
        <div class="budget-total">/ $${category.monthlyLimit.toLocaleString()}</div>
      </div>

      <div class="budget-progress-bar">
        <div class="budget-progress-fill" 
             style="width: ${Math.min(percentUsed, 100)}%; background-color: ${category.color};">
        </div>
      </div>

      <div class="budget-footer">
        <span class="budget-status">${statusText}</span>
        <span class="budget-remaining ${isOverBudget ? 'over' : ''}">
          ${isOverBudget ? `Over by $${Math.abs(remaining)}` : `$${remaining} left`}
        </span>
      </div>
    </div>
  `;
}

