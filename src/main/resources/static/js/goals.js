function renderGoals() {
    const section = document.getElementById('section-goals');
    if (!section) return;

    const contentDiv = section.querySelector('.section-content');
    if (!contentDiv) return;

    const goalsData = {
        totalSaved: 30900,
        totalTarget: 90000,
        activeGoals: 4,
        goals: [
            {
                id: 1,
                name: 'Emergency Fund',
                icon: '🛡️',
                color: '#10b981',
                darkColor: '#047857',
                saved: 7500,
                target: 10000,
                deadline: new Date('2025-12-01'),
                monthlyContribution: 2500
            },
            {
                id: 2,
                name: 'New Car',
                icon: '🚗',
                color: '#475569',
                darkColor: '#1e293b',
                saved: 8200,
                target: 25000,
                deadline: new Date('2025-11-15'),
                monthlyContribution: 16800
            },
            {
                id: 3,
                name: 'Vacation Fund',
                icon: '✈️',
                color: '#0ea5e9',
                darkColor: '#0369a1',
                saved: 3200,
                target: 5000,
                deadline: new Date('2025-10-20'),
                monthlyContribution: 1800
            },
            {
                id: 4,
                name: 'Home Down Payment',
                icon: '🏠',
                color: '#a855f7',
                darkColor: '#7e22ce',
                saved: 12000,
                target: 50000,
                deadline: new Date('2026-11-30'),
                monthlyContribution: 38000
            }
        ]
    };

    const html = `
        <div class="goals-container">
            <!-- Header -->
            <div class="goals-header">
                <div class="goals-header-left">
                    <h1 class="goals-title">Goals</h1>
                    <p class="goals-subtitle">Track your savings and financial milestones</p>
                </div>
                <button class="btn-create-goal">
                    <span class="btn-icon">+</span>
                    <span>Create Goal</span>
                </button>
            </div>

            <!-- Summary Cards -->
            <div class="goals-summary-grid">
                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(16, 185, 129, 0.1);">
                        <svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
                            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                        </svg>
                    </div>
                    <div class="summary-content">
                        <div class="summary-label">Total Saved</div>
                        <div class="summary-value">$${goalsData.totalSaved.toLocaleString()}</div>
                    </div>
                </div>

                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(59, 130, 246, 0.1);">
                        <svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <path d="M12 6v6l4 2"></path>
                        </svg>
                    </div>
                    <div class="summary-content">
                        <div class="summary-label">Total Target</div>
                        <div class="summary-value">$${goalsData.totalTarget.toLocaleString()}</div>
                    </div>
                </div>

                <div class="goal-summary-card">
                    <div class="summary-icon-wrapper" style="background: rgba(168, 85, 247, 0.1);">
                        <svg class="summary-icon" viewBox="0 0 24 24" fill="none" stroke="#a855f7" stroke-width="2">
                            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"></path>
                        </svg>
                    </div>
                    <div class="summary-content">
                        <div class="summary-label">Active Goals</div>
                        <div class="summary-value">${goalsData.activeGoals}</div>
                    </div>
                </div>
            </div>

            <!-- Goals Grid -->
            <div class="goals-grid">
                ${goalsData.goals.map(goal => renderGoalCard(goal)).join('')}
            </div>
        </div>
    `;

    contentDiv.innerHTML = html;

    // event listeners
    attachGoalEventListeners();
}

function renderGoalCard(goal) {
    const percentage = Math.round((goal.saved / goal.target) * 100);
    const remaining = goal.target - goal.saved;
    const monthlyNeeded = remaining;

    // Check if deadline has passed
    const today = new Date();
    const isPassed = goal.deadline < today;
    const daysLeft = Math.ceil((goal.deadline - today) / (1000 * 60 * 60 * 24));

    const deadlineText = isPassed
        ? 'Deadline passed'
        : daysLeft > 0
            ? `${daysLeft} days left`
            : 'Due today';

    return `
        <div class="goal-card" data-goal-id="${goal.id}">
            <div class="goal-card-header" style="background: ${goal.color};">
                <div class="goal-header-content">
                    <div class="goal-icon-name">
                        <div class="goal-icon">${goal.icon}</div>
                        <div class="goal-name">${goal.name}</div>
                    </div>
                    <div class="goal-percentage">${percentage}%</div>
                </div>
                <div class="goal-deadline">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    <span>${deadlineText}</span>
                </div>
            </div>

            <div class="goal-card-body">
                <div class="goal-amount">
                    <span class="amount-current">$${goal.saved.toLocaleString()}</span>
                    <span class="amount-target">of $${goal.target.toLocaleString()}</span>
                </div>

                <div class="goal-progress-wrapper">
                    <div class="goal-progress-bar">
                        <div class="goal-progress-fill" style="width: ${percentage}%; background: ${goal.darkColor};"></div>
                    </div>
                </div>

                <div class="goal-footer">
                    <div class="goal-remaining">$${remaining.toLocaleString()} remaining</div>
                    <div class="goal-monthly">~$${monthlyNeeded.toLocaleString()}/month</div>
                </div>

                <button class="btn-add-contribution" data-goal-id="${goal.id}">
                    Add Contribution
                </button>
            </div>
        </div>
    `;
}

function attachGoalEventListeners() {
    // Create Goal button
    const createBtn = document.querySelector('.btn-create-goal');
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            alert('Create Goal modal will be implemented with backend');
        });
    }

    // contribution buttons
    const contributionBtns = document.querySelectorAll('.btn-add-contribution');
    contributionBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const goalId = e.target.dataset.goalId;
            alert(`Add contribution to goal ${goalId} - will be implemented with backend`);
        });
    });

    // Goal card click (for viewing details)
    const goalCards = document.querySelectorAll('.goal-card');
    goalCards.forEach(card => {
        card.addEventListener('click', (e) => {
            // Don't trigger if clicking on the button
            if (e.target.closest('.btn-add-contribution')) return;

            const goalId = card.dataset.goalId;
            console.log(`View goal ${goalId} details`);
        });
    });
}

// function globally available here:
window.renderGoals = renderGoals;

