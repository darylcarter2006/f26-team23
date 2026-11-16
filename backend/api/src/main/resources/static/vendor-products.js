// Vendor products page: lists a vendor's products and lets a vendor team member add or edit them.
// Real login is not built yet, so the page acts as the selected vendor's owner.

const vendorSelect = document.getElementById("vendorSelect");
const productsTable = document.getElementById("productsTable");
const productsTableBody = document.getElementById("productsTableBody");
const emptyState = document.getElementById("emptyState");
const errorBanner = document.getElementById("errorBanner");
const successBanner = document.getElementById("successBanner");
const formHeading = document.getElementById("formHeading");
const productForm = document.getElementById("productForm");
const titleInput = document.getElementById("titleInput");
const descriptionInput = document.getElementById("descriptionInput");
const priceInput = document.getElementById("priceInput");
const stockInput = document.getElementById("stockInput");
const categorySelect = document.getElementById("categorySelect");
const photoUrlInput = document.getElementById("photoUrlInput");
const saveProductButton = document.getElementById("saveProductButton");
const cancelEditButton = document.getElementById("cancelEditButton");

// The vendors loaded from the API, keyed by vendor id, so we can find the owner's user id.
const vendorsById = new Map();
// The id of the product being edited, or null when the form is adding a new product.
let editingProductId = null;

// Shows an error message at the top of the page and hides any success message.
function showError(errorMessage) {
  errorBanner.textContent = errorMessage;
  errorBanner.classList.remove("hidden");
  successBanner.classList.add("hidden");
}

// Shows a success message at the top of the page and hides any error message.
function showSuccess(successMessage) {
  successBanner.textContent = successMessage;
  successBanner.classList.remove("hidden");
  errorBanner.classList.add("hidden");
}

// Clears both messages at the top of the page.
function clearMessages() {
  errorBanner.classList.add("hidden");
  successBanner.classList.add("hidden");
}

// Returns the vendor currently chosen in the dropdown, or undefined when none is chosen.
function getSelectedVendor() {
  return vendorsById.get(vendorSelect.value);
}

// Adds one option to a dropdown.
function addSelectOption(selectElement, optionText, optionValue) {
  const optionElement = document.createElement("option");
  optionElement.textContent = optionText;
  optionElement.value = optionValue;
  selectElement.appendChild(optionElement);
}

// Loads vendors into the dropdown. A ?vendorId= in the address picks that vendor, and a bad id shows a message.
async function loadVendors() {
  const vendors = await sendApiRequest("/api/vendors");
  for (const vendor of vendors) {
    vendorsById.set(vendor.id, vendor);
    addSelectOption(vendorSelect, vendor.businessName, vendor.id);
  }

  const requestedVendorId = new URLSearchParams(window.location.search).get("vendorId");
  if (requestedVendorId) {
    // Asks the API directly so an unknown id gives the server's "Vendor not found" message.
    const requestedVendor = await sendApiRequest("/api/vendors/" + encodeURIComponent(requestedVendorId));
    vendorSelect.value = requestedVendor.id;
  }
}

// Loads categories into the dropdown, with a "No category" choice first.
async function loadCategories() {
  addSelectOption(categorySelect, "No category", "");
  const categories = await sendApiRequest("/api/categories");
  for (const category of categories) {
    addSelectOption(categorySelect, category.name, category.id);
  }
}

// Formats an ISO timestamp from the API as a short local date, or "-" when missing.
function formatUpdatedDate(isoTimestamp) {
  return isoTimestamp ? new Date(isoTimestamp).toLocaleDateString() : "-";
}

// Creates a table cell containing plain text.
function createTextCell(cellText) {
  const tableCell = document.createElement("td");
  tableCell.textContent = cellText;
  return tableCell;
}

// Builds the table row for one product.
function createProductRow(product) {
  const productRow = document.createElement("tr");

  const titleCell = document.createElement("td");
  const titleText = document.createElement("strong");
  titleText.textContent = product.title;
  titleCell.appendChild(titleText);
  productRow.appendChild(titleCell);

  productRow.appendChild(createTextCell(product.categoryName || "-"));
  productRow.appendChild(createTextCell("$" + product.price));
  productRow.appendChild(createTextCell(product.stockCount));
  productRow.appendChild(createTextCell(formatUpdatedDate(product.updatedAt)));

  const actionsCell = document.createElement("td");
  actionsCell.className = "actions-cell";
  const editButton = document.createElement("button");
  editButton.type = "button";
  editButton.className = "mini-btn message";
  editButton.textContent = "Edit";
  editButton.addEventListener("click", () => startEditingProduct(product.id));
  actionsCell.appendChild(editButton);
  productRow.appendChild(actionsCell);

  return productRow;
}

