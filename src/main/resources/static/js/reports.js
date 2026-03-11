
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

  // Trigger animations
  const reportsWrap = pageContent.querySelector('.reports-wrap');
  if (reportsWrap) {
    reportsWrap.classList.remove('reports-animate');
    requestAnimationFrame(function () {
      reportsWrap.classList.add('reports-animate');
    });
  }

  // Init charts first, then load  data from API
  setTimeout(function () {
    initializeReportsCharts();
    setupReportsResizeObserver();
    loadReportsData();
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
        labels: [],
        datasets: [
          {
            label: 'Income',
            data: [],
            backgroundColor: incomeGradient,
            borderColor: 'rgba(34, 197, 94, 1)',
            borderWidth: 0,
            borderRadius: 10,
            borderSkipped: false,
            barThickness: 36
          },
          {
            label: 'Expenses',
            data: [],
            backgroundColor: expensesGradient,
            borderColor: 'rgba(236, 72, 153, 1)',
            borderWidth: 0,
            borderRadius: 10,
            borderSkipped: false,
            barThickness: 36
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
        labels: [],
        datasets: [{
          label: 'Savings',
          data: [],
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
        labels: [],
        datasets: []
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
  const range = document.getElementById('reportsTimeRange')?.value || '6months';

  fetch('/api/reports/data?range=' + encodeURIComponent(range))
    .then(function (res) {
      if (!res.ok) throw new Error('Failed to load report data');
      return res.json();
    })
    .then(function (data) {
      console.log('[Reports] API response:', data);
      if (!data.hasData) {
        console.log('[Reports] No data for this period — showing empty state');
        showReportsEmptyState();
        return;
      }
      hideReportsEmptyState();
      updateReportsCharts(data);
      updateReportsStats(data.stats);
      console.log('[Reports] Stats applied:', data.stats);
    })
    .catch(function (err) {
      console.error('[Reports] fetch error:', err);
      showReportsEmptyState();
    });
}

function showReportsEmptyState() {
  // Show a message inside each chart container
  ['incomeVsExpensesChart', 'savingsTrendChart', 'categoryTrendsChart'].forEach(function (id) {
    var canvas = document.getElementById(id);
    if (!canvas) return;
    var container = canvas.parentElement;
    if (!container) return;
    canvas.style.display = 'none';
    if (!container.querySelector('.reports-empty-msg')) {
      var msg = document.createElement('div');
      msg.className = 'reports-empty-msg';
      msg.textContent = 'No financial data for this period.';
      container.appendChild(msg);
    }
  });
}

function hideReportsEmptyState() {
  ['incomeVsExpensesChart', 'savingsTrendChart', 'categoryTrendsChart'].forEach(function (id) {
    var canvas = document.getElementById(id);
    if (!canvas) return;
    canvas.style.display = '';
    var container = canvas.parentElement;
    if (container) {
      var msg = container.querySelector('.reports-empty-msg');
      if (msg) msg.remove();
    }
  });
}

function updateReportsCharts(data) {
  var labels = data.labels || [];

  // ── Income vs Expenses ────────────────────────────────────────
  if (incomeExpensesChartInstance) {
    var ctx = incomeExpensesChartInstance.ctx;
    var h = ctx.canvas.offsetHeight || 300;

    var incomeGrad = ctx.createLinearGradient(0, 0, 0, h);
    incomeGrad.addColorStop(0, 'rgba(34, 197, 94, 0.9)');
    incomeGrad.addColorStop(1, 'rgba(16, 185, 129, 0.7)');

    var expensesGrad = ctx.createLinearGradient(0, 0, 0, h);
    expensesGrad.addColorStop(0, 'rgba(236, 72, 153, 0.9)');
    expensesGrad.addColorStop(1, 'rgba(219, 39, 119, 0.7)');

    incomeExpensesChartInstance.data.labels = labels;
    incomeExpensesChartInstance.data.datasets[0].data = data.incomeVsExpenses.income;
    incomeExpensesChartInstance.data.datasets[0].backgroundColor = incomeGrad;
    incomeExpensesChartInstance.data.datasets[1].data = data.incomeVsExpenses.expenses;
    incomeExpensesChartInstance.data.datasets[1].backgroundColor = expensesGrad;
    incomeExpensesChartInstance.update();
  }

  // ── Savings Trend ─────────────────────────────────────────────
  if (savingsTrendChartInstance) {
    var sCtx = savingsTrendChartInstance.ctx;
    var sH = sCtx.canvas.offsetHeight || 300;

    var lineGrad = sCtx.createLinearGradient(0, 0, 0, sH);
    lineGrad.addColorStop(0, 'rgba(34, 197, 94, 1)');
    lineGrad.addColorStop(1, 'rgba(16, 185, 129, 0.8)');

    var fillGrad = sCtx.createLinearGradient(0, 0, 0, sH);
    fillGrad.addColorStop(0,   'rgba(34, 197, 94, 0.3)');
    fillGrad.addColorStop(0.5, 'rgba(34, 197, 94, 0.15)');
    fillGrad.addColorStop(1,   'rgba(34, 197, 94, 0.02)');

    savingsTrendChartInstance.data.labels = labels;
    savingsTrendChartInstance.data.datasets[0].data = data.savingsTrend;
    savingsTrendChartInstance.data.datasets[0].borderColor = lineGrad;
    savingsTrendChartInstance.data.datasets[0].backgroundColor = fillGrad;
    savingsTrendChartInstance.update();
  }

  // ── Category Trends ───────────────────────────────────────────
  if (categoryTrendsChartInstance && data.categoryTrends && data.categoryTrends.length > 0) {
    categoryTrendsChartInstance.data.labels = labels;
    categoryTrendsChartInstance.data.datasets = data.categoryTrends.map(function (series) {
      return {
        label:                  series.label,
        data:                   series.data,
        borderColor:            series.color,
        backgroundColor:        hexToRgba(series.color, 0.15),
        borderWidth:            3,
        tension:                0.42,
        fill:                   true,
        pointRadius:            5,
        pointBackgroundColor:   series.color,
        pointBorderColor:       'rgba(15, 23, 42, 0.9)',
        pointBorderWidth:       2,
        pointHoverRadius:       8,
        pointHoverBackgroundColor: series.color,
        pointHoverBorderColor:  '#ffffff',
        pointHoverBorderWidth:  3
      };
    });
    categoryTrendsChartInstance.update();

    // Rebuild legend
    var legendContainer = document.getElementById('categoryLegend');
    if (legendContainer) {
      legendContainer.innerHTML = data.categoryTrends.map(function (s) {
        return '<div class="legend-item">' +
          '<span class="legend-dot" style="background-color:' + s.color + ';"></span>' +
          '<span class="legend-label">' + s.label + '</span>' +
          '</div>';
      }).join('');
    }
  }
}

function updateReportsStats(stats) {
  if (!stats) return;
  var fmt = function (n) { return '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 }); };
  var el = function (id) { return document.getElementById(id); };

  if (el('totalSaved'))   el('totalSaved').textContent   = fmt(stats.totalSaved);
  if (el('avgIncome'))    el('avgIncome').textContent    = fmt(stats.avgIncome);
  if (el('avgExpenses'))  el('avgExpenses').textContent  = fmt(stats.avgExpenses);
  if (el('savingsRate'))  el('savingsRate').textContent  = Number(stats.savingsRate).toFixed(1) + '%';
}

function hexToRgba(hex, alpha) {
  var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  if (!result) return 'rgba(100,100,100,' + alpha + ')';
  return 'rgba(' +
    parseInt(result[1], 16) + ',' +
    parseInt(result[2], 16) + ',' +
    parseInt(result[3], 16) + ',' +
    alpha + ')';
}



if (typeof window.initReports === 'undefined') {
  window.initReports = renderReportsSection;
}

