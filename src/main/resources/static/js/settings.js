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

  const firstNameInput = document.getElementById('settings-first-name');
  const lastNameInput = document.getElementById('settings-last-name');
  const emailInput = document.getElementById('settings-email');
  const currencySelect = document.getElementById('settings-currency');
  const accountTiles = contentDiv.querySelectorAll('.settings-account-tile');

  function getSelectedAccountType() {
    let type = 'PERSONAL';
    accountTiles.forEach(function (tile) {
      if (tile.classList.contains('settings-account-tile--current')) {
        type = tile.getAttribute('data-type') === 'business' ? 'BUSINESS' : 'PERSONAL';
      }
    });
    return type;
  }

  function selectAccountType(type) {
    accountTiles.forEach(function (tile) {
      const isBusiness = tile.getAttribute('data-type') === 'business';
      const tileType = isBusiness ? 'BUSINESS' : 'PERSONAL';
      if (tileType === type) {
        tile.classList.add('settings-account-tile--current');
        tile.setAttribute('aria-pressed', 'true');
      } else {
        tile.classList.remove('settings-account-tile--current');
        tile.setAttribute('aria-pressed', 'false');
      }
    });
  }

  accountTiles.forEach(function (tile) {
    tile.addEventListener('click', function () {
      const isBusiness = tile.getAttribute('data-type') === 'business';
      selectAccountType(isBusiness ? 'BUSINESS' : 'PERSONAL');
    });
  });

  // Load existing settings from backend
  fetch('/api/settings', { credentials: 'same-origin' })
    .then(function (response) {
      if (!response.ok) {
        return null;
      }
      return response.json();
    })
    .then(function (data) {
      if (!data) {
        return;
      }
      if (data.firstName && firstNameInput) firstNameInput.value = data.firstName;
      if (data.lastName && lastNameInput) lastNameInput.value = data.lastName;
      if (data.email && emailInput) emailInput.value = data.email;
      if (data.currency && currencySelect) currencySelect.value = data.currency;
      if (data.accountType) {
        selectAccountType(data.accountType);
      }
    })
    .catch(function (err) {
      console.error('Failed to load settings', err);
    });

  const saveBtn = document.getElementById('settings-save-btn');
  if (saveBtn) {
    saveBtn.addEventListener('click', function () {
      const payload = {
        firstName: firstNameInput ? firstNameInput.value : null,
        lastName: lastNameInput ? lastNameInput.value : null,
        email: emailInput ? emailInput.value : null,
        currency: currencySelect ? currencySelect.value : null,
        accountType: getSelectedAccountType()
      };

      fetch('/api/settings', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('Failed to save settings');
          }
          return response.json();
        })
        .then(function () {
          alert('Settings saved successfully.');
        })
        .catch(function (err) {
          console.error(err);
          alert('Could not save settings. Please try again.');
        });
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
}