// Draws the vendor's products, or the empty message when there are none.
function renderProducts(products) {
  productsTableBody.replaceChildren(...products.map(createProductRow));
  const hasProducts = products.length > 0;
  productsTable.classList.toggle("hidden", !hasProducts);
  emptyState.classList.toggle("hidden", hasProducts);
}

// Loads the selected vendor's products from the API and draws them.
async function loadProducts() {
  const selectedVendor = getSelectedVendor();
  if (!selectedVendor) {
    renderProducts([]);
    return;
  }
  try {
    const products = await sendApiRequest("/api/products?vendorId=" + encodeURIComponent(selectedVendor.id));
    renderProducts(products);
  } catch (loadError) {
    renderProducts([]);
    showError(loadError.message);
  }
}

// Puts the form back into "add a product" mode with empty fields.
function resetFormToAddMode() {
  editingProductId = null;
  productForm.reset();
  formHeading.textContent = "Add a Product";
  saveProductButton.textContent = "Add Product";
  cancelEditButton.classList.add("hidden");
}

// Fetches one product from the API and fills the form so it can be edited.
async function startEditingProduct(productId) {
  clearMessages();
  try {
    const product = await sendApiRequest("/api/products/" + encodeURIComponent(productId));
    editingProductId = product.id;
    titleInput.value = product.title;
    descriptionInput.value = product.description || "";
    priceInput.value = product.price;
    stockInput.value = product.stockCount;
    categorySelect.value = product.categoryId || "";
    photoUrlInput.value = product.photoUrls.length > 0 ? product.photoUrls[0] : "";
    formHeading.textContent = "Edit Product";
    saveProductButton.textContent = "Save Changes";
    cancelEditButton.classList.remove("hidden");
    productForm.scrollIntoView({ behavior: "smooth" });
  } catch (loadError) {
    showError(loadError.message);
    await loadProducts();
  }
}

// Returns a whole number from a form field, or null when the field is empty or not a whole number.
function readWholeNumber(inputElement) {
  if (inputElement.value === "") {
    return null;
  }
  const parsedNumber = Number(inputElement.value);
  return Number.isInteger(parsedNumber) ? parsedNumber : null;
}

// Returns a message describing what is wrong with the form, or null when it is valid.
function findFormProblem() {
  if (!getSelectedVendor()) {
    return "Please choose a vendor first.";
  }
  if (titleInput.value.trim() === "") {
    return "Please enter a product title.";
  }
  const price = readWholeNumber(priceInput);
  if (price === null || price < 0) {
    return "Price must be a whole number of dollars, 0 or more.";
  }
  const stockCount = readWholeNumber(stockInput);
  if (stockCount === null || stockCount < 0) {
    return "Stock count must be a whole number, 0 or more.";
  }
  return null;
}

// Builds the JSON body for a product from the form fields. The vendor's owner is the acting user.
function buildProductRequestBody() {
  const selectedVendor = getSelectedVendor();
  const photoUrl = photoUrlInput.value.trim();
  return {
    actorId: selectedVendor.userId,
    vendorId: selectedVendor.id,
    title: titleInput.value.trim(),
    description: descriptionInput.value.trim(),
    price: readWholeNumber(priceInput),
    stockCount: readWholeNumber(stockInput),
    categoryId: categorySelect.value || null,
    photoUrls: photoUrl ? [photoUrl] : [],
  };
}

// Creates a new product or saves changes to the one being edited, then shows the result.
async function saveProduct(submitEvent) {
  submitEvent.preventDefault();
  const formProblem = findFormProblem();
  if (formProblem) {
    showError(formProblem);
    return;
  }

  const isEditing = editingProductId !== null;
  const requestPath = isEditing ? "/api/products/" + encodeURIComponent(editingProductId) : "/api/products";
  saveProductButton.disabled = true;
  try {
    const savedProduct = await sendApiRequest(requestPath, {
      method: isEditing ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(buildProductRequestBody()),
    });
    showSuccess("\"" + savedProduct.title + "\" was " + (isEditing ? "updated." : "added."));
    resetFormToAddMode();
    await loadProducts();
  } catch (saveError) {
    showError(saveError.message);
  } finally {
    saveProductButton.disabled = false;
  }
}

// Loads vendors and categories, then the selected vendor's products. Any error is shown at the top.
async function startPage() {
  try {
    await Promise.all([loadVendors(), loadCategories()]);
  } catch (startError) {
    showError(startError.message);
  }
  if (vendorsById.size === 0 && errorBanner.classList.contains("hidden")) {
    showError("There are no vendors yet. Apply and get approved on the Become a Vendor page first.");
  }
  await loadProducts();
}

vendorSelect.addEventListener("change", () => {
  clearMessages();
  resetFormToAddMode();
  loadProducts();
});
cancelEditButton.addEventListener("click", () => {
  clearMessages();
  resetFormToAddMode();
});
productForm.addEventListener("submit", saveProduct);
startPage();
