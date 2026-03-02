// Goals with Backend + Placeholder Cards
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
            <div class="goals-header">
                <div class="goals-header-left">
                    <h1 class="goals-title">Goals</h1>
                    <p class="goals-subtitle">Track your savings and financial milestones</p>
                </div>
                <button class="btn-create-goal" id="btnCreateGoal"><span class="btn-icon">+</span><span>Create Goal</span></button>
            </div>
            <div class="goals-summary-grid">
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(16, 185, 129, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg></div>
                    <div class="summary-content"><div class="summary-label">Total Saved</div><div class="summary-value">${summary.totalSaved.toLocaleString()}</div></div>
                </div>
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(59, 130, 246, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path></svg></div>
                    <div class="summary-content"><div class="summary-label">Total Target</div><div class="summary-value">${summary.totalTarget.toLocaleString()}</div></div>
                </div>
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(168, 85, 247, 0.1);"><svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#a855f7" stroke-width="2"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"></path></svg></div>
                    <div class="summary-content"><div class="summary-label">Active Goals</div><div class="summary-value">${summary.activeGoals}</div></div>
                </div>
            </div>
            <div class="goals-grid">${goals.map(goal => renderGoalCard(goal)).join('')}</div>
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
    const placeholderBadge = goal.isPlaceholder ? '<span style="position: absolute; top: 8px; right: 8px; background: rgba(255,255,255,0.2); color: white; padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 600;">DEMO</span>' : '';
    const editAttrs = `data-id="${goal.id}" data-name="${goal.name}" data-target="${targetAmount}" data-current="${currentAmount}" data-is-placeholder="${goal.isPlaceholder || false}"`;
    const editBtn = `<button type="button" class="budget-action-btn budget-edit-btn" ${editAttrs} aria-label="Edit goal" title="Edit">✎</button>`;
    const deleteBtn = `<button type="button" class="budget-action-btn budget-delete-btn" data-id="${goal.id}" data-name="${goal.name}" data-is-placeholder="${goal.isPlaceholder || false}" aria-label="Delete goal" title="Delete">🗑</button>`;
    const actionsHtml = `<div class="budget-card-actions">${editBtn}${deleteBtn}</div>`;
    return `
        <div class="goal-card" data-goal-id="${goal.id}">
            <div class="goal-card-header" style="background: ${color};">
                ${placeholderBadge}
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
                ${actionsHtml}
                <div class="goal-amount"><span class="amount-current">$${currentAmount.toLocaleString()}</span><span class="amount-target">of $${targetAmount.toLocaleString()}</span></div>
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
    if (createBtn) createBtn.addEventListener('click', createNewGoal);
    document.querySelectorAll('.budget-edit-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (btn.dataset.isPlaceholder === 'true') { alert('This is a demo card. Create a real goal to edit it!'); return; }
            const newTarget = prompt(`Edit target amount for "${btn.dataset.name}":`, btn.dataset.target);
            if (newTarget && !isNaN(newTarget) && parseFloat(newTarget) > 0) {
                updateGoal(btn.dataset.id, { targetAmount: parseFloat(newTarget) });
            } else if (newTarget !== null) alert('Please enter a valid amount');
        });
    });
    document.querySelectorAll('.budget-delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (btn.dataset.isPlaceholder === 'true') { alert('This is a demo card. Create a real goal to manage it!'); return; }
            if (confirm(`Are you sure you want to delete the goal "${btn.dataset.name}"?`)) deleteGoal(btn.dataset.id);
        });
    });
    document.querySelectorAll('.btn-add-contribution').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (btn.dataset.isPlaceholder === 'true') { alert('This is a demo card. Create a real goal to add contributions!'); return; }
            addContribution(btn.dataset.goalId);
        });
    });
}
function createNewGoal() {
    const name = prompt('Enter goal name:');
    if (!name) return;
    const targetAmount = prompt('Enter target amount:');
    if (!targetAmount || isNaN(targetAmount) || parseFloat(targetAmount) <= 0) { alert('Please enter a valid target amount'); return; }
    const deadline = prompt('Enter deadline (YYYY-MM-DD) or leave empty:');
    const goalData = { name: name.trim(), targetAmount: parseFloat(targetAmount), deadline: deadline || null, icon: '🎯', color: '#10b981' };
    fetch('/api/goals', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(goalData) })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { alert('Goal created!'); renderGoals(); })
        .catch(err => alert('Error: ' + err.message));
}
function updateGoal(id, updates) {
    fetch(`/api/goals/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updates) })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { alert('Goal updated!'); renderGoals(); })
        .catch(err => alert('Error: ' + err.message));
}
function deleteGoal(id) {
    fetch(`/api/goals/${id}`, { method: 'DELETE' })
        .then(res => { if (!res.ok) throw new Error('Failed'); alert('Goal deleted!'); renderGoals(); })
        .catch(err => alert('Error: ' + err.message));
}
function addContribution(id) {
    const amount = prompt('Enter contribution amount:');
    if (!amount || isNaN(amount) || parseFloat(amount) <= 0) { if (amount !== null) alert('Please enter a valid amount'); return; }
    const note = prompt('Add a note (optional):') || '';
    fetch(`/api/goals/${id}/contributions`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amount: parseFloat(amount), note }) })
        .then(res => { if (!res.ok) throw new Error('Failed'); return res.json(); })
        .then(() => { alert('Contribution added!'); renderGoals(); })
        .catch(err => alert('Error: ' + err.message));
}
window.renderGoals = renderGoals;
