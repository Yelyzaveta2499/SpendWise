function renderBusiness() {
    const section = document.getElementById('section-business');
    if (!section) return;
    const contentDiv = section.querySelector('.section-content');
    if (!contentDiv) return;
    contentDiv.innerHTML = '<div style="text-align: center; padding: 40px; color: #64748b;">Loading business data...</div>';
    fetchBusinessAndRender(contentDiv);
}

function fetchBusinessAndRender(contentDiv) {
    // Demo data for Category × Tag Matrix
    const categoryData = [
        {
            name: 'Office Supplies',
            amount: '$2,340',
            change: '+2%',
            tags: ['Operations', 'Recurring'],
            tagColors: ['#0ea5e9', '#10b981']
        },
        {
            name: 'Travel',
            amount: '$4,820',
            change: '+1%',
            tags: ['Client A', 'Marketing'],
            tagColors: ['#0ea5e9', '#ec4899']
        },
        {
            name: 'Software',
            amount: '$1,890',
            change: '+2%',
            tags: ['Operations', 'Recurring'],
            tagColors: ['#0ea5e9', '#10b981']
        },
        {
            name: 'Consulting',
            amount: '$6,500',
            change: '+12%',
            tags: ['Project X', 'Client A'],
            tagColors: ['#a855f7', '#0ea5e9']
        },
        {
            name: 'Advertising',
            amount: '$3,200',
            change: '-8%',
            tags: ['Marketing'],
            tagColors: ['#ec4899']
        },
        {
            name: 'Utilities',
            amount: '$1,100',
            change: '+1%',
            tags: ['Operations', 'Recurring'],
            tagColors: ['#0ea5e9', '#10b981']
        }
    ];

    // Demo data for Recent Tagged Expenses
    const recentExpenses = [
        {
            id: 1,
            name: 'Cloud Infrastructure',
            icon: '💻',
            iconBg: '#3b82f6',
            category: 'Software',
            tags: ['Operations', 'Recurring'],
            tagColors: ['#0ea5e9', '#10b981'],
            amount: '-$2,840'
        },
        {
            id: 2,
            name: 'Client Dinner - NYC',
            icon: '🍽️',
            iconBg: '#ec4899',
            category: 'Travel',
            tags: ['Client A'],
            tagColors: ['#0ea5e9'],
            amount: '-$1,250'
        },
        {
            id: 3,
            name: 'Facebook Ads',
            icon: '📢',
            iconBg: '#a855f7',
            category: 'Advertising',
            tags: ['Marketing'],
            tagColors: ['#ec4899'],
            amount: '-$3,200'
        },
        {
            id: 4,
            name: 'Legal Consultation',
            icon: '⚖️',
            iconBg: '#f59e0b',
            category: 'Consulting',
            tags: ['Project X'],
            tagColors: ['#a855f7'],
            amount: '-$4,500'
        },
        {
            id: 5,
            name: 'Equipment Repair',
            icon: '🔧',
            iconBg: '#10b981',
            category: 'Maintenance',
            tags: ['Operations'],
            tagColors: ['#0ea5e9'],
            amount: '-$890'
        },
        {
            id: 6,
            name: 'Power & Internet',
            icon: '⚡',
            iconBg: '#06b6d4',
            category: 'Utilities',
            tags: ['Operations', 'Recurring'],
            tagColors: ['#0ea5e9', '#10b981'],
            amount: '-$540'
        }
    ];

    renderBusinessContent(contentDiv, categoryData, recentExpenses);
}

function renderBusinessContent(contentDiv, categoryData, recentExpenses) {
    const html = `
        <div class="business-container">
            <!-- Category × Tag Matrix Section -->
            <div class="business-section">
                <div class="business-section-header">
                    <div class="business-section-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="3" width="7" height="7"></rect>
                            <rect x="14" y="3" width="7" height="7"></rect>
                            <rect x="14" y="14" width="7" height="7"></rect>
                            <rect x="3" y="14" width="7" height="7"></rect>
                        </svg>
                    </div>
                    <div>
                        <h3 class="business-section-title">Category × Tag Matrix</h3>
                        <p class="business-section-subtitle">Multi-purpose category view</p>
                    </div>
                </div>

                <div class="category-matrix">
                    ${categoryData.map(category => `
                        <div class="category-row">
                            <div class="category-name">${category.name}</div>
                            <div class="category-tags">
                                ${category.tags.map((tag, index) => `
                                    <span class="category-tag" style="background-color: ${category.tagColors[index]}20; color: ${category.tagColors[index]}; border-color: ${category.tagColors[index]}40;">
                                        ${tag}
                                    </span>
                                `).join('')}
                            </div>
                            <div class="category-amount">
                                <span class="amount-value">${category.amount}</span>
                                <span class="amount-change ${category.change.startsWith('+') ? 'positive' : 'negative'}">${category.change}</span>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>

            <!-- Recent Tagged Expenses Section -->
            <div class="business-section">
                <div class="business-section-header">
                    <h3 class="business-section-title">Recent Tagged Expenses</h3>
                    <a href="#" class="view-all-link">View All →</a>
                </div>

                <div class="recent-expenses-list">
                    ${recentExpenses.map(expense => `
                        <div class="expense-item">
                            <div class="expense-icon" style="background-color: ${expense.iconBg}20;">
                                <span style="font-size: 24px;">${expense.icon}</span>
                            </div>
                            <div class="expense-details">
                                <div class="expense-name">${expense.name}</div>
                                <div class="expense-meta">
                                    <span class="expense-category">${expense.category}</span>
                                    <span class="expense-separator">•</span>
                                    ${expense.tags.map((tag, index) => `
                                        <span class="expense-tag" style="color: ${expense.tagColors[index]};">${tag}</span>
                                    `).join('<span class="expense-separator">•</span>')}
                                </div>
                            </div>
                            <div class="expense-amount">${expense.amount}</div>
                        </div>
                    `).join('')}
                </div>
            </div>
        </div>
    `;

    contentDiv.innerHTML = html;
}

// Make renderBusiness globally available
window.renderBusiness = renderBusiness;

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        const businessSection = document.getElementById('section-business');
        if (businessSection) {
            // Observer will trigger when section becomes visible
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const contentDiv = entry.target.querySelector('.section-content');
                        if (contentDiv && (contentDiv.innerHTML.includes('Loading') || contentDiv.innerHTML.trim() === '')) {
                            renderBusiness();
                        }
                    }
                });
            }, { threshold: 0.1 });

            observer.observe(businessSection);
        }
    });
} else {
    // DOM already loaded
    const businessSection = document.getElementById('section-business');
    if (businessSection) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const contentDiv = entry.target.querySelector('.section-content');
                    if (contentDiv && (contentDiv.innerHTML.includes('Loading') || contentDiv.innerHTML.trim() === '')) {
                        renderBusiness();
                    }
                }
            });
        }, { threshold: 0.1 });

        observer.observe(businessSection);
    }
}

