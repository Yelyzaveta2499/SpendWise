
function renderReportsSection() {
  const section = document.getElementById('section-reports');
  if (!section) return;

  const pageContent = section.querySelector('.section-content');
  if (!pageContent) return;

  pageContent.innerHTML = `
    <div class="reports-wrap">
      <div class="reports-container">

        <!-- Dropdown -->
        <div class="reports-header-custom">
          <div class="reports-controls">
            <select id="reportsTimeRange" class="reports-select">
              <option value="6months">Last 6 Months</option>
              <option value="12months">Last 12 Months</option>
              <option value="ytd">Year to Date</option>
              <option value="custom">Custom Range</option>
            </select>
          </div>
        </div>

        <!-- Top Row Charts -->
        <div class="reports-grid-top">
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
            <div class="stat-content">
              <div class="stat-label">Total Saved</div>
              <div class="stat-value" id="totalSaved">$0</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-label">Avg Monthly Income</div>
              <div class="stat-value" id="avgIncome">$0</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-label">Avg Monthly Expenses</div>
              <div class="stat-value" id="avgExpenses">$0</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-label">Savings Rate</div>
              <div class="stat-value" id="savingsRate">0%</div>
            </div>
          </div>
        </div>

      </div>
    </div>
  `;

  loadReportsData();


  const reportsWrap = pageContent.querySelector('.reports-wrap');
  if (reportsWrap) {
    reportsWrap.classList.remove('reports-animate');
    requestAnimationFrame(function () {
      reportsWrap.classList.add('reports-animate');
    });
  }

  // Init charts after DOM is painted — 300ms delay
  setTimeout(function () {
    initializeReportsCharts();
    setupReportsResizeObserver();
  }, 300);

  // Event listeners
  document.getElementById('reportsTimeRange')?.addEventListener('change', loadReportsData);
  document.getElementById('exportReportBtn')?.addEventListener('click', exportReportAsPDF);
}

let incomeExpensesChartInstance = null;
let savingsTrendChartInstance = null;
let categoryTrendsChartInstance = null;
let reportsResizeObserver = null;

function setupReportsResizeObserver() {
  // Clean up any previous observer
  if (reportsResizeObserver) {
    reportsResizeObserver.disconnect();
    reportsResizeObserver = null;
  }

  const containers = document.querySelectorAll('.report-chart-container');
  if (!containers.length) return;

  reportsResizeObserver = new ResizeObserver(function () {
    if (incomeExpensesChartInstance) incomeExpensesChartInstance.resize();
    if (savingsTrendChartInstance)   savingsTrendChartInstance.resize();
    if (categoryTrendsChartInstance) categoryTrendsChartInstance.resize();
  });

  containers.forEach(function (el) {
    reportsResizeObserver.observe(el);
  });
}

