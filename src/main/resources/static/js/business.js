function renderBusiness() {
    const section = document.getElementById('section-business');
    if (!section) return;
    const contentDiv = section.querySelector('.section-content');
    if (!contentDiv) return;
    contentDiv.innerHTML = '<div style="text-align: center; padding: 40px; color: #64748b;">Loading business data...</div>';
    fetchBusinessAndRender(contentDiv);
}

function fetchBusinessAndRender(contentDiv) {
    // Fetch real data from API
    fetch('/api/business/analytics')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch business analytics');
            }
            return response.json();
        })
        .then(data => {

            const transformedData = transformAnalyticsData(data);
            renderBusinessContent(contentDiv, transformedData);
        })
        .catch(error => {
            console.error('Error fetching business analytics:', error);
            contentDiv.innerHTML = `
                <div style="text-align: center; padding: 40px; color: #ef4444;">
                    <p>Error loading business data</p>
                    <p style="font-size: 14px; color: #64748b;">${error.message}</p>
                </div>
            `;
        });
}


function transformAnalyticsData(apiData) {

    const stats = apiData.stats || {};
    const statsData = {
        totalRevenue: {
            amount: '$0.00',
            label: 'Total Revenue',
            change: '+0%',
            positive: true
        },
        totalExpenses: {
            amount: stats.totalExpenses || '$0.00',
            label: 'Total Expenses',
            change: '+0%',
            positive: false
        },
        activeClients: {
            amount: '0',
            label: 'Active Clients',
            change: '+0',
            positive: true
        },
        activeTags: {
            amount: String(stats.activeTags || 0),
            label: 'Active Tags',
            change: '+0',
            positive: true
        }
    };

    // Transform expense tags
    const expenseTags = (apiData.expenseTags || []).map(tag => ({
        name: tag.name,
        count: tag.count,
        color: tag.color || '#3b82f6'
    }));

    // Transform spending by tag
    const spendingByTag = (apiData.spendingByTag || []).map(tag => ({
        name: tag.name,
        amount: tag.amount,
        color: tag.color || '#3b82f6'
    }));

    // Transform category data - add placeholder change percentages
    const categoryData = (apiData.categoryData || []).map(category => ({
        name: category.name,
        amount: category.amount,
        change: '+0%', // Placeholder for now
        tags: category.tags || [],
        tagColors: category.tagColors || []
    }));

    // Transform recent expenses
    const recentExpenses = (apiData.recentExpenses || []).map(expense => ({
        id: expense.id,
        name: expense.name,
        icon: expense.icon,
        iconColor: expense.iconColor,
        category: expense.category,
        tags: expense.tags || [],
        tagColors: expense.tagColors || [],
        amount: expense.amount
    }));

    return {
        stats: statsData,
        expenseTags: expenseTags,
        spendingByTag: spendingByTag,
        categoryData: categoryData,
        recentExpenses: recentExpenses
    };
}

