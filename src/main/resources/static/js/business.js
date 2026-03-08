function renderBusiness() {
    const section = document.getElementById('section-business');
    if (!section) return;
    const contentDiv = section.querySelector('.section-content');
    if (!contentDiv) return;
    contentDiv.innerHTML = '<div style="text-align: center; padding: 40px; color: #64748b;">Loading business data...</div>';
    fetchBusinessAndRender(contentDiv);
}

function fetchBusinessAndRender(contentDiv) {
    // Demo data
    const statsData = {
        totalRevenue: { amount: '$48,250', label: 'Total Revenue', change: '+12.5%', positive: true },
        totalExpenses: { amount: '$31,840', label: 'Total Expenses', change: '+4.2%', positive: false },
        activeClients: { amount: '12', label: 'Active Clients', change: '+2', positive: true },
        activeTags: { amount: '8', label: 'Active Tags', change: '+1', positive: true }
    };

    const expenseTags = [
        { name: 'Client A', count: 24, color: '#0ea5e9' },
        { name: 'Project X', count: 18, color: '#a855f7' },
        { name: 'Q1 2024', count: 31, color: '#f59e0b' },
        { name: 'Marketing', count: 12, color: '#ec4899' },
        { name: 'Operations', count: 45, color: '#10b981' },
        { name: 'Vendor B', count: 8, color: '#06b6d4' },
        { name: 'Recurring', count: 56, color: '#10b981' },
        { name: 'Tax Deductible', count: 33, color: '#eab308' }
    ];

    const spendingByTag = [
        { name: 'Operations', amount: 14000, color: '#10b981' },
        { name: 'Client A', amount: 12000, color: '#0ea5e9' },
        { name: 'Marketing', amount: 9500, color: '#ec4899' },
        { name: 'Project X', amount: 8000, color: '#a855f7' },
        { name: 'Recurring', amount: 6500, color: '#f59e0b' },
        { name: 'Vendor B', amount: 4000, color: '#06b6d4' }
    ];

    const categoryData = [
        { name: 'Office Supplies', amount: '$2,340', change: '+2%', tags: ['Operations', 'Recurring'], tagColors: ['#10b981', '#10b981'] },
        { name: 'Travel', amount: '$4,820', change: '+12%', tags: ['Client A', 'Marketing'], tagColors: ['#0ea5e9', '#ec4899'] },
        { name: 'Software', amount: '$1,890', change: '+2%', tags: ['Operations', 'Recurring'], tagColors: ['#10b981', '#10b981'] },
        { name: 'Consulting', amount: '$6,500', change: '+18%', tags: ['Project X', 'Client A'], tagColors: ['#a855f7', '#0ea5e9'] },
        { name: 'Advertising', amount: '$3,200', change: '-8%', tags: ['Marketing'], tagColors: ['#ec4899'] },
        { name: 'Utilities', amount: '$1,100', change: '+1%', tags: ['Operations', 'Recurring'], tagColors: ['#10b981', '#10b981'] }
    ];

    const recentExpenses = [
        { id: 1, name: 'Cloud Infrastructure', icon: '💻', iconColor: '#3b82f6', category: 'Software', tags: ['Operations', 'Recurring'], tagColors: ['#10b981', '#10b981'], amount: '-$2,840' },
        { id: 2, name: 'Client Dinner - NYC', icon: '🍽️', iconColor: '#a855f7', category: 'Travel', tags: ['Client A'], tagColors: ['#0ea5e9'], amount: '-$1,250' },
        { id: 3, name: 'Facebook Ads', icon: '📢', iconColor: '#ec4899', category: 'Advertising', tags: ['Marketing'], tagColors: ['#ec4899'], amount: '-$3,200' },
        { id: 4, name: 'Legal Consultation', icon: '⚖️', iconColor: '#f59e0b', category: 'Consulting', tags: ['Project X'], tagColors: ['#a855f7'], amount: '-$4,500' },
        { id: 5, name: 'Equipment Repair', icon: '🔧', iconColor: '#10b981', category: 'Maintenance', tags: ['Operations'], tagColors: ['#10b981'], amount: '-$890' },
        { id: 6, name: 'Power & Internet', icon: '⚡', iconColor: '#06b6d4', category: 'Utilities', tags: ['Operations', 'Recurring'], tagColors: ['#10b981', '#10b981'], amount: '-$540' },
        { id: 7, name: 'Office Furniture', icon: '🪑', iconColor: '#8b5cf6', category: 'Office Supplies', tags: ['Operations'], tagColors: ['#10b981'], amount: '-$1,450' },
        { id: 8, name: 'Team Lunch', icon: '🍕', iconColor: '#f59e0b', category: 'Travel', tags: ['Client A', 'Marketing'], tagColors: ['#0ea5e9', '#ec4899'], amount: '-$320' }
    ];

    renderBusinessContent(contentDiv, { stats: statsData, expenseTags, spendingByTag, categoryData, recentExpenses });
}

