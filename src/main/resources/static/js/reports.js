
function renderReportsSection() {
  const section = document.getElementById('section-reports');
  if (!section) return;

  const pageContent = section.querySelector('.section-content');
  if (!pageContent) return;

  pageContent.innerHTML = `
    <div class="reports-container">
      <!-- Header -->
      <div class="reports-header">
        <div>
          <h1 class="reports-title">Financial Analytics</h1>
          <p class="reports-subtitle">Comprehensive overview of your financial trends</p>
        </div>
        <div class="reports-controls">
          <select id="reportsTimeRange" class="reports-select">
            <option value="6months">Last 6 Months</option>
            <option value="12months">Last 12 Months</option>
            <option value="ytd">Year to Date</option>
            <option value="custom">Custom Range</option>
          </select>
          <button class="reports-export-btn" id="exportReportBtn">
            <span>📊</span> Export PDF
          </button>
        </div>
      </div>

      <!-- Top Row Charts -->
      <div class="reports-grid-top">
        <!-- Income vs Expenses Chart -->
        <div class="report-card">
          <div class="report-card-header">
            <div>
              <h3 class="report-card-title">Income vs Expenses</h3>
              <p class="report-card-subtitle">Monthly comparison</p>
            </div>
          </div>
          <div class="report-chart-container">
            <canvas id="incomeVsExpensesChart"></canvas>
          </div>
        </div>

        <!-- Savings Trend Chart -->
        <div class="report-card">
          <div class="report-card-header">
            <div>
              <h3 class="report-card-title">Savings Trend</h3>
              <p class="report-card-subtitle">Monthly savings over time</p>
            </div>
          </div>
          <div class="report-chart-container">
            <canvas id="savingsTrendChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Category Spending Trends -->
      <div class="report-card report-card-full">
        <div class="report-card-header">
          <div>
            <h3 class="report-card-title">Category Spending Trends</h3>
            <p class="report-card-subtitle">How your spending categories change over time</p>
          </div>
          <div class="category-legend" id="categoryLegend"></div>
        </div>
        <div class="report-chart-container report-chart-large">
          <canvas id="categoryTrendsChart"></canvas>
        </div>
      </div>

      <!-- Bottom Stats Row -->
      <div class="reports-stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);">💰</div>
          <div class="stat-content">
            <div class="stat-label">Total Saved</div>
            <div class="stat-value" id="totalSaved">$0</div>
            <div class="stat-change positive">+12.5% from last period</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);">📈</div>
          <div class="stat-content">
            <div class="stat-label">Avg Monthly Income</div>
            <div class="stat-value" id="avgIncome">$0</div>
            <div class="stat-change positive">+8.2% from last period</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">💳</div>
          <div class="stat-content">
            <div class="stat-label">Avg Monthly Expenses</div>
            <div class="stat-value" id="avgExpenses">$0</div>
            <div class="stat-change negative">+5.3% from last period</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);">🎯</div>
          <div class="stat-content">
            <div class="stat-label">Savings Rate</div>
            <div class="stat-value" id="savingsRate">0%</div>
            <div class="stat-change positive">+3.1% from last period</div>
          </div>
        </div>
      </div>
    </div>
  `;

  // Initialize charts
  initializeReportsCharts();
  loadReportsData();

  // Event listeners
  document.getElementById('reportsTimeRange')?.addEventListener('change', loadReportsData);
  document.getElementById('exportReportBtn')?.addEventListener('click', exportReportAsPDF);
}

let incomeExpensesChartInstance = null;
let savingsTrendChartInstance = null;
let categoryTrendsChartInstance = null;

