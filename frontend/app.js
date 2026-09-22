// ============================================================================
// Spartan Marketplace — homepage interactivity
// No backend yet: role/login state is a client-side demo switcher (bottom
// right) so the click-through demo can show Guest / Customer / Vendor /
// Admin views without real auth. Listings come from data/mock-listings.js.
// ============================================================================

const PERSONAS = {
  customer: { name: "Maya R.", initials: "MR", roleLabel: "Customer · .edu Verified" },
  vendor: { name: "Jordan P.", initials: "JP", roleLabel: "Vendor · Spartan Tech Exchange" },
  admin: { name: "Sam K.", initials: "SK", roleLabel: "SysAdmin · Platform Staff" },
};

const state = {
  role: "guest", // guest | customer | vendor | admin
  category: "All Categories",
  priceMin: null,
  priceMax: null,
  sort: "newest",
  search: "",
  saved: new Set([2, 6]), // a couple pre-saved items so the logged-in view isn't empty
  dropdownOpen: false,
};

const els = {
  headerRight: document.getElementById("headerRight"),
  dropdownMount: document.getElementById("dropdownMount"),
  categoryList: document.getElementById("categoryList"),
  cardGrid: document.getElementById("cardGrid"),
  emptyState: document.getElementById("emptyState"),
  resultsCount: document.getElementById("resultsCount"),
  searchInput: document.getElementById("searchInput"),
  priceMin: document.getElementById("priceMin"),
  priceMax: document.getElementById("priceMax"),
  sortSelect: document.getElementById("sortSelect"),
  resetFiltersBtn: document.getElementById("resetFiltersBtn"),
  demoSwitcher: document.getElementById("demoSwitcher"),
  logoBtn: document.getElementById("logoBtn"),
  toast: document.getElementById("toast"),
};

// ----------------------------------------------------------------------------
// Toast (used for "coming soon" affordances and accept/reject confirmations)
// ----------------------------------------------------------------------------

let toastTimer = null;
function showToast(message) {
  els.toast.textContent = message;
  els.toast.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => els.toast.classList.remove("show"), 2200);
}

// ----------------------------------------------------------------------------
// Header (auth buttons vs. logged-in user area) + profile dropdown
// ----------------------------------------------------------------------------

function renderHeader() {
  state.dropdownOpen = false;

  if (state.role === "guest") {
    els.headerRight.innerHTML = `
      <div class="auth-buttons">
        <button class="btn btn-ghost" id="signUpBtn">Sign Up</button>
        <button class="btn btn-primary" id="logInBtn">Log In</button>
      </div>`;
    els.dropdownMount.innerHTML = "";
    document.getElementById("signUpBtn").addEventListener("click", () =>
      showToast("Sign up isn't wired up yet — this is a front-end demo.")
    );
    document.getElementById("logInBtn").addEventListener("click", () =>
      showToast("Log in isn't wired up yet — try the demo switcher instead ↘")
    );
    return;
  }

  const persona = PERSONAS[state.role];
  const isVendor = state.role === "vendor";
  const isAdmin = state.role === "admin";
  const roleClass = isAdmin ? "role-admin" : "";

  let dashboardIconBtn = "";
  if (isVendor) {
    dashboardIconBtn = `<button class="icon-btn" id="dashboardIconBtn" aria-label="Vendor Dashboard" style="background: var(--gold); color: var(--navy);">🏪</button>`;
  } else if (isAdmin) {
    dashboardIconBtn = `<button class="icon-btn" id="dashboardIconBtn" aria-label="Admin Dashboard" style="background: var(--crimson); color: #fff;">🛡️</button>`;
  }

  els.headerRight.innerHTML = `
    <div class="user-area">
      <button class="btn btn-primary" id="createListingBtn">+ Create Listing</button>
      ${dashboardIconBtn}
      <button class="icon-btn" id="messagesBtn" aria-label="Messages">💬</button>
      <button class="icon-btn" id="notificationsBtn" aria-label="Notifications">🔔<span class="dot"></span></button>
      <div class="profile ${roleClass}" id="profileBtn" aria-expanded="false">
        <span class="avatar-wrap">
          <span class="vendor-ring"></span>
          <span class="avatar">${persona.initials}</span>
          ${isVendor ? '<span class="vendor-flag">🏪</span>' : ""}
          ${isAdmin ? '<span class="vendor-flag">🛡️</span>' : ""}
        </span>
        <span class="profile-name">${persona.name}</span>
        <span class="chevron">▼</span>
      </div>
    </div>`;

  document.getElementById("createListingBtn").addEventListener("click", () =>
    showToast("Listing creation isn't wired up yet in this demo.")
  );
  document.getElementById("messagesBtn").addEventListener("click", () =>
    showToast("Messages aren't wired up yet in this demo.")
  );
  document.getElementById("notificationsBtn").addEventListener("click", () =>
    showToast("Notifications aren't wired up yet in this demo.")
  );
  const dashBtn = document.getElementById("dashboardIconBtn");
  if (dashBtn) {
    dashBtn.addEventListener("click", () => {
      window.location.href = isVendor ? "vendor-dashboard.html" : "admin-dashboard.html";
    });
  }
  document.getElementById("profileBtn").addEventListener("click", (e) => {
    e.stopPropagation();
    state.dropdownOpen = !state.dropdownOpen;
    renderDropdown();
  });

  renderDropdown();
}