function initializeReportsCharts() {
  // Destroy existing charts and observer if they exist
  if (reportsResizeObserver) { reportsResizeObserver.disconnect(); reportsResizeObserver = null; }
  if (incomeExpensesChartInstance) incomeExpensesChartInstance.destroy();
  if (savingsTrendChartInstance) savingsTrendChartInstance.destroy();
  if (categoryTrendsChartInstance) categoryTrendsChartInstance.destroy();

  // Income vs Expenses Chart (Bar Chart)
  const incomeExpensesCtx = document.getElementById('incomeVsExpensesChart');
  if (incomeExpensesCtx) {
    const ctx = incomeExpensesCtx.getContext('2d');


    const incomeGradient = ctx.createLinearGradient(0, 0, 0, 300);
    incomeGradient.addColorStop(0, 'rgba(34, 197, 94, 0.9)');
    incomeGradient.addColorStop(1, 'rgba(16, 185, 129, 0.7)');

    // Create gradient for Expenses bars
    const expensesGradient = ctx.createLinearGradient(0, 0, 0, 300);
    expensesGradient.addColorStop(0, 'rgba(236, 72, 153, 0.9)');
    expensesGradient.addColorStop(1, 'rgba(219, 39, 119, 0.7)');

    incomeExpensesChartInstance = new Chart(incomeExpensesCtx, {
      type: 'bar',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [
          {
            label: 'Income',
            data: [4200, 4500, 4300, 4700, 4500, 4600],
            backgroundColor: incomeGradient,
            borderColor: 'rgba(34, 197, 94, 1)',
            borderWidth: 0,
            borderRadius: 10,
            borderSkipped: false,
            barThickness: 36,
            shadowOffsetX: 0,
            shadowOffsetY: 4,
            shadowBlur: 8,
            shadowColor: 'rgba(34, 197, 94, 0.3)'
          },
          {
            label: 'Expenses',
            data: [3100, 3000, 3300, 3100, 3400, 3200],
            backgroundColor: expensesGradient,
            borderColor: 'rgba(236, 72, 153, 1)',
            borderWidth: 0,
            borderRadius: 10,
            borderSkipped: false,
            barThickness: 36,
            shadowOffsetX: 0,
            shadowOffsetY: 4,
            shadowBlur: 8,
            shadowColor: 'rgba(236, 72, 153, 0.3)'
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
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              color: '#cbd5e1',
              usePointStyle: true,
              pointStyle: 'rectRounded',
              padding: 20,
              font: {
                size: 13,
                weight: '600',
                family: "'Inter', sans-serif"
              }
            }
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(15, 23, 42, 0.98)',
            titleColor: '#f1f5f9',
            bodyColor: '#e2e8f0',
            borderColor: 'rgba(34, 197, 94, 0.3)',
            borderWidth: 1,
            padding: 16,
            displayColors: true,
            boxPadding: 6,
            cornerRadius: 12,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13
            },
            callbacks: {
              label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                  label += ': ';
                }
                label += '$' + context.parsed.y.toLocaleString();
                return label;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(148, 163, 184, 0.08)',
              drawBorder: false,
              lineWidth: 1
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '500'
              },
              padding: 8,
              callback: function(value) {
                return '$' + (value / 1000) + 'k';
              }
            }
          },
          x: {
            grid: {
              display: false,
              drawBorder: false
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '600'
              },
              padding: 8
            }
          }
        }
      }
    });
  }

  // Savings Trend Chart (Line Chart)
  const savingsTrendCtx = document.getElementById('savingsTrendChart');
  if (savingsTrendCtx) {
    const ctx = savingsTrendCtx.getContext('2d');

    // Create gradient for line
    const lineGradient = ctx.createLinearGradient(0, 0, 0, 300);
    lineGradient.addColorStop(0, 'rgba(34, 197, 94, 1)');
    lineGradient.addColorStop(1, 'rgba(16, 185, 129, 0.8)');

    // Create gradient for fill area
    const fillGradient = ctx.createLinearGradient(0, 0, 0, 300);
    fillGradient.addColorStop(0, 'rgba(34, 197, 94, 0.3)');
    fillGradient.addColorStop(0.5, 'rgba(34, 197, 94, 0.15)');
    fillGradient.addColorStop(1, 'rgba(34, 197, 94, 0.02)');

    savingsTrendChartInstance = new Chart(savingsTrendCtx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
          label: 'Savings',
          data: [1200, 1900, 1100, 1650, 2100, 1350],
          borderColor: lineGradient,
          backgroundColor: fillGradient,
          borderWidth: 3,
          fill: true,
          tension: 0.42,
          pointRadius: 6,
          pointBackgroundColor: '#22c55e',
          pointBorderColor: 'rgba(15, 23, 42, 0.9)',
          pointBorderWidth: 3,
          pointHoverRadius: 9,
          pointHoverBackgroundColor: '#22c55e',
          pointHoverBorderColor: '#ffffff',
          pointHoverBorderWidth: 3,
          segment: {
            borderColor: ctx => {
              // sparkle effect for rising trends
              return ctx.p0.parsed.y < ctx.p1.parsed.y ? 'rgba(34, 197, 94, 1)' : 'rgba(34, 197, 94, 0.8)';
            }
          }
        }]
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
            enabled: true,
            backgroundColor: 'rgba(15, 23, 42, 0.98)',
            titleColor: '#f1f5f9',
            bodyColor: '#e2e8f0',
            borderColor: 'rgba(34, 197, 94, 0.4)',
            borderWidth: 2,
            padding: 16,
            displayColors: false,
            cornerRadius: 12,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13,
              weight: '600'
            },
            callbacks: {
              label: function(context) {
                return 'Saved: $' + context.parsed.y.toLocaleString();
              },
              title: function(context) {
                return context[0].label;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(148, 163, 184, 0.08)',
              drawBorder: false,
              lineWidth: 1
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '500'
              },
              padding: 8,
              callback: function(value) {
                return '$' + (value / 1000) + 'k';
              }
            }
          },
          x: {
            grid: {
              display: false,
              drawBorder: false
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '600'
              },
              padding: 8
            }
          }
        }
      }
    });
  }


  // Category Spending Trends
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
            backgroundColor: 'rgba(6, 182, 212, 0.15)',
            borderWidth: 3,
            tension: 0.42,
            pointRadius: 5,
            pointBackgroundColor: '#06b6d4',
            pointBorderColor: 'rgba(15, 23, 42, 0.9)',
            pointBorderWidth: 2,
            pointHoverRadius: 8,
            pointHoverBackgroundColor: '#06b6d4',
            pointHoverBorderColor: '#ffffff',
            pointHoverBorderWidth: 3,
            fill: true
          },
          {
            label: 'Food',
            data: [300, 350, 400, 450, 520, 480],
            borderColor: '#22c55e',
            backgroundColor: 'rgba(34, 197, 94, 0.15)',
            borderWidth: 3,
            tension: 0.42,
            pointRadius: 5,
            pointBackgroundColor: '#22c55e',
            pointBorderColor: 'rgba(15, 23, 42, 0.9)',
            pointBorderWidth: 2,
            pointHoverRadius: 8,
            pointHoverBackgroundColor: '#22c55e',
            pointHoverBorderColor: '#ffffff',
            pointHoverBorderWidth: 3,
            fill: true
          },
          {
            label: 'Transport',
            data: [300, 280, 350, 320, 340, 330],
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245, 158, 11, 0.15)',
            borderWidth: 3,
            tension: 0.42,
            pointRadius: 5,
            pointBackgroundColor: '#f59e0b',
            pointBorderColor: 'rgba(15, 23, 42, 0.9)',
            pointBorderWidth: 2,
            pointHoverRadius: 8,
            pointHoverBackgroundColor: '#f59e0b',
            pointHoverBorderColor: '#ffffff',
            pointHoverBorderWidth: 3,
            fill: true
          },
          {
            label: 'Entertainment',
            data: [120, 150, 200, 180, 220, 190],
            borderColor: '#a855f7',
            backgroundColor: 'rgba(168, 85, 247, 0.15)',
            borderWidth: 3,
            tension: 0.42,
            pointRadius: 5,
            pointBackgroundColor: '#a855f7',
            pointBorderColor: 'rgba(15, 23, 42, 0.9)',
            pointBorderWidth: 2,
            pointHoverRadius: 8,
            pointHoverBackgroundColor: '#a855f7',
            pointHoverBorderColor: '#ffffff',
            pointHoverBorderWidth: 3,
            fill: true
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
            enabled: true,
            backgroundColor: 'rgba(15, 23, 42, 0.98)',
            titleColor: '#f1f5f9',
            bodyColor: '#e2e8f0',
            borderColor: 'rgba(148, 163, 184, 0.3)',
            borderWidth: 2,
            padding: 16,
            displayColors: true,
            usePointStyle: true,
            boxPadding: 8,
            cornerRadius: 12,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13,
              weight: '600'
            },
            callbacks: {
              label: function(context) {
                return context.dataset.label + ': $' + context.parsed.y.toLocaleString();
              },
              labelPointStyle: function(context) {
                return {
                  pointStyle: 'circle',
                  rotation: 0
                };
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            stacked: false,
            grid: {
              color: 'rgba(148, 163, 184, 0.08)',
              drawBorder: false,
              lineWidth: 1
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '500'
              },
              padding: 8,
              callback: function(value) {
                return '$' + value.toLocaleString();
              }
            }
          },
          x: {
            grid: {
              display: false,
              drawBorder: false
            },
            border: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: {
                size: 12,
                weight: '600'
              },
              padding: 8
            }
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