function initializeReportsCharts() {
  // Destroy existing charts if they exist
  if (incomeExpensesChartInstance) incomeExpensesChartInstance.destroy();
  if (savingsTrendChartInstance) savingsTrendChartInstance.destroy();
  if (categoryTrendsChartInstance) categoryTrendsChartInstance.destroy();

  // Income vs Expenses Chart (Bar Chart)
  const incomeExpensesCtx = document.getElementById('incomeVsExpensesChart');
  if (incomeExpensesCtx) {
    incomeExpensesChartInstance = new Chart(incomeExpensesCtx, {
      type: 'bar',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [
          {
            label: 'Income',
            data: [4200, 4500, 4300, 4700, 4500, 4600],
            backgroundColor: 'rgba(34, 197, 94, 0.8)',
            borderColor: 'rgba(34, 197, 94, 1)',
            borderWidth: 0,
            borderRadius: 6,
            barThickness: 40
          },
          {
            label: 'Expenses',
            data: [3100, 3000, 3300, 3100, 3400, 3200],
            backgroundColor: 'rgba(236, 72, 153, 0.8)',
            borderColor: 'rgba(236, 72, 153, 1)',
            borderWidth: 0,
            borderRadius: 6,
            barThickness: 40
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              color: '#94a3b8',
              usePointStyle: true,
              padding: 15,
              font: { size: 12 }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.95)',
            padding: 12,
            titleColor: '#f1f5f9',
            bodyColor: '#cbd5e1',
            borderColor: 'rgba(148, 163, 184, 0.2)',
            borderWidth: 1
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(148, 163, 184, 0.1)',
              drawBorder: false
            },
            ticks: {
              color: '#64748b',
              callback: function(value) {
                return '$' + (value / 1000) + 'k';
              }
            }
          },
          x: {
            grid: { display: false },
            ticks: { color: '#64748b' }
          }
        }
      }
    });
  }

  // Savings Trend Chart (Line Chart)
  const savingsTrendCtx = document.getElementById('savingsTrendChart');
  if (savingsTrendCtx) {
    savingsTrendChartInstance = new Chart(savingsTrendCtx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
          label: 'Savings',
          data: [1200, 1900, 1100, 1650, 2100, 1350],
          borderColor: 'rgba(34, 197, 94, 1)',
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: 'rgba(34, 197, 94, 1)',
          pointBorderColor: '#1e293b',
          pointBorderWidth: 2,
          pointHoverRadius: 7
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.95)',
            padding: 12,
            titleColor: '#f1f5f9',
            bodyColor: '#cbd5e1',
            borderColor: 'rgba(148, 163, 184, 0.2)',
            borderWidth: 1
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(148, 163, 184, 0.1)',
              drawBorder: false
            },
            ticks: {
              color: '#64748b',
              callback: function(value) {
                return '$' + (value / 1000) + 'k';
              }
            }
          },
          x: {
            grid: { display: false },
            ticks: { color: '#64748b' }
          }
        }
      }
    });
  }


  const categoryTrendsCtx = document.getElementById('categoryTrendsChart');
  if (categoryTrendsCtx) {
    categoryTrendsChartInstance = new Chart(categoryTrendsCtx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [
          {
            label: 'Housing',
            data: [1200, 1200, 1200, 1200, 1200, 1200],
            borderColor: '#06b6d4',
            backgroundColor: 'rgba(6, 182, 212, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#06b6d4',
            pointBorderColor: '#1e293b',
            pointBorderWidth: 2
          },
          {
            label: 'Food',
            data: [300, 350, 400, 450, 520, 480],
            borderColor: '#22c55e',
            backgroundColor: 'rgba(34, 197, 94, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#22c55e',
            pointBorderColor: '#1e293b',
            pointBorderWidth: 2
          },
          {
            label: 'Transport',
            data: [300, 280, 350, 320, 340, 330],
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#f59e0b',
            pointBorderColor: '#1e293b',
            pointBorderWidth: 2
          },
          {
            label: 'Entertainment',
            data: [120, 150, 200, 180, 220, 190],
            borderColor: '#a855f7',
            backgroundColor: 'rgba(168, 85, 247, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#a855f7',
            pointBorderColor: '#1e293b',
            pointBorderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.95)',
            padding: 12,
            titleColor: '#f1f5f9',
            bodyColor: '#cbd5e1',
            borderColor: 'rgba(148, 163, 184, 0.2)',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                return context.dataset.label + ': $' + context.parsed.y;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(148, 163, 184, 0.1)',
              drawBorder: false
            },
            ticks: {
              color: '#64748b',
              callback: function(value) {
                return '$' + value;
              }
            }
          },
          x: {
            grid: { display: false },
            ticks: { color: '#64748b' }
          }
        }
      }
    });

    // Create custom legend
    createCategoryLegend();
  }
}

function createCategoryLegend() {
  const legendContainer = document.getElementById('categoryLegend');
  if (!legendContainer) return;

  const categories = [
    { name: 'Housing', color: '#06b6d4' },
    { name: 'Food', color: '#22c55e' },
    { name: 'Transport', color: '#f59e0b' },
    { name: 'Entertainment', color: '#a855f7' }
  ];

  legendContainer.innerHTML = categories.map(cat => `
    <div class="legend-item">
      <span class="legend-dot" style="background-color: ${cat.color};"></span>
      <span class="legend-label">${cat.name}</span>
    </div>
  `).join('');
}

function loadReportsData() {

  const timeRange = document.getElementById('reportsTimeRange')?.value || '6months';

  // Update stats
  document.getElementById('totalSaved').textContent = '$12,450';
  document.getElementById('avgIncome').textContent = '$4,467';
  document.getElementById('avgExpenses').textContent = '$3,183';
  document.getElementById('savingsRate').textContent = '28.7%';


}



if (typeof window.initReports === 'undefined') {
  window.initReports = renderReportsSection;
}

