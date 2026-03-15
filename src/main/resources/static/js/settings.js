function renderSettings() {
  const section = document.getElementById('section-settings');
  if (!section) return;

  const contentDiv = section.querySelector('.section-content');
  if (!contentDiv) return;

  contentDiv.innerHTML = `
    <div class="settings-page">
      <div class="settings-top-row">
        <div class="settings-card settings-card--profile">
        <div class="settings-card-header">
          <h2 class="settings-card-title">Profile Information</h2>
        </div>
        <form class="settings-profile-form" id="settings-profile-form">
          <div class="settings-form-row">
            <div class="settings-form-field">
              <label for="settings-first-name">First Name</label>
              <input id="settings-first-name" name="firstName" type="text" value="John" autocomplete="given-name" />
            </div>
            <div class="settings-form-field">
              <label for="settings-last-name">Last Name</label>
              <input id="settings-last-name" name="lastName" type="text" value="Doe" autocomplete="family-name" />
            </div>
          </div>
          <div class="settings-form-row">
            <div class="settings-form-field">
              <label for="settings-email">Email</label>
              <input id="settings-email" name="email" type="email" value="john.doe@example.com" autocomplete="email" />
            </div>
            <div class="settings-form-field">
              <label for="settings-currency">Currency</label>
              <select id="settings-currency" name="currency">
                <option value="usd" selected>USD ($)</option>
                <option value="eur">EUR (€)</option>
                <option value="gbp">GBP (£)</option>
              </select>
            </div>
          </div>
          <div class="settings-form-actions">
            <button type="button" class="settings-btn settings-btn-primary" id="settings-save-btn">Save Changes</button>
          </div>
        </form>
      </div>

      <div class="settings-card settings-card--account">
        <div class="settings-card-header">
          <h2 class="settings-card-title">Account Type</h2>
        </div>
        <div class="settings-account-types" role="radiogroup" aria-label="Account type">
          <button type="button" class="settings-account-tile settings-account-tile--current" data-type="personal" aria-pressed="true">
            <div class="settings-account-tile-header">
              <span class="settings-account-title">Personal</span>
              <span class="settings-account-badge">Current</span>
            </div>
            <p class="settings-account-description">Track personal expenses, budgets, and savings goals.</p>
          </button>
          <button type="button" class="settings-account-tile" data-type="business" aria-pressed="false">
            <div class="settings-account-tile-header">
              <span class="settings-account-title">Business</span>
            </div>
            <p class="settings-account-description">Advanced features for small business expense tracking.</p>
          </button>
        </div>
      </div>
      </div>

      <div class="settings-card settings-card--danger">
        <div class="settings-card-header settings-card-header--danger">
          <h2 class="settings-card-title settings-card-title--danger">Danger Zone</h2>
        </div>
        <div class="settings-danger-content">
          <div class="settings-danger-text">
            <h3>Delete Account</h3>
            <p>Permanently delete your account and all data</p>
          </div>
          <div class="settings-danger-actions">
            <button type="button" class="settings-btn settings-btn-danger" id="settings-delete-btn">Delete Account</button>
          </div>
        </div>
      </div>
    </div>
  `;

  const saveBtn = document.getElementById('settings-save-btn');
  if (saveBtn) {
    saveBtn.addEventListener('click', function () {
      // Placeholder behavior - backend integration will add later
      alert('Profile changes would be saved here.');
    });
  }

  const deleteBtn = document.getElementById('settings-delete-btn');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', function () {
      const confirmed = confirm('This will permanently delete your account and all data. Continue?');
      if (confirmed) {
        alert('Account deletion would be handled here.');
      }
    });
  }

  const accountTiles = contentDiv.querySelectorAll('.settings-account-tile');
  accountTiles.forEach(function(tile) {
    tile.addEventListener('click', function () {
      accountTiles.forEach(function(t) {
        t.classList.remove('settings-account-tile--current');
        t.setAttribute('aria-pressed', 'false');
      });
      tile.classList.add('settings-account-tile--current');
      tile.setAttribute('aria-pressed', 'true');
    });
  });
}