function renderBusinessContent(contentDiv, data) {
    const { stats, expenseTags, spendingByTag, categoryData, recentExpenses } = data;

    const maxSpending = Math.max(...spendingByTag.map(t => t.amount));

    const html = `
        <div class="business-page">
            <!-- Stats Cards Row -->
            <div class="business-stats-row">
                <div class="business-stat-card">
                    <div class="stat-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="12" y1="1" x2="12" y2="23"></line>
                            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                        </svg>
                    </div>
                    <div class="stat-content">
                        <div class="stat-value">${stats.totalRevenue.amount}</div>
                        <div class="stat-label">${stats.totalRevenue.label}</div>
                    </div>
                    <div class="stat-change positive">${stats.totalRevenue.change}</div>
                </div>

                <div class="business-stat-card">
                    <div class="stat-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                        </svg>
                    </div>
                    <div class="stat-content">
                        <div class="stat-value">${stats.totalExpenses.amount}</div>
                        <div class="stat-label">${stats.totalExpenses.label}</div>
                    </div>
                    <div class="stat-change negative">${stats.totalExpenses.change}</div>
                </div>

                <div class="business-stat-card">
                    <div class="stat-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                    </div>
                    <div class="stat-content">
                        <div class="stat-value">${stats.activeClients.amount}</div>
                        <div class="stat-label">${stats.activeClients.label}</div>
                    </div>
                    <div class="stat-change positive">${stats.activeClients.change}</div>
                </div>

                <div class="business-stat-card">
                    <div class="stat-icon" style="background: rgba(168, 85, 247, 0.1); color: #a855f7;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
                            <line x1="7" y1="7" x2="7.01" y2="7"></line>
                        </svg>
                    </div>
                    <div class="stat-content">
                        <div class="stat-value">${stats.activeTags.amount}</div>
                        <div class="stat-label">${stats.activeTags.label}</div>
                    </div>
                    <div class="stat-change positive">${stats.activeTags.change}</div>
                </div>
            </div>

            <!-- Main Content Grid -->
            <div class="business-main-grid">
                <!-- Left Column -->
                <div class="business-left-col">
                    <!-- Expense Tags Section -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <div class="card-header-left">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
                                    <line x1="7" y1="7" x2="7.01" y2="7"></line>
                                </svg>
                                <h3>Expense Tags</h3>
                            </div>
                            <button class="btn-new-tag">+ New Tag</button>
                        </div>
                        <div class="expense-tags-list">
                            ${expenseTags.map(tag => `
                                <span class="expense-tag-badge" style="background: ${tag.color}20; color: ${tag.color}; border-color: ${tag.color}40;">
                                    ${tag.name} <span class="tag-count">${tag.count}</span>
                                </span>
                            `).join('')}
                        </div>
                    </div>

                    <!-- Monthly Tag Report -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <div class="card-header-left">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M9 2v4M15 2v4M3 8h18M5 4h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z"></path>
                                </svg>
                                <div>
                                    <h3>Monthly Tag Report</h3>
                                    <p class="card-subtitle">Expenses by tag over time</p>
                                </div>
                            </div>
                            <button class="btn-export">Export</button>
                        </div>
                        <div class="chart-container">
                            <canvas id="monthlyTagChart"></canvas>
                        </div>
                        <div class="chart-legend">
                            <div class="legend-item"><span class="legend-dot" style="background: #0ea5e9;"></span> Client A</div>
                            <div class="legend-item"><span class="legend-dot" style="background: #10b981;"></span> Operations</div>
                            <div class="legend-item"><span class="legend-dot" style="background: #ec4899;"></span> Marketing</div>
                        </div>
                    </div>

                    <!-- Category × Tag Matrix -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <div class="card-header-left">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <rect x="3" y="3" width="7" height="7"></rect>
                                    <rect x="14" y="3" width="7" height="7"></rect>
                                    <rect x="14" y="14" width="7" height="7"></rect>
                                    <rect x="3" y="14" width="7" height="7"></rect>
                                </svg>
                                <div>
                                    <h3>Category × Tag Matrix</h3>
                                    <p class="card-subtitle">Multi-purpose category view</p>
                                </div>
                            </div>
                        </div>
                        <div class="category-matrix-list">
                            ${categoryData.map(cat => `
                                <div class="category-matrix-row">
                                    <div class="category-info">
                                        <div class="category-name">${cat.name}</div>
                                        <div class="category-tags">
                                            ${cat.tags.map((tag, i) => `
                                                <span class="mini-tag" style="background: ${cat.tagColors[i]}; color: white;">${tag}</span>
                                            `).join('')}
                                        </div>
                                    </div>
                                    <div class="category-amount-section">
                                        <div class="category-amount">${cat.amount}</div>
                                        <div class="category-change ${cat.change.startsWith('-') ? 'negative' : 'positive'}">${cat.change}</div>
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    </div>

                    <!-- Recent Tagged Expenses -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <h3>Recent Tagged Expenses</h3>
                            <a href="#" class="view-all-link">View All →</a>
                        </div>
                        <div class="recent-expenses-grid">
                            ${recentExpenses.map(exp => `
                                <div class="recent-expense-item" style="--item-color: ${exp.iconColor}">
                                    <div class="expense-item-header">
                                        <div class="expense-icon-box" style="background: ${exp.iconColor}20; color: ${exp.iconColor};">
                                            ${exp.icon}
                                        </div>
                                        <div class="expense-info">
                                            <div class="expense-name">${exp.name}</div>
                                            <div class="expense-meta">
                                                ${exp.category} • ${exp.tags.map((tag, i) => `<span style="color: ${exp.tagColors[i]}">${tag}</span>`).join(' • ')}
                                            </div>
                                        </div>
                                    </div>
                                    <div class="expense-amount">${exp.amount}</div>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>

                <!-- Right Column -->
                <div class="business-right-col">
                    <!-- Spending by Tag -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <div class="card-header-left">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <path d="M12 6v6l4 2"></path>
                                </svg>
                                <div>
                                    <h3>Spending by Tag</h3>
                                    <p class="card-subtitle">Monthly tag breakdown</p>
                                </div>
                            </div>
                        </div>
                        <div class="spending-bars">
                            ${spendingByTag.map(tag => `
                                <div class="spending-bar-row">
                                    <div class="bar-label">${tag.name}</div>
                                    <div class="bar-track">
                                        <div class="bar-fill" style="width: ${(tag.amount / maxSpending) * 100}%; background: ${tag.color};"></div>
                                    </div>
                                    <div class="bar-values">
                                        <span class="bar-amount">$${(tag.amount / 1000).toFixed(1)}k</span>
                                    </div>
                                </div>
                            `).join('')}
                            <div class="bar-axis">
                                <span>$0k</span>
                                <span>$4k</span>
                                <span>$7k</span>
                                <span>$11k</span>
                                <span>$14k</span>
                            </div>
                        </div>
                    </div>

                    <!-- Income & Expenses Chart -->
                    <div class="business-card">
                        <div class="business-card-header">
                            <h3>Income & Expenses</h3>
                            <div class="chart-stats">
                                <div class="chart-stat">
                                    <span class="stat-label-small">Max. Expenses</span>
                                    <span class="stat-value-small" style="color: #ef4444;">$18,853</span>
                                </div>
                                <div class="chart-stat">
                                    <span class="stat-label-small">Max. Income</span>
                                    <span class="stat-value-small" style="color: #10b981;">$23,240</span>
                                </div>
                            </div>
                        </div>
                        <div class="chart-container">
                            <canvas id="incomeExpensesChart"></canvas>
                        </div>
                    </div>

                    <!-- Notification -->
                    <div class="notification-card">
                        <div class="notification-icon">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                        </div>
                        <div class="notification-content">
                            <div class="notification-title">Notification</div>
                            <div class="notification-message">3 Bills are past Due. Pay soon to avoid late fees.</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    contentDiv.innerHTML = html;

    // Initialize charts after DOM is updated
    setTimeout(() => {
        initMonthlyTagChart();
        initIncomeExpensesChart();
    }, 100);
}

function initMonthlyTagChart() {
    const canvas = document.getElementById('monthlyTagChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    // Simple area chart simulation
    canvas.width = canvas.offsetWidth;
    canvas.height = 200;

    // Draw gradient area chart
    const gradient1 = ctx.createLinearGradient(0, 0, 0, 200);
    gradient1.addColorStop(0, 'rgba(14, 165, 233, 0.3)');
    gradient1.addColorStop(1, 'rgba(14, 165, 233, 0.0)');

    const gradient2 = ctx.createLinearGradient(0, 0, 0, 200);
    gradient2.addColorStop(0, 'rgba(16, 185, 129, 0.3)');
    gradient2.addColorStop(1, 'rgba(16, 185, 129, 0.0)');

    const gradient3 = ctx.createLinearGradient(0, 0, 0, 200);
    gradient3.addColorStop(0, 'rgba(236, 72, 153, 0.3)');
    gradient3.addColorStop(1, 'rgba(236, 72, 153, 0.0)');

    // Draw three area curves
    drawAreaCurve(ctx, gradient1, '#0ea5e9', 0);
    drawAreaCurve(ctx, gradient2, '#10b981', 30);
    drawAreaCurve(ctx, gradient3, '#ec4899', 60);
}

function drawAreaCurve(ctx, gradient, strokeColor, offset) {
    const width = ctx.canvas.width;
    const height = ctx.canvas.height;
    const points = 20;

    ctx.beginPath();
    ctx.moveTo(0, height);

    for (let i = 0; i <= points; i++) {
        const x = (width / points) * i;
        const y = height - (Math.sin(i * 0.5) * 30 + Math.random() * 20 + offset + 80);
        if (i === 0) {
            ctx.lineTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    }

    ctx.lineTo(width, height);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    // Draw line on top
    ctx.beginPath();
    for (let i = 0; i <= points; i++) {
        const x = (width / points) * i;
        const y = height - (Math.sin(i * 0.5) * 30 + Math.random() * 20 + offset + 80);
        if (i === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    }
    ctx.strokeStyle = strokeColor;
    ctx.lineWidth = 2;
    ctx.stroke();
}

function initIncomeExpensesChart() {
    const canvas = document.getElementById('incomeExpensesChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    canvas.width = canvas.offsetWidth;
    canvas.height = 200;

    const width = canvas.width;
    const height = canvas.height;
    const months = 12;

    // Draw two lines
    ctx.beginPath();
    ctx.moveTo(0, height - 100);
    for (let i = 0; i <= months; i++) {
        const x = (width / months) * i;
        const y = height - (80 + Math.sin(i * 0.8) * 30 + i * 5);
        ctx.lineTo(x, y);
    }
    ctx.strokeStyle = '#0ea5e9';
    ctx.lineWidth = 3;
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(0, height - 80);
    for (let i = 0; i <= months; i++) {
        const x = (width / months) * i;
        const y = height - (60 + Math.sin(i * 0.6) * 20 + i * 6);
        ctx.lineTo(x, y);
    }
    ctx.strokeStyle = '#ec4899';
    ctx.lineWidth = 3;
    ctx.stroke();
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