function renderDropdown() {
  if (state.role === "guest" || !state.dropdownOpen) {
    els.dropdownMount.innerHTML = "";
    const profileBtn = document.getElementById("profileBtn");
    if (profileBtn) profileBtn.setAttribute("aria-expanded", "false");
    return;
  }

  const profileBtn = document.getElementById("profileBtn");
  if (profileBtn) profileBtn.setAttribute("aria-expanded", "true");

  const persona = PERSONAS[state.role];
  const isVendor = state.role === "vendor";
  const isAdmin = state.role === "admin";

  let roleSpecificItem = "";
  let roleLabelClass = "";
  let borderClass = "";
  if (isVendor) {
    roleSpecificItem = `<div class="dropdown-item vendor-link" data-nav="vendor-dashboard.html">🏪 Vendor Dashboard</div>`;
    roleLabelClass = "accent-gold";
  } else if (isAdmin) {
    roleSpecificItem = `<div class="dropdown-item admin-link" data-nav="admin-dashboard.html">🛡️ Admin Dashboard</div>`;
    roleLabelClass = "accent-crimson";
    borderClass = "role-admin-border";
  }

  const upsellItem =
    state.role === "customer"
      ? `<div class="dropdown-item upsell" id="applyVendorItem">🏪 Apply to Become a Vendor</div>`
      : "";

  els.dropdownMount.innerHTML = `
    <div class="dropdown ${borderClass}">
      <div class="dropdown-header">
        <p class="name">${persona.name}</p>
        <p class="role ${roleLabelClass}">${persona.roleLabel}</p>
      </div>
      ${roleSpecificItem}
      <div class="dropdown-item">📦 My Listings</div>
      <div class="dropdown-item">💬 Messages</div>
      <div class="dropdown-item">❤️ Saved Items</div>
      ${upsellItem}
      <div class="dropdown-item">⚙️ Account Settings</div>
      <div class="dropdown-item logout" id="logOutItem">Log Out</div>
    </div>`;

  const navItem = els.dropdownMount.querySelector("[data-nav]");
  if (navItem) {
    navItem.addEventListener("click", () => {
      window.location.href = navItem.getAttribute("data-nav");
    });
  }
  const applyItem = document.getElementById("applyVendorItem");
  if (applyItem) {
    applyItem.addEventListener("click", () =>
      showToast("Vendor application form isn't wired up yet in this demo.")
    );
  }
  document.getElementById("logOutItem").addEventListener("click", () => {
    setRole("guest");
    showToast("Logged out.");
  });
}

document.addEventListener("click", () => {
  if (state.dropdownOpen) {
    state.dropdownOpen = false;
    renderDropdown();
  }
});

// ----------------------------------------------------------------------------
// Demo role switcher
// ----------------------------------------------------------------------------

function setRole(role) {
  state.role = role;
  renderHeader();
  updateSwitcherActiveState();
}

function updateSwitcherActiveState() {
  els.demoSwitcher.querySelectorAll("button").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.role === state.role);
  });
}

els.demoSwitcher.querySelectorAll("button").forEach((btn) => {
  btn.addEventListener("click", () => setRole(btn.dataset.role));
});

els.logoBtn.addEventListener("click", () => {
  els.searchInput.value = "";
  state.search = "";
  state.category = "All Categories";
  state.priceMin = null;
  state.priceMax = null;
  state.sort = "newest";
  els.priceMin.value = "";
  els.priceMax.value = "";
  els.sortSelect.value = "newest";
  renderCategories();
  renderCards();
});

// ----------------------------------------------------------------------------
// Categories (derived from the mock data so it never drifts from what's
// actually listed)
// ----------------------------------------------------------------------------

