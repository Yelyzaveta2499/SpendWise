function renderBudgets() {
  // Find the budgets section content
  const budgetsSection = document.getElementById('section-budgets');
  if (!budgetsSection) return;

  // Replacing static sample budgetData with dynamic data from backend.


  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();

  function calculateFromApi(budgets, expenses) {
    // budgets: [{id, category, amount, month, year, ...}]
    // expenses: [{category, amount, expenseDate, ...}]

    const budgetsThisMonth = (Array.isArray(budgets) ? budgets : []).filter(b => b.month === currentMonth && b.year === currentYear);

    const totalBudget = budgetsThisMonth.reduce((sum, b) => sum + Number(b.amount || 0), 0);

    const spentByCategory = {};
    (Array.isArray(expenses) ? expenses : []).forEach(e => {
      if (!e || !e.expenseDate) return;
      const d = new Date(e.expenseDate);
      if ((d.getMonth() + 1) !== currentMonth || d.getFullYear() !== currentYear) return;
      const cat = e.category || 'Other';
      spentByCategory[cat] = (spentByCategory[cat] || 0) + Number(e.amount || 0);
    });

    const totalSpent = Object.values(spentByCategory).reduce((sum, v) => sum + v, 0);
    const remaining = totalBudget - totalSpent;

    //categories
    const uiCategories = [
      { name: 'Housing', icon: '🏠' },
      { name: 'Food & Dining', icon: '🍽️' },
      { name: 'Transportation', icon: '🚗' },
      { name: 'Entertainment', icon: '🎬' },
      { name: 'Shopping', icon: '🛍️' },
      { name: 'Utilities', icon: '💡' },
      { name: 'Healthcare', icon: '🏥' },
      { name: 'Personal Care', icon: '✨' }
    ];

    const budgetsByCategory = {};
    budgetsThisMonth.forEach(b => {
      budgetsByCategory[b.category] = b;
    });

    const categories = uiCategories.map(c => {
      const b = budgetsByCategory[c.name];
      const monthlyLimit = b ? Number(b.amount || 0) : 0;
      const spent = spentByCategory[c.name] || 0;


      let color = '#1e3a8a';
      if (monthlyLimit > 0 && spent > monthlyLimit) {
        color = '#dc2626';
      } else if (monthlyLimit > 0 && (spent / monthlyLimit) >= 0.85) {
        color = '#f59e0b';
      }

      return {
        name: c.name,
        icon: c.icon,
        monthlyLimit: monthlyLimit,
        spent: spent,
        color: color,
        budgetId: b ? b.id : null
      };
    });

    return {
      totalBudget,
      totalSpent,
      remaining,
      categories
    };
  }

  function fetchDataAndRender() {
    Promise.all([
      fetch('/api/budgets').then(res => res.ok ? res.json() : []).catch(() => []),
      fetch('/api/expenses').then(res => res.ok ? res.json() : []).catch(() => [])
    ]).then(function(values) {
      const data = calculateFromApi(values[0], values[1]);


      const budgetData = {
        totalBudget: data.totalBudget,
        totalSpent: data.totalSpent,
        remaining: data.remaining,
        categories: data.categories
      };

      const percentSpent = (budgetData.totalBudget > 0)
        ? ((budgetData.totalSpent / budgetData.totalBudget) * 100).toFixed(1)
        : '0.0';

      //innerHTML HTML markup.
      const pageContent = budgetsSection.querySelector('.section-content');
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

      // Modal handlers ( POST to API)
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

          fetch('/api/budgets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              category: category,
              amount: Number(limit),
              month: currentMonth,
              year: currentYear
            })
          })
            .then(function(resp) {
              if (!resp.ok) {
                return resp.json().then(function(err) {
                  throw new Error(err.error || 'Failed to add budget');
                });
              }
              return resp.json();
            })
            .then(function() {
              budgetForm.reset();
              modal.style.display = 'none';
              fetchDataAndRender();
            })
            .catch(function(err) {
              alert(err.message || 'Could not add budget');
            });
        });
      }


    });
  }

  // data render
  fetchDataAndRender();
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
