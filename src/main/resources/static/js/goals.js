function renderGoals() {
    const section = document.getElementById('section-goals');
    if (!section) return;
    const contentDiv = section.querySelector('.section-content');
    if (!contentDiv) return;
    contentDiv.innerHTML = '<div style="text-align: center; padding: 40px; color: #64748b;">Loading goals...</div>';
    fetchGoalsAndRender(contentDiv);
}
function fetchGoalsAndRender(contentDiv) {
    const placeholderGoals = [
        { id: 'demo-1', name: 'Emergency Fund', icon: '🛡️', color: '#10b981', currentAmount: 7500, targetAmount: 10000, deadline: '2025-12-01', isPlaceholder: true },
        { id: 'demo-2', name: 'New Car', icon: '🚗', color: '#475569', currentAmount: 8200, targetAmount: 25000, deadline: '2025-11-15', isPlaceholder: true },
        { id: 'demo-3', name: 'Vacation Fund', icon: '✈️', color: '#0ea5e9', currentAmount: 3200, targetAmount: 5000, deadline: '2025-10-20', isPlaceholder: true },
        { id: 'demo-4', name: 'Home Down Payment', icon: '🏠', color: '#a855f7', currentAmount: 12000, targetAmount: 50000, deadline: '2026-11-30', isPlaceholder: true }
    ];
    Promise.all([
        fetch('/api/goals').then(res => res.ok ? res.json() : []).catch(() => []),
        fetch('/api/goals/summary').then(res => res.ok ? res.json() : null).catch(() => null)
    ])
    .then(([backendGoals, backendSummary]) => {
        const summary = backendSummary || { totalSaved: 30900, totalTarget: 90000, activeGoals: 4 };
        const allGoals = [...backendGoals, ...placeholderGoals];
        renderGoalsContent(contentDiv, allGoals, summary);
    })
    .catch(() => {
        const summary = { totalSaved: 30900, totalTarget: 90000, activeGoals: 4 };
        renderGoalsContent(contentDiv, placeholderGoals, summary);
    });
}
function renderGoalsContent(contentDiv, goals, summary) {
    const html = `
        <div class="goals-container">
            <div class="goals-header-row">
                <button class="btn-create-goal" id="btnCreateGoal"><span class="btn-icon">+</span><span>Create Goal</span></button>
                
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(16, 185, 129, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg></div>
                    <div class="summary-content"><div class="summary-label">Total Saved</div><div class="summary-value">$${summary.totalSaved.toLocaleString()}</div></div>
                </div>
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(59, 130, 246, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path></svg></div>
                    <div class="summary-content"><div class="summary-label">Total Target</div><div class="summary-value">$${summary.totalTarget.toLocaleString()}</div></div>
                </div>
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(168, 85, 247, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#a855f7" stroke-width="2"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"></path></svg></div>
                    <div class="summary-content"><div class="summary-label">Active Goals</div><div class="summary-value">${summary.activeGoals}</div></div>
                </div>
            </div>
            <div class="goals-grid">${goals.map(goal => renderGoalCard(goal)).join('')}</div>
        </div>
        
        <!-- Goal Creation Modal -->
        <div id="goal-modal" class="budget-modal" style="display: none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 id="goal-modal-title">Create New Goal</h3>
                    <button class="modal-close" id="goal-modal-close">&times;</button>
                </div>
                <form id="goal-form" class="budget-form">
                    <input id="goal-name" type="text" placeholder="Goal name (e.g., Emergency Fund)" required maxlength="50" />
                    
                    <input id="goal-target" type="number" step="0.01" placeholder="Target amount (e.g., 10000)" required min="1" />
                    
                    <input id="goal-deadline" type="date" placeholder="Deadline (optional)" />
                    
                    <div style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                        <label style="font-size: 14px; color: #64748b; font-weight: 500;">Icon:</label>
                        <select id="goal-icon" style="flex: 1; padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px;">
                            <option value="🎯">🎯 Target</option>
                            <option value="🏠">🏠 Home</option>
                            <option value="🚗">🚗 Car</option>
                            <option value="✈️">✈️ Travel</option>
                            <option value="🛡️">🛡️ Emergency</option>
                            <option value="💰">💰 Savings</option>
                            <option value="🎓">🎓 Education</option>
                            <option value="💍">💍 Wedding</option>
                            <option value="📱">📱 Electronics</option>
                            <option value="🏖️">🏖️ Vacation</option>
                        </select>
                    </div>
                    
                    <div style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                        <label style="font-size: 14px; color: #64748b; font-weight: 500;">Color:</label>
                        <select id="goal-color" style="flex: 1; padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px;">
                            <option value="#10b981">🟢 Green</option>
                            <option value="#3b82f6">🔵 Blue</option>
                            <option value="#a855f7">🟣 Purple</option>
                            <option value="#f59e0b">🟠 Orange</option>
                            <option value="#ef4444">🔴 Red</option>
                            <option value="#ec4899">🩷 Pink</option>
                            <option value="#14b8a6">🐬 Teal</option>
                            <option value="#475569">⚫ Dark Gray</option>
                        </select>
                    </div>
                    
                    <button type="submit" class="btn-submit">Create Goal</button>
                </form>
            </div>
        </div>
        
        <!-- Add Contribution Modal -->
        <div id="contribution-modal" class="budget-modal" style="display: none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 id="contribution-modal-title">Add Contribution</h3>
                    <button class="modal-close" id="contribution-modal-close">&times;</button>
                </div>
                <form id="contribution-form" class="budget-form">
                    <input id="contribution-amount" type="number" step="0.01" placeholder="Contribution amount (e.g., 500)" required min="0.01" />
                    
                    <textarea id="contribution-note" placeholder="Add a note (optional)" rows="3" style="width: 100%; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; font-family: inherit; resize: vertical;"></textarea>
                    
                    <button type="submit" class="btn-submit">Add Contribution</button>
                </form>
            </div>
        </div>
        
        <!-- Edit Goal Modal -->
        <div id="edit-goal-modal" class="budget-modal" style="display: none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>Edit Goal</h3>
                    <button class="modal-close" id="edit-goal-modal-close">&times;</button>
                </div>
                <form id="edit-goal-form" class="budget-form">
                    <input id="edit-goal-name" type="text" placeholder="Goal name" required maxlength="50" />
                    
                    <input id="edit-goal-target" type="number" step="0.01" placeholder="Target amount" required min="1" />
                    
                    <input id="edit-goal-deadline" type="date" placeholder="Deadline (optional)" />
                    
                    <div style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                        <label style="font-size: 14px; color: #64748b; font-weight: 500;">Icon:</label>
                        <select id="edit-goal-icon" style="flex: 1; padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px;">
                            <option value="🎯">🎯 Target</option>
                            <option value="🏠">🏠 Home</option>
                            <option value="🚗">🚗 Car</option>
                            <option value="✈️">✈️ Travel</option>
                            <option value="🛡️">🛡️ Emergency</option>
                            <option value="💰">💰 Savings</option>
                            <option value="🎓">🎓 Education</option>
                            <option value="💍">💍 Wedding</option>
                            <option value="📱">📱 Electronics</option>
                            <option value="🏖️">🏖️ Vacation</option>
                        </select>
                    </div>
                    
                    <div style="display: flex; gap: 12px; align-items: center; margin: 8px 0;">
                        <label style="font-size: 14px; color: #64748b; font-weight: 500;">Color:</label>
                        <select id="edit-goal-color" style="flex: 1; padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px;">
                            <option value="#10b981">🟢 Green</option>
                            <option value="#3b82f6">🔵 Blue</option>
                            <option value="#a855f7">🟣 Purple</option>
                            <option value="#f59e0b">🟠 Orange</option>
                            <option value="#ef4444">🔴 Red</option>
                            <option value="#ec4899">🩷 Pink</option>
                            <option value="#14b8a6">🐬 Teal</option>
                            <option value="#475569">⚫ Dark Gray</option>
                        </select>
                    </div>
                    
                    <button type="submit" class="btn-submit">Update Goal</button>
                </form>
            </div>
        </div>
    `;
    contentDiv.innerHTML = html;
    attachGoalEventListeners();
}
function renderGoalCard(goal) {
    const currentAmount = goal.currentAmount || 0;
    const targetAmount = goal.targetAmount || 0;
    const percentage = targetAmount > 0 ? Math.round((currentAmount / targetAmount) * 100) : 0;
    const remaining = targetAmount - currentAmount;
    const icon = goal.icon || '🎯';
    const color = goal.color || '#10b981';
    const darkColor = adjustColorDarkness(color);
    let deadlineText = 'No deadline';
    if (goal.deadline) {
        const deadline = new Date(goal.deadline);
        const today = new Date();
        const isPassed = deadline < today;
        const daysLeft = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24));
        deadlineText = isPassed ? 'Deadline passed' : daysLeft > 0 ? ` days left` : 'Due today';
    }
    const editAttrs = `data-id="${goal.id}" data-name="${goal.name}" data-target="${targetAmount}" data-current="${currentAmount}" data-is-placeholder="${goal.isPlaceholder || false}"`;
    const editBtn = `<button type="button" class="budget-action-btn budget-edit-btn" ${editAttrs} aria-label="Edit goal" title="Edit">✎</button>`;
    const deleteBtn = `<button type="button" class="budget-action-btn budget-delete-btn" data-id="${goal.id}" data-name="${goal.name}" data-is-placeholder="${goal.isPlaceholder || false}" aria-label="Delete goal" title="Delete">🗑</button>`;
    const actionsHtml = `<div class="budget-card-actions">${editBtn}${deleteBtn}</div>`;
    return `
        <div class="goal-card" data-goal-id="${goal.id}">
            <div class="goal-card-header" style="background: ${color};">
                <div class="goal-header-content">
                    <div class="goal-icon-name"><div class="goal-icon">${icon}</div><div class="goal-name">${goal.name}</div></div>
                    <div class="goal-percentage">${percentage}%</div>
                </div>
                <div class="goal-deadline">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    <span>${deadlineText}</span>
                </div>
            </div>
            <div class="goal-card-body">
                <div class="goal-amount-row">
                    <div class="goal-amount"><span class="amount-current">$${currentAmount.toLocaleString()}</span><span class="amount-target">of $${targetAmount.toLocaleString()}</span></div>
                    ${actionsHtml}
                </div>
                <div class="goal-progress-wrapper"><div class="goal-progress-bar"><div class="goal-progress-fill" style="width: ${percentage}%; background: ${darkColor};"></div></div></div>
                <div class="goal-footer"><div class="goal-remaining">$${remaining.toLocaleString()} remaining</div><div class="goal-monthly">Target: $${targetAmount.toLocaleString()}</div></div>
                <button class="btn-add-contribution" data-goal-id="${goal.id}" data-is-placeholder="${goal.isPlaceholder || false}">Add Contribution</button>
            </div>
        </div>
    `;
}
function adjustColorDarkness(hexColor) {
    if (!hexColor || !hexColor.startsWith('#')) return '#047857';
    const hex = hexColor.replace('#', '');
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);
    const factor = 0.7;
    return '#' + ((Math.floor(r * factor) << 16) | (Math.floor(g * factor) << 8) | Math.floor(b * factor)).toString(16).padStart(6, '0');
}
function attachGoalEventListeners() {
    const createBtn = document.getElementById('btnCreateGoal');
    const modal = document.getElementById('goal-modal');
    const modalClose = document.getElementById('goal-modal-close');
    const goalForm = document.getElementById('goal-form');

    const contributionModal = document.getElementById('contribution-modal');
    const contributionModalClose = document.getElementById('contribution-modal-close');
    const contributionForm = document.getElementById('contribution-form');

    const editModal = document.getElementById('edit-goal-modal');
    const editModalClose = document.getElementById('edit-goal-modal-close');
    const editGoalForm = document.getElementById('edit-goal-form');

    let currentGoalId = null;
    let currentEditGoalId = null;

    // Create Goal Modal
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            document.getElementById('goal-modal-title').textContent = 'Create New Goal';
            document.getElementById('goal-name').value = '';
            document.getElementById('goal-target').value = '';
            document.getElementById('goal-deadline').value = '';
            document.getElementById('goal-icon').value = '🎯';
            document.getElementById('goal-color').value = '#10b981';
            if (modal) modal.style.display = 'flex';
        });
    }

    if (modalClose) {
        modalClose.addEventListener('click', () => {
            if (modal) modal.style.display = 'none';
        });
    }

    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.style.display = 'none';
        });
    }

    if (goalForm) {
        goalForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const name = document.getElementById('goal-name').value.trim();
            const targetAmount = parseFloat(document.getElementById('goal-target').value);
            const deadline = document.getElementById('goal-deadline').value || null;
            const icon = document.getElementById('goal-icon').value;
            const color = document.getElementById('goal-color').value;

            if (name && targetAmount > 0) {
                createNewGoal(name, targetAmount, deadline, icon, color);
                if (modal) modal.style.display = 'none';
            }
        });
    }

    // Edit Goal Modal
    if (editModalClose) {
        editModalClose.addEventListener('click', () => {
            if (editModal) editModal.style.display = 'none';
        });
    }

    if (editModal) {
        editModal.addEventListener('click', (e) => {
            if (e.target === editModal) editModal.style.display = 'none';
        });
    }

    if (editGoalForm) {
        editGoalForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const name = document.getElementById('edit-goal-name').value.trim();
            const targetAmount = parseFloat(document.getElementById('edit-goal-target').value);
            const deadline = document.getElementById('edit-goal-deadline').value || null;
            const icon = document.getElementById('edit-goal-icon').value;
            const color = document.getElementById('edit-goal-color').value;

            if (name && targetAmount > 0 && currentEditGoalId) {
                updateGoal(currentEditGoalId, { name, targetAmount, deadline, icon, color });
                if (editModal) editModal.style.display = 'none';
            }
        });
    }

    // Contribution Modal
    if (contributionModalClose) {
        contributionModalClose.addEventListener('click', () => {
            if (contributionModal) contributionModal.style.display = 'none';
        });
    }

    if (contributionModal) {
        contributionModal.addEventListener('click', (e) => {
            if (e.target === contributionModal) contributionModal.style.display = 'none';
        });
    }

    if (contributionForm) {
        contributionForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const amount = parseFloat(document.getElementById('contribution-amount').value);
            const note = document.getElementById('contribution-note').value.trim() || '';

            if (amount > 0 && currentGoalId) {
                addContribution(currentGoalId, amount, note);
                if (contributionModal) contributionModal.style.display = 'none';
            }
        });
    }

    // Edit buttons - Open edit modal with current goal data
    document.querySelectorAll('.budget-edit-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();

            currentEditGoalId = btn.dataset.id;
            const goalName = btn.dataset.name;
            const targetAmount = btn.dataset.target;

            // Pre-fill the edit form
            document.getElementById('edit-goal-name').value = goalName;
            document.getElementById('edit-goal-target').value = targetAmount;

            // Open edit modal
            if (editModal) editModal.style.display = 'flex';
        });
    });

    // Delete buttons - Direct delete with confirmation
    document.querySelectorAll('.budget-delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const goalId = btn.dataset.id;
            const goalName = btn.dataset.name;

            if (confirm(`Are you sure you want to delete the goal "${goalName}"?`)) {
                deleteGoal(goalId);
            }
        });
    });

    // Add Contribution buttons
    document.querySelectorAll('.btn-add-contribution').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();

            // Open contribution modal for all cards
            currentGoalId = btn.dataset.goalId;
            document.getElementById('contribution-amount').value = '';
            document.getElementById('contribution-note').value = '';
            if (contributionModal) contributionModal.style.display = 'flex';
        });
    });
}
function createNewGoal(name, targetAmount, deadline, icon, color) {
    const goalData = { name, targetAmount, deadline, icon, color };
    fetch('/api/goals', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(goalData) })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { renderGoals(); })
        .catch(err => console.error('Error creating goal:', err));
}
function updateGoal(id, updates) {
    // Check if it's a demo card
    if (id && id.toString().startsWith('demo-')) {
        console.log('Demo card - updates not saved:', { id, updates });
        renderGoals(); // Just refresh to show the UI works
        return;
    }

    fetch(`/api/goals/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updates) })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { renderGoals(); })
        .catch(err => console.error('Error updating goal:', err));
}

function deleteGoal(id) {
    // Check if it's a demo card
    if (id && id.toString().startsWith('demo-')) {
        console.log('Demo card - delete not saved:', { id });
        renderGoals(); // Just refresh to show the UI works
        return;
    }

    fetch(`/api/goals/${id}`, { method: 'DELETE' })
        .then(res => { if (!res.ok) throw new Error('Failed'); renderGoals(); })
        .catch(err => console.error('Error deleting goal:', err));
}
function addContribution(id, amount, note) {
    // Check if it's a placeholder/demo card
    if (id && id.toString().startsWith('demo-')) {
        console.log('Demo card - contribution not saved:', { id, amount, note });
        renderGoals(); // Just refresh to show the UI works
        return;
    }

    fetch(`/api/goals/${id}/contributions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount, note })
    })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { renderGoals(); })
        .catch(err => console.error('Error adding contribution:', err));
}
window.renderGoals = renderGoals;
