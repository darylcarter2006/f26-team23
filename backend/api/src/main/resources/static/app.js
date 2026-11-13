// Browse and search page: loads categories and listings from the API and renders them.

const SORT_VALUE_SENT_TO_API = {
  "newest": "newest",
  "price-asc": "priceAsc",
  "price-desc": "priceDesc",
};

let selectedCategoryId = "";

const searchForm = document.getElementById("searchForm");
const searchInput = document.getElementById("searchInput");
const categoryList = document.getElementById("categoryList");
const minimumPriceInput = document.getElementById("minimumPriceInput");
const maximumPriceInput = document.getElementById("maximumPriceInput");
const sortSelect = document.getElementById("sortSelect");
const applyFiltersButton = document.getElementById("applyFiltersButton");
const listingCardsContainer = document.getElementById("listingCardsContainer");
const resultsCount = document.getElementById("resultsCount");
const emptyState = document.getElementById("emptyState");
const errorMessageBanner = document.getElementById("errorMessageBanner");

// Shows an error message in the banner at the top of the results.
function showErrorMessage(errorMessage) {
  errorMessageBanner.textContent = errorMessage;
  errorMessageBanner.classList.remove("hidden");
}

// Hides the error banner.
function hideErrorMessage() {
  errorMessageBanner.classList.add("hidden");
}

// Builds the query string for the listings request, leaving out every empty filter.
function buildListingsQueryString() {
  const queryParameters = new URLSearchParams();
  addQueryParameterIfNotEmpty(queryParameters, "q", searchInput.value.trim());
  addQueryParameterIfNotEmpty(queryParameters, "categoryId", selectedCategoryId);
  addQueryParameterIfNotEmpty(queryParameters, "minPrice", minimumPriceInput.value);
  addQueryParameterIfNotEmpty(queryParameters, "maxPrice", maximumPriceInput.value);
  addQueryParameterIfNotEmpty(queryParameters, "sort", SORT_VALUE_SENT_TO_API[sortSelect.value]);
  return queryParameters.toString();
}

// Adds a query parameter only when its value is not empty.
function addQueryParameterIfNotEmpty(queryParameters, parameterName, parameterValue) {
  if (parameterValue) {
    queryParameters.set(parameterName, parameterValue);
  }
}

// Fetches the listings that match the current filters from the API.
function fetchListingsFromApi() {
  return sendApiRequest("/api/listings?" + buildListingsQueryString());
}

// Fetches all categories from the API.
function fetchCategoriesFromApi() {
  return sendApiRequest("/api/categories");
}

// Creates one category button for the sidebar.
function createCategoryButton(categoryName, categoryId) {
  const categoryListItem = document.createElement("li");
  const categoryButton = document.createElement("button");
  categoryButton.className = "category-item";
  categoryButton.textContent = categoryName;
  if (categoryId === selectedCategoryId) {
    categoryButton.classList.add("active");
  }
  categoryButton.addEventListener("click", () => selectCategory(categoryId));
  categoryListItem.appendChild(categoryButton);
  return categoryListItem;
}

// Fills the sidebar with an "All categories" button and one button per category.
function renderCategoryButtons(categories) {
  categoryList.replaceChildren(createCategoryButton("All categories", ""));
  for (const category of categories) {
    categoryList.appendChild(createCategoryButton(category.name, category.id));
  }
}

// Remembers the chosen category, then reloads categories and listings.
function selectCategory(categoryId) {
  selectedCategoryId = categoryId;
  loadCategoriesAndListings();
}

// Creates the photo area of a card, using a placeholder when the listing has no photo.
function createCardPhoto(listing) {
  const cardPhoto = document.createElement("div");
  cardPhoto.className = "card-photo";
  if (listing.photoUrls.length > 0) {
    const photoImage = document.createElement("img");
    photoImage.src = listing.photoUrls[0];
    photoImage.alt = listing.title;
    cardPhoto.appendChild(photoImage);
  } else {
    cardPhoto.textContent = "📦";
  }
  return cardPhoto;
}

// Creates a paragraph element with the given class and text.
function createTextParagraph(className, text) {
  const paragraph = document.createElement("p");
  paragraph.className = className;
  paragraph.textContent = text;
  return paragraph;
}

// Creates the text area of a card: title, price, category and seller.
function createCardBody(listing) {
  const cardBody = document.createElement("div");
  cardBody.className = "card-body";
  const categoryName = listing.categoryName || "Uncategorized";
  cardBody.appendChild(createTextParagraph("card-title", listing.title));
  cardBody.appendChild(createTextParagraph("card-price", "$" + listing.price));
  cardBody.appendChild(createTextParagraph("card-meta", categoryName + " · " + listing.sellerName));
  return cardBody;
}

// Creates one clickable listing card that links to the detail page.
function createListingCard(listing) {
  const cardLink = document.createElement("a");
  cardLink.className = "card-link";
  cardLink.href = "listing.html?id=" + encodeURIComponent(listing.id);
  const card = document.createElement("div");
  card.className = "card";
  card.appendChild(createCardPhoto(listing));
  card.appendChild(createCardBody(listing));
  cardLink.appendChild(card);
  return cardLink;
}

// Shows the listing cards, the result count and the empty state.
function renderListingCards(listings) {
  listingCardsContainer.replaceChildren(...listings.map(createListingCard));
  resultsCount.textContent = "Showing " + listings.length + " listings";
  emptyState.classList.toggle("hidden", listings.length > 0);
}

// Loads categories and listings, showing an error banner if either request fails.
async function loadCategoriesAndListings() {
  hideErrorMessage();
  try {
    renderCategoryButtons(await fetchCategoriesFromApi());
    renderListingCards(await fetchListingsFromApi());
  } catch (loadError) {
    listingCardsContainer.replaceChildren();
    resultsCount.textContent = "";
    showErrorMessage(loadError.message);
  }
}

// Runs the search when the search box is submitted with Enter.
searchForm.addEventListener("submit", (submitEvent) => {
  submitEvent.preventDefault();
  loadCategoriesAndListings();
});

applyFiltersButton.addEventListener("click", loadCategoriesAndListings);

loadCategoriesAndListings();
