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
          
          <!-- Tags selector -->
          <div class="form-group-tags">
            <label for="exp-tags" class="tags-label">
              Business Tags (optional)
              <span class="tags-hint">Select tags to mark this as a business expense</span>
            </label>
            <div id="exp-tags-container" class="tags-container">
              <div class="tags-loading">Loading tags...</div>
            </div>
          </div>
          
          <button type="submit" class="btn-submit">Add Expense</button>
        </form>
      </div>
    </div>
  `;

  // state object backed by API -> changed from hardcoded expense categories
  const state = {
    items: [],
    editingId: null,
    showAll: false,
    availableTags: [],
    selectedTags: []
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


  function getIconBg(category) {
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

    // Check if expense has tags
    const hasTags = item.tags && item.tags.length > 0;
    const tagCount = hasTags ? item.tags.length : 0;
    const tagBadge = hasTags ? `
      <div class="expense-tag-badge" title="${item.tags.join(', ')}">
        🏷️ ${tagCount}
      </div>
    ` : '';

    // Business indicator for tagged expenses
    const businessBadge = hasTags ? `
      <div class="business-indicator" title="Business Expense (Tagged)">
        💼 BIZ
      </div>
    ` : '';

    // Corner ribbon for business expenses
    const businessCornerRibbon = hasTags ? `
      <div class="business-corner-ribbon" title="Business Expense">
        <span>💼</span>
      </div>
    ` : '';


    const itemClass = hasTags ? 'transaction-item business-expense' : 'transaction-item';

    return `
      <div class="${itemClass}" data-id="${item.id || ''}">
        ${businessCornerRibbon}
        <div class="transaction-left">
          <div class="category-icon" style="background-color: ${iconBg};">
            ${icon}
          </div>
          <div class="transaction-details">
            <div class="transaction-name">
              ${item.name}
              ${tagBadge}
              ${businessBadge}
            </div>
            <div class="transaction-category">${item.category}</div>
          </div>
        </div>
        <div class="transaction-right">
          <div class="expense-right-top">
            <div class="expense-row-actions">
              <button type="button" class="expense-action-btn expense-edit-btn" data-id="${item.id || ''}">
                ✎
              </button>
              <button type="button" class="expense-action-btn expense-delete-btn" data-id="${item.id || ''}">
                🗑
              </button>
            </div>
            <div class="${amountClass}">${amountStr}</div>
          </div>
          <div class="transaction-date">${formatDate(item.expenseDate || item.date)}</div>
        </div>
      </div>
    `;
  }

  // Render the list with search and filter
  function renderList() {
    const q = (searchEl.value || '').toLowerCase();
    const cat = categoryEl.value || '';

    const filtered = state.items.filter(function (i) {
      const nameMatch = !q || (i.name && i.name.toLowerCase().includes(q));
      const categoryText = i.category ? i.category.toLowerCase() : '';
      const categoryMatchSearch = !q || categoryText.includes(q);
      const categoryMatchFilter = !cat || i.category === cat;
      return (nameMatch || categoryMatchSearch) && categoryMatchFilter;
    });

    const visibleItems = state.showAll ? filtered : filtered.slice(0, 10);

    listEl.innerHTML = visibleItems.map(formatItem).join('');

    if (filtered.length > 10) {
      const toggleLabel = state.showAll ? 'Hide older' : 'Show older';
      const toggle = document.createElement('button');
      toggle.type = 'button';
      toggle.textContent = toggleLabel;
      toggle.className = 'btn-show-more-expenses';
      toggle.style.margin = '8px 16px 16px auto';
      toggle.style.display = 'block';
      toggle.style.background = 'transparent';
      toggle.style.border = 'none';
      toggle.style.color = '#1e3a8a';
      toggle.style.cursor = 'pointer';
      toggle.style.fontWeight = '600';
      listEl.appendChild(toggle);

      toggle.addEventListener('click', function () {
        state.showAll = !state.showAll;
        renderList();
      });
    }

    // edit/delete buttons
    const editButtons = listEl.querySelectorAll('.expense-edit-btn');
    editButtons.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        const id = btn.getAttribute('data-id');
        if (!id) return;
        const expense = state.items.find(function(it) { return String(it.id) === String(id); });
        if (!expense) return;
        state.editingId = id;

        const titleEl = document.querySelector('#expense-modal .modal-header h3');
        if (titleEl) titleEl.textContent = 'Edit Expense';

        document.getElementById('exp-name').value = expense.name || '';
        document.getElementById('exp-category').value = expense.category || '';
        document.getElementById('exp-amount').value = Number(expense.amount || 0);
        document.getElementById('exp-date').value = (expense.expenseDate || expense.date || '').slice(0, 10);

        // Load tags and pre-select the expense's current tags
        loadAvailableTags().then(function() {
          // Fetch current tags for this expense
          return fetch('/api/expenses/' + id + '/tags')
            .then(function(response) {
              if (!response.ok) return [];
              return response.json();
            })
            .catch(function() {
              return [];
            });
        }).then(function(expenseTags) {
          const tagIds = expenseTags.map(function(tag) { return tag.id; });
          state.selectedTags = tagIds;
          renderTagSelector(tagIds);
        });

        modal.style.display = 'flex';
      });
    });

    const deleteButtons = listEl.querySelectorAll('.expense-delete-btn');
    deleteButtons.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        const id = btn.getAttribute('data-id');
        if (!id) return;
        if (!confirm('Delete this expense?')) return;

        fetch('/api/expenses/' + id, { method: 'DELETE' })
          .then(function(resp) {
            if (!resp.ok) {
              return resp.json().then(function(err) {
                throw new Error(err.error || 'Failed to delete expense');
              }).catch(function() {
                throw new Error('Failed to delete expense');
              });
            }
          })
          .then(function() {
            state.items = state.items.filter(function(it) { return String(it.id) !== String(id); });
            renderList();
          })
          .catch(function(err) {
            alert(err.message || 'Could not delete expense');
          });
      });
    });
  }

  // Load available tags from API
  function loadAvailableTags() {
    return fetch('/api/tags')
      .then(function(response) {
        if (!response.ok) return [];
        return response.json();
      })
      .then(function(tags) {
        state.availableTags = tags || [];
        return tags;
      })
      .catch(function() {
        state.availableTags = [];
        return [];
      });
  }

  // Render tag checkboxes in modal
  function renderTagSelector(selectedTagIds) {
    const container = document.getElementById('exp-tags-container');
    if (!container) return;

    selectedTagIds = selectedTagIds || [];

    // Ensure state.selectedTags is synchronized
    state.selectedTags = selectedTagIds;

    if (state.availableTags.length === 0) {
      container.innerHTML = '<div class="tags-empty">No tags available. Create tags in Business section.</div>';
      return;
    }

    const checkboxes = state.availableTags.map(function(tag) {
      const isChecked = selectedTagIds.includes(tag.id);
      return `
        <label class="tag-checkbox-label">
          <input 
            type="checkbox" 
            class="tag-checkbox" 
            value="${tag.id}" 
            ${isChecked ? 'checked' : ''}
          />
          <span class="tag-checkbox-box"></span>
          <span class="tag-checkbox-text" style="color: ${tag.color};">
            ${tag.name}
          </span>
        </label>
      `;
    }).join('');

    container.innerHTML = checkboxes;

    // Update selected tags when checkboxes change
    const checkboxEls = container.querySelectorAll('.tag-checkbox');
    checkboxEls.forEach(function(checkbox) {
      checkbox.addEventListener('change', function() {
        state.selectedTags = Array.from(container.querySelectorAll('.tag-checkbox:checked'))
          .map(function(cb) { return parseInt(cb.value); });
      });
    });
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
        const expenses = Array.isArray(data) ? data : [];

        // Fetch tags for each expense
        const tagFetchPromises = expenses.map(function(expense) {
          return fetch('/api/expenses/' + expense.id + '/tags')
            .then(function(response) {
              if (!response.ok) return [];
              return response.json();
            })
            .then(function(tags) {
              // Add tag names to expense object
              expense.tags = tags.map(function(tag) { return tag.name; });
              return expense;
            })
            .catch(function() {
              expense.tags = [];
              return expense;
            });
        });

        // Wait for all tag fetches to complete
        return Promise.all(tagFetchPromises);
      })
      .then(function(expensesWithTags) {
        state.items = expensesWithTags;
        renderList();
      })
      .catch(function () {
        listEl.innerHTML = '<div style="padding: 16px; color: #dc3545;">Could not load expenses. Please try again later.</div>';
      });
  }

  loadExpensesFromApi();

  // Load tags on page load
  loadAvailableTags();

  // Modal handlers
  addBtn.addEventListener('click', function () {
    state.editingId = null;
    state.selectedTags = [];
    const titleEl = document.querySelector('#expense-modal .modal-header h3');
    if (titleEl) titleEl.textContent = 'Add New Expense';
    formEl.reset();

    // Load and render tags
    loadAvailableTags().then(function() {
      renderTagSelector([]);
    });

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

  // Form submit handler - add new expense via API OR update existing
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
      date: date,
      tags: state.selectedTags  // Include selected tag IDs
    };

    const isEdit = !!state.editingId;
    const url = isEdit ? ('/api/expenses/' + state.editingId) : '/api/expenses';
    const method = isEdit ? 'PUT' : 'POST';

    console.log('Submitting expense:', payload);

    fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
      .then(function (response) {
        if (!response.ok) {
          return response.json().then(function (err) {
            console.error('Server error:', err);
            var msg = err && err.error ? err.error : (isEdit ? 'Failed to update expense' : 'Failed to create expense');
            throw new Error(msg);
          }).catch(function (parseErr) {
            console.error('Error parsing response:', parseErr);
            throw new Error(isEdit ? 'Failed to update expense' : 'Failed to create expense');
          });
        }
        return response.json();
      })
      .then(function(result) {
        console.log('Expense saved successfully:', result);
        modal.style.display = 'none';
        formEl.reset();
        state.selectedTags = [];
        state.editingId = null;
        loadExpensesFromApi();
      })
      .catch(function (err) {
        console.error('Error saving expense:', err);
        alert(err.message || 'Error saving expense');
      });
  });

  searchEl.addEventListener('input', renderList);
  categoryEl.addEventListener('change', renderList);
}