function getCategories() {
  const preferredOrder = ["Textbooks", "Dorm Essentials", "Electronics", "Furniture", "Clothing", "Sports & Outdoors"];
  const present = new Set(MOCK_LISTINGS.map((l) => l.category));
  const ordered = preferredOrder.filter((c) => present.has(c));
  const rest = [...present].filter((c) => !ordered.includes(c)).sort();
  return ["All Categories", ...ordered, ...rest];
}

function renderCategories() {
  const categories = getCategories();
  els.categoryList.innerHTML = categories
    .map(
      (cat) => `
      <li>
        <button class="category-item ${cat === state.category ? "active" : ""}" data-category="${cat}">
          ${cat}
        </button>
      </li>`
    )
    .join("");

  els.categoryList.querySelectorAll(".category-item").forEach((btn) => {
    btn.addEventListener("click", () => {
      state.category = btn.dataset.category;
      renderCategories();
      renderCards();
    });
  });
}

// ----------------------------------------------------------------------------
// Filtering / sorting / rendering the result cards
// ----------------------------------------------------------------------------

function getFilteredListings() {
  let results = MOCK_LISTINGS.slice();

  if (state.category !== "All Categories") {
    results = results.filter((l) => l.category === state.category);
  }
  if (state.search.trim()) {
    const q = state.search.trim().toLowerCase();
    results = results.filter((l) => l.title.toLowerCase().includes(q));
  }
  if (state.priceMin !== null && !Number.isNaN(state.priceMin)) {
    results = results.filter((l) => l.price >= state.priceMin);
  }
  if (state.priceMax !== null && !Number.isNaN(state.priceMax)) {
    results = results.filter((l) => l.price <= state.priceMax);
  }

  if (state.sort === "price-asc") {
    results.sort((a, b) => a.price - b.price);
  } else if (state.sort === "price-desc") {
    results.sort((a, b) => b.price - a.price);
  } else {
    results.sort((a, b) => new Date(b.postedAt) - new Date(a.postedAt));
  }

  return results;
}

function renderCards() {
  const results = getFilteredListings();
  els.resultsCount.textContent = `Showing ${results.length} listing${results.length === 1 ? "" : "s"}`;

  if (results.length === 0) {
    els.cardGrid.innerHTML = "";
    els.emptyState.classList.remove("hidden");
    return;
  }
  els.emptyState.classList.add("hidden");

  els.cardGrid.innerHTML = results
    .map((l) => {
      const isSaved = state.saved.has(l.id);
      const badge = l.type === "product" ? `<span class="badge">Vendor</span>` : "";
      return `
      <div class="card" data-id="${l.id}">
        <div class="card-photo">
          ${l.icon}
          <button class="save-btn ${isSaved ? "saved" : ""}" data-id="${l.id}" aria-label="Save item">${isSaved ? "♥" : "♡"}</button>
        </div>
        <div class="card-body">
          ${badge}
          <p class="card-title">${l.title}</p>
          <div class="card-price-row"><span class="card-price">$${l.price}</span></div>
          <p class="card-meta">${l.conditionOrLocation}</p>
        </div>
      </div>`;
    })
    .join("");

  els.cardGrid.querySelectorAll(".save-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const id = Number(btn.dataset.id);
      if (state.saved.has(id)) {
        state.saved.delete(id);
      } else {
        state.saved.add(id);
      }
      renderCards();
    });
  });
}

// ----------------------------------------------------------------------------
// Filter controls
// ----------------------------------------------------------------------------

els.searchInput.addEventListener("input", (e) => {
  state.search = e.target.value;
  renderCards();
});

els.priceMin.addEventListener("input", (e) => {
  state.priceMin = e.target.value === "" ? null : Number(e.target.value);
  renderCards();
});

els.priceMax.addEventListener("input", (e) => {
  state.priceMax = e.target.value === "" ? null : Number(e.target.value);
  renderCards();
});

els.sortSelect.addEventListener("change", (e) => {
  state.sort = e.target.value;
  renderCards();
});

els.resetFiltersBtn.addEventListener("click", () => {
  state.category = "All Categories";
  state.priceMin = null;
  state.priceMax = null;
  state.sort = "newest";
  state.search = "";
  els.searchInput.value = "";
  els.priceMin.value = "";
  els.priceMax.value = "";
  els.sortSelect.value = "newest";
  renderCategories();
  renderCards();
});

// ----------------------------------------------------------------------------
// Init
// ----------------------------------------------------------------------------

renderHeader();
updateSwitcherActiveState();
renderCategories();
renderCards();
