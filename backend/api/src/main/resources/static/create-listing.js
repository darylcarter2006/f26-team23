// Create listing page: loads categories and submits a new listing to the API.

// Real login is not built yet, so every listing is created by this one demo user.
const DEMO_SELLER_USER_ID = "<PASTE_CUSTOMER_USER_ID_HERE>";

const createListingForm = document.getElementById("createListingForm");
const titleInput = document.getElementById("titleInput");
const descriptionInput = document.getElementById("descriptionInput");
const priceInput = document.getElementById("priceInput");
const categorySelect = document.getElementById("categorySelect");
const photoUrlInput = document.getElementById("photoUrlInput");
const formErrorBanner = document.getElementById("formErrorBanner");
const formSuccessBanner = document.getElementById("formSuccessBanner");

// Shows an error message above the form and hides any success message.
function showFormError(errorMessage) {
  formErrorBanner.textContent = errorMessage;
  formErrorBanner.classList.remove("hidden");
  formSuccessBanner.classList.add("hidden");
}

// Shows the success message with a link to the new listing's detail page.
function showFormSuccess(createdListing) {
  const detailPageLink = document.createElement("a");
  detailPageLink.href = "listing.html?id=" + encodeURIComponent(createdListing.id);
  detailPageLink.textContent = "View your listing";
  formSuccessBanner.replaceChildren("Listing submitted for review. ", detailPageLink);
  formSuccessBanner.classList.remove("hidden");
  formErrorBanner.classList.add("hidden");
}

// Adds one option to the category dropdown.
function addCategoryOption(optionText, optionValue) {
  const categoryOption = document.createElement("option");
  categoryOption.textContent = optionText;
  categoryOption.value = optionValue;
  categorySelect.appendChild(categoryOption);
}

// Loads categories from the API into the dropdown, with a "No category" choice first.
async function loadCategoryOptions() {
  try {
    const categories = await sendApiRequest("/api/categories");
    addCategoryOption("No category", "");
    for (const category of categories) {
      addCategoryOption(category.name, category.id);
    }
  } catch (loadError) {
    showFormError(loadError.message);
  }
}

// Builds the JSON body for the new listing from the form fields.
function buildListingRequestBody() {
  const photoUrl = photoUrlInput.value.trim();
  return {
    sellerId: DEMO_SELLER_USER_ID,
    title: titleInput.value.trim(),
    description: descriptionInput.value.trim(),
    price: priceInput.value === "" ? null : Number(priceInput.value),
    categoryId: categorySelect.value || null,
    photoUrls: photoUrl ? [photoUrl] : [],
  };
}

// Sends the new listing to the API, then shows the success message or the server's error.
async function submitListing(submitEvent) {
  submitEvent.preventDefault();
  try {
    const createdListing = await sendApiRequest("/api/listings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(buildListingRequestBody()),
    });
    showFormSuccess(createdListing);
    createListingForm.reset();
  } catch (submitError) {
    showFormError(submitError.message);
  }
}

createListingForm.addEventListener("submit", submitListing);
loadCategoryOptions();
