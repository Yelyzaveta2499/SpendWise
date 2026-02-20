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

    // Use a single persisted monthly allocation row so Total Budget doesn't change on every app start.
    // store it in budgets table with a special category key
    const TOTAL_BUDGET_KEY = '__TOTAL_MONTHLY_BUDGET__';
    const monthlyAllocationRow = budgetsThisMonth.find(b => b.category === TOTAL_BUDGET_KEY);
    const totalBudget = monthlyAllocationRow ? Number(monthlyAllocationRow.amount || 0) : 0;

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
      categories,
      totalBudgetId: monthlyAllocationRow ? monthlyAllocationRow.id : null
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
        categories: data.categories,
        totalBudgetId: data.totalBudgetId
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
          <div class="summary-label" style="display:flex; align-items:center; justify-content: space-between; gap: 10px;">
            <span>Total Budget</span>
            <button type="button" id="edit-total-budget-btn" class="budget-action-btn" style="width:30px;height:30px;border-radius:10px;">✎</button>
          </div>
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

    <!-- Add/Edit Budget Modal -->
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

          <button type="submit" class="btn-submit">Save</button>
        </form>
      </div>
    </div>

    <!-- Total Budget Modal -->
    <div id="total-budget-modal" class="budget-modal" style="display: none;">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Edit Total Budget</h3>
          <button class="modal-close" id="total-budget-modal-close">&times;</button>
        </div>
        <form id="total-budget-form" class="budget-form">
          <input id="total-budget-amount" type="number" step="1" placeholder="Monthly total (e.g., 3200)" required />
          <button type="submit" class="btn-submit">Save</button>
        </form>
      </div>
    </div>
  `;

      // --- Total budget edit ---
      const TOTAL_BUDGET_KEY = '__TOTAL_MONTHLY_BUDGET__';
      const totalBudgetModal = document.getElementById('total-budget-modal');
      const totalBudgetClose = document.getElementById('total-budget-modal-close');
      const totalBudgetForm = document.getElementById('total-budget-form');
      const totalBudgetInput = document.getElementById('total-budget-amount');
      const editTotalBudgetBtn = document.getElementById('edit-total-budget-btn');

      if (editTotalBudgetBtn) {
        editTotalBudgetBtn.addEventListener('click', function() {
          if (totalBudgetInput) totalBudgetInput.value = Number(budgetData.totalBudget || 0);
          if (totalBudgetModal) totalBudgetModal.style.display = 'flex';
        });
      }

      if (totalBudgetClose) {
        totalBudgetClose.addEventListener('click', function() {
          if (totalBudgetModal) totalBudgetModal.style.display = 'none';
        });
      }

      globalThis.addEventListener('click', function(e) {
        if (e.target === totalBudgetModal) {
          totalBudgetModal.style.display = 'none';
        }
      });

      if (totalBudgetForm) {
        totalBudgetForm.addEventListener('submit', function(e) {
          e.preventDefault();
          const amount = Number(totalBudgetInput ? totalBudgetInput.value : 0);
          if (!amount || amount <= 0) {
            alert('Total budget must be greater than 0');
            return;
          }

          const payload = {
            category: TOTAL_BUDGET_KEY,
            amount: amount,
            month: currentMonth,
            year: currentYear
          };

          const hasId = !!budgetData.totalBudgetId;
          const url = hasId ? ('/api/budgets/' + budgetData.totalBudgetId) : '/api/budgets';
          const method = hasId ? 'PUT' : 'POST';

          fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          })
            .then(function(resp) {
              if (!resp.ok) {
                return resp.json().then(function(err) {
                  throw new Error(err.error || 'Failed to save total budget');
                });
              }
              return resp.json();
            })
            .then(function() {
              if (totalBudgetModal) totalBudgetModal.style.display = 'none';
              fetchDataAndRender();
            })
            .catch(function(err) {
              alert(err.message || 'Could not save total budget');
            });
        });
      }

      // Modal handlers ( POST to API)
      const addBudgetBtn = document.getElementById('add-budget-btn');
      const modal = document.getElementById('budget-modal');
      const modalClose = document.getElementById('budget-modal-close');
      const budgetForm = document.getElementById('add-budget-form');

      // Track whether we are editing or adding
      let editingBudgetId = null;

      function openModalForAdd() {
        editingBudgetId = null;
        const titleEl = modal ? modal.querySelector('.modal-header h3') : null;
        if (titleEl) titleEl.textContent = 'Add New Budget';

        // defaults
        const catEl = document.getElementById('budget-category');
        const limitEl = document.getElementById('budget-limit');
        if (catEl) catEl.value = '';
        if (limitEl) limitEl.value = '';

        if (modal) modal.style.display = 'flex';
      }

      function openModalForEdit(budgetId, categoryName, amount) {
        editingBudgetId = budgetId;
        const titleEl = modal ? modal.querySelector('.modal-header h3') : null;
        if (titleEl) titleEl.textContent = 'Edit Budget';

        const catEl = document.getElementById('budget-category');
        const limitEl = document.getElementById('budget-limit');

        if (catEl) catEl.value = categoryName;
        if (limitEl) limitEl.value = amount;

        if (modal) modal.style.display = 'flex';
      }

      if (addBudgetBtn) {
        addBudgetBtn.addEventListener('click', openModalForAdd);
      }

      if (modalClose) {
        modalClose.addEventListener('click', function () {
          if (modal) modal.style.display = 'none';
        });
      }

      globalThis.addEventListener('click', function (e) {
        if (e.target === modal) {
          modal.style.display = 'none';
        }
      });

      // Wire edit/delete buttons for each card
      const editButtons = pageContent.querySelectorAll('.budget-edit-btn');
      editButtons.forEach(function(btn) {
        btn.addEventListener('click', function(ev) {
          ev.preventDefault();
          const id = btn.getAttribute('data-id');
          const category = btn.getAttribute('data-category');
          const amount = btn.getAttribute('data-amount');

          // If id exists -> edit existing budget (PUT).
          // If no id -> treat as set/new budget for this category (POST).
          if (id) {
            openModalForEdit(id, category || '', amount || '');
          } else {
            editingBudgetId = null;
            const titleEl = modal ? modal.querySelector('.modal-header h3') : null;
            if (titleEl) titleEl.textContent = 'Set Budget';

            const catEl = document.getElementById('budget-category');
            const limitEl = document.getElementById('budget-limit');
            if (catEl) catEl.value = category || '';
            if (limitEl) limitEl.value = amount || '';

            if (modal) modal.style.display = 'flex';
          }
        });
      });

      const deleteButtons = pageContent.querySelectorAll('.budget-delete-btn');
      deleteButtons.forEach(function(btn) {
        btn.addEventListener('click', function(ev) {
          ev.preventDefault();
          const id = btn.getAttribute('data-id');
          const category = btn.getAttribute('data-category') || 'this budget';
          if (!id) return;

          const ok = confirm('Delete ' + category + ' budget?');
          if (!ok) return;

          fetch('/api/budgets/' + id, {
            method: 'DELETE'
          })
            .then(function(resp) {
              if (!resp.ok) {
                return resp.json().then(function(err) {
                  throw new Error(err.error || 'Failed to delete budget');
                });
              }
            })
            .then(function() {
              fetchDataAndRender();
            })
            .catch(function(err) {
              alert(err.message || 'Could not delete budget');
            });
        });
      });

      if (budgetForm) {
        budgetForm.addEventListener('submit', function (e) {
          e.preventDefault();
          const category = document.getElementById('budget-category').value;
          const limit = document.getElementById('budget-limit').value;

          // Decide create vs update
          const method = editingBudgetId ? 'PUT' : 'POST';
          const url = editingBudgetId ? ('/api/budgets/' + editingBudgetId) : '/api/budgets';

          const payload = {
            category: category,
            amount: Number(limit),
            month: currentMonth,
            year: currentYear
          };

          fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          })
            .then(function(resp) {
              if (!resp.ok) {
                return resp.json().then(function(err) {
                  throw new Error(err.error || (editingBudgetId ? 'Failed to update budget' : 'Failed to add budget'));
                });
              }
              return resp.json();
            })
            .then(function() {
              budgetForm.reset();
              if (modal) modal.style.display = 'none';
              editingBudgetId = null;
              fetchDataAndRender();
            })
            .catch(function(err) {
              alert(err.message || 'Could not save budget');
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

  // Always show Edit (it acts as "Set budget" when budgetId is missing).
  // Only show Delete when a budget exists.
  const editAttrs = `data-id="${category.budgetId || ''}" data-category="${category.name}" data-amount="${category.monthlyLimit}"`;
  const editBtn = `
          <button type="button" class="budget-action-btn budget-edit-btn" ${editAttrs} aria-label="Edit budget" title="Edit">
            ✎
          </button>`;

  const deleteBtn = category.budgetId ? `
          <button type="button" class="budget-action-btn budget-delete-btn" data-id="${category.budgetId}" data-category="${category.name}" aria-label="Delete budget" title="Delete">
            🗑
          </button>` : '';

  const actionsHtml = `
        <div class="budget-card-actions">
          ${editBtn}
          ${deleteBtn}
        </div>
  `;

  return `
    <div class="budget-card ${statusClass}" data-budget-id="${category.budgetId || ''}" data-category="${category.name}">
      <div class="budget-card-header">
        <div class="budget-icon">${category.icon}</div>
        <div class="budget-info">
          <div class="budget-name">${category.name}</div>
          <div class="budget-limit-label">Monthly limit</div>
        </div>
        ${actionsHtml}
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