function renderBusinessContent(contentDiv, data) {
    const { stats, expenseTags, spendingByTag, categoryData, recentExpenses } = data;

    const maxSpending = Math.max(...spendingByTag.map(t => t.amount));

    const html = `
        <div class="business-wrap">
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
                        <div class="business-chart" id="monthlyTagChart">
                            <div class="business-chart-empty">Loading chart...</div>
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
                        <div class="business-chart" id="incomeExpensesChart">
                            <div class="business-chart-empty">Loading chart...</div>
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

    // Trigger business cards animations on load/render
    const businessWrap = contentDiv.querySelector('.business-wrap');
    if (businessWrap) {
        businessWrap.classList.remove('business-animate');
        // Wait a frame so the browser paints the initial state, then add the class to start animations
        requestAnimationFrame(function () {
            businessWrap.classList.add('business-animate');
        });
    }

    // Initialize charts after DOM is updated
    setTimeout(() => {
        initMonthlyTagChart();
        initIncomeExpensesChart();
    }, 100);
}

function initMonthlyTagChart() {
    const chartHost = document.getElementById('monthlyTagChart');
    if (!chartHost) return;


    const data = [
        { month: 'Oct', clientA: 2800, operations: 3200, marketing: 2100 },
        { month: 'Nov', clientA: 3400, operations: 3600, marketing: 2500 },
        { month: 'Dec', clientA: 2200, operations: 2800, marketing: 1800 },
        { month: 'Jan', clientA: 4100, operations: 4200, marketing: 3200 },
        { month: 'Feb', clientA: 3600, operations: 3900, marketing: 2800 },
        { month: 'Mar', clientA: 4500, operations: 4600, marketing: 3600 }
    ];

    const w = 760;
    const h = 260;
    const padT = 28;
    const padB = 24;
    const padL = 40;
    const padR = 20;

    function x(i) {
        if (data.length === 1) return padL;
        const innerW = w - padL - padR;
        return padL + (innerW * (i / (data.length - 1)));
    }

    const clientAValues = data.map(p => Number(p.clientA) || 0);
    const operationsValues = data.map(p => Number(p.operations) || 0);
    const marketingValues = data.map(p => Number(p.marketing) || 0);

    const rawMax = Math.max(1, ...clientAValues, ...operationsValues, ...marketingValues);

    // max with 4 intervals
    let chartMax = rawMax;
    if (rawMax >= 1000) {
        const step = 1500;
        chartMax = Math.ceil(rawMax / step) * step;
        if (chartMax < step * 4) chartMax = step * 4;
    }

    function y(v) {
        const innerH = h - padT - padB;
        const t = (Number(v) || 0) / chartMax;
        return (h - padB) - (innerH * t);
    }


    function smoothPath(values) {
        const pts = values.map(function(v, i) {
            return { x: x(i), y: y(v) };
        });

        if (pts.length === 1) {
            return 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
        }

        let d = 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
        for (let i = 1; i < pts.length; i++) {
            const prev = pts[i - 1];
            const curr = pts[i];
            const midX = (prev.x + curr.x) / 2;
            d += ' C ' + midX.toFixed(1) + ' ' + prev.y.toFixed(1) + ', ' + midX.toFixed(1) + ' ' + curr.y.toFixed(1) + ', ' + curr.x.toFixed(1) + ' ' + curr.y.toFixed(1);
        }
        return d;
    }

    function areaPath(values) {
        const line = smoothPath(values);
        const baseY = h - padB;
        const firstX = x(0);
        const lastX = x(values.length - 1);
        return line + ' L ' + lastX.toFixed(1) + ' ' + baseY.toFixed(1) + ' L ' + firstX.toFixed(1) + ' ' + baseY.toFixed(1) + ' Z';
    }

    const clientAD = smoothPath(clientAValues);
    const operationsD = smoothPath(operationsValues);
    const marketingD = smoothPath(marketingValues);
    const clientAAreaD = areaPath(clientAValues);
    const operationsAreaD = areaPath(operationsValues);
    const marketingAreaD = areaPath(marketingValues);

    const labels = data.map(function(p, i) {
        const lbl = p.month || '';
        return `<text class="chart-label" x="${x(i).toFixed(1)}" y="${(h - 8)}" text-anchor="middle">${lbl}</text>`;
    }).join('');

    const ticks = 4;
    const yLines = [];
    const yLabels = [];
    for (let i = 0; i <= ticks; i++) {
        const t = i / ticks;
        const value = chartMax * (1 - t);
        const yy = padT + (h - padT - padB) * t;

        yLines.push(`<line class="chart-grid" x1="${padL}" y1="${yy.toFixed(1)}" x2="${(w - padR)}" y2="${yy.toFixed(1)}" />`);

        const k = value / 1000;
        const label = '$' + (k === 0 ? '0' : (k % 1 === 0 ? k.toFixed(0) : k.toFixed(1))) + 'k';
        yLabels.push(`<text class="chart-ylabel" x="${(padL - 14)}" y="${(yy + 4).toFixed(1)}" text-anchor="end">${label}</text>`);
    }


    const xLines = data.map(function(p, i) {
        const xx = x(i);
        return `<line class="chart-grid" x1="${xx.toFixed(1)}" y1="${padT}" x2="${xx.toFixed(1)}" y2="${(h - padB)}" />`;
    }).join('');

    chartHost.innerHTML = `
      <svg viewBox="0 0 ${w} ${h}" width="100%" height="100%" aria-label="Monthly Tag Report chart">
        <defs>
          <linearGradient id="clientAFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(14, 165, 233, 0.22)" />
            <stop offset="100%" stop-color="rgba(14, 165, 233, 0)" />
          </linearGradient>
          <linearGradient id="operationsFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(16, 185, 129, 0.22)" />
            <stop offset="100%" stop-color="rgba(16, 185, 129, 0)" />
          </linearGradient>
          <linearGradient id="marketingFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(236, 72, 153, 0.22)" />
            <stop offset="100%" stop-color="rgba(236, 72, 153, 0)" />
          </linearGradient>
        </defs>

        <!-- grid: horizontal + vertical (dotted) -->
        <g>
          ${yLines.join('')}
          ${xLines}
        </g>

        <!-- area fills -->
        <path class="chart-fill-clientA paint-fill" d="${clientAAreaD}" fill="url(#clientAFill)" />
        <path class="chart-fill-operations paint-fill" d="${operationsAreaD}" fill="url(#operationsFill)" />
        <path class="chart-fill-marketing paint-fill" d="${marketingAreaD}" fill="url(#marketingFill)" />

        <!-- lines (paint animation) -->
        <path id="clientALine" class="chart-line-clientA paint-line" d="${clientAD}" />
        <path id="operationsLine" class="chart-line-operations paint-line" d="${operationsD}" />
        <path id="marketingLine" class="chart-line-marketing paint-line" d="${marketingD}" />

        <!-- labels -->
        ${labels}
        ${yLabels.join('')}

        <!-- legend -->
        <g font-size="12" fill="#64748b">
          <circle cx="${padL + 90}" cy="${h - 6}" r="6" fill="#0ea5e9"></circle>
          <text x="${padL + 102}" y="${h - 2}">Client A</text>
          <circle cx="${padL + 180}" cy="${h - 6}" r="6" fill="#10b981"></circle>
          <text x="${padL + 192}" y="${h - 2}">Operations</text>
          <circle cx="${padL + 290}" cy="${h - 6}" r="6" fill="#ec4899"></circle>
          <text x="${padL + 302}" y="${h - 2}">Marketing</text>
        </g>
      </svg>
    `;


    const clientAPathEl = chartHost.querySelector('#clientALine');
    const operationsPathEl = chartHost.querySelector('#operationsLine');
    const marketingPathEl = chartHost.querySelector('#marketingLine');

    if (clientAPathEl && clientAPathEl.getTotalLength) {
        const len = Math.ceil(clientAPathEl.getTotalLength());
        clientAPathEl.style.setProperty('--dash', String(len));
    }

    if (operationsPathEl && operationsPathEl.getTotalLength) {
        const len = Math.ceil(operationsPathEl.getTotalLength());
        operationsPathEl.style.setProperty('--dash', String(len));
        operationsPathEl.style.animationDelay = '80ms';
    }

    if (marketingPathEl && marketingPathEl.getTotalLength) {
        const len = Math.ceil(marketingPathEl.getTotalLength());
        marketingPathEl.style.setProperty('--dash', String(len));
        marketingPathEl.style.animationDelay = '160ms';
    }
}

function initIncomeExpensesChart() {
    const chartHost = document.getElementById('incomeExpensesChart');
    if (!chartHost) return;


    const data = [
        { month: 'Oct', income: 18500, expenses: 12200 },
        { month: 'Nov', income: 21000, expenses: 14500 },
        { month: 'Dec', income: 19200, expenses: 13800 },
        { month: 'Jan', income: 23240, expenses: 15600 },
        { month: 'Feb', income: 20800, expenses: 14200 },
        { month: 'Mar', income: 22500, expenses: 18853 }
    ];

    const w = 760;
    const h = 260;
    const padT = 28;
    const padB = 24;
    const padL = 40;
    const padR = 20;

    function x(i) {
        if (data.length === 1) return padL;
        const innerW = w - padL - padR;
        return padL + (innerW * (i / (data.length - 1)));
    }

    const incomeValues = data.map(p => Number(p.income) || 0);
    const expensesValues = data.map(p => Number(p.expenses) || 0);

    const rawMax = Math.max(1, ...incomeValues, ...expensesValues);

    // max with 4 intervals
    let chartMax = rawMax;
    if (rawMax >= 1000) {
        const step = 1500;
        chartMax = Math.ceil(rawMax / step) * step;
        if (chartMax < step * 4) chartMax = step * 4;
    }

    function y(v) {
        const innerH = h - padT - padB;
        const t = (Number(v) || 0) / chartMax;
        return (h - padB) - (innerH * t);
    }

    function smoothPath(values) {
        const pts = values.map(function(v, i) {
            return { x: x(i), y: y(v) };
        });

        if (pts.length === 1) {
            return 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
        }

        let d = 'M ' + pts[0].x.toFixed(1) + ' ' + pts[0].y.toFixed(1);
        for (let i = 1; i < pts.length; i++) {
            const prev = pts[i - 1];
            const curr = pts[i];
            const midX = (prev.x + curr.x) / 2;
            d += ' C ' + midX.toFixed(1) + ' ' + prev.y.toFixed(1) + ', ' + midX.toFixed(1) + ' ' + curr.y.toFixed(1) + ', ' + curr.x.toFixed(1) + ' ' + curr.y.toFixed(1);
        }
        return d;
    }

    function areaPath(values) {
        const line = smoothPath(values);
        const baseY = h - padB;
        const firstX = x(0);
        const lastX = x(values.length - 1);
        return line + ' L ' + lastX.toFixed(1) + ' ' + baseY.toFixed(1) + ' L ' + firstX.toFixed(1) + ' ' + baseY.toFixed(1) + ' Z';
    }

    const incomeD = smoothPath(incomeValues);
    const expensesD = smoothPath(expensesValues);
    const incomeAreaD = areaPath(incomeValues);
    const expensesAreaD = areaPath(expensesValues);

    const labels = data.map(function(p, i) {
        const lbl = p.month || '';
        return `<text class="chart-label" x="${x(i).toFixed(1)}" y="${(h - 8)}" text-anchor="middle">${lbl}</text>`;
    }).join('');

    const ticks = 4;
    const yLines = [];
    const yLabels = [];
    for (let i = 0; i <= ticks; i++) {
        const t = i / ticks;
        const value = chartMax * (1 - t);
        const yy = padT + (h - padT - padB) * t;

        yLines.push(`<line class="chart-grid" x1="${padL}" y1="${yy.toFixed(1)}" x2="${(w - padR)}" y2="${yy.toFixed(1)}" />`);

        const k = value / 1000;
        const label = '$' + (k === 0 ? '0' : (k % 1 === 0 ? k.toFixed(0) : k.toFixed(1))) + 'k';
        yLabels.push(`<text class="chart-ylabel" x="${(padL - 14)}" y="${(yy + 4).toFixed(1)}" text-anchor="end">${label}</text>`);
    }

    // vertical dotted grid for each month
    const xLines = data.map(function(p, i) {
        const xx = x(i);
        return `<line class="chart-grid" x1="${xx.toFixed(1)}" y1="${padT}" x2="${xx.toFixed(1)}" y2="${(h - padB)}" />`;
    }).join('');

    chartHost.innerHTML = `
      <svg viewBox="0 0 ${w} ${h}" width="100%" height="100%" aria-label="Income & Expenses chart">
        <defs>
          <linearGradient id="incomeBusinessFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(16, 185, 129, 0.22)" />
            <stop offset="100%" stop-color="rgba(16, 185, 129, 0)" />
          </linearGradient>
          <linearGradient id="expensesBusinessFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="rgba(239, 68, 68, 0.22)" />
            <stop offset="100%" stop-color="rgba(239, 68, 68, 0)" />
          </linearGradient>
        </defs>

        <!-- grid: horizontal + vertical (dotted) -->
        <g>
          ${yLines.join('')}
          ${xLines}
        </g>

        <!-- area fills -->
        <path class="chart-fill-income-business paint-fill" d="${incomeAreaD}" fill="url(#incomeBusinessFill)" />
        <path class="chart-fill-expenses-business paint-fill" d="${expensesAreaD}" fill="url(#expensesBusinessFill)" />

        <!-- lines (paint animation) -->
        <path id="incomeBusinessLine" class="chart-line-income-business paint-line" d="${incomeD}" />
        <path id="expensesBusinessLine" class="chart-line-expenses-business paint-line" d="${expensesD}" />

        <!-- labels -->
        ${labels}
        ${yLabels.join('')}

        <!-- legend -->
        <g font-size="12" fill="#64748b">
          <circle cx="${padL + 120}" cy="${h - 6}" r="6" fill="#10b981"></circle>
          <text x="${padL + 132}" y="${h - 2}">Income</text>
          <circle cx="${padL + 210}" cy="${h - 6}" r="6" fill="#ef4444"></circle>
          <text x="${padL + 222}" y="${h - 2}">Expenses</text>
        </g>
      </svg>
    `;


    const incomePathEl = chartHost.querySelector('#incomeBusinessLine');
    const expensesPathEl = chartHost.querySelector('#expensesBusinessLine');

    if (incomePathEl && incomePathEl.getTotalLength) {
        const len = Math.ceil(incomePathEl.getTotalLength());
        incomePathEl.style.setProperty('--dash', String(len));
    }

    if (expensesPathEl && expensesPathEl.getTotalLength) {
        const len = Math.ceil(expensesPathEl.getTotalLength());
        expensesPathEl.style.setProperty('--dash', String(len));
        expensesPathEl.style.animationDelay = '80ms';
    }
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

