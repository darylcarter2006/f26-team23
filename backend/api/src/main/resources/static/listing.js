// Listing detail page: loads one listing by the id in the query string and shows it.

const listingDetailPanel = document.getElementById("listingDetailPanel");

// Reads the listing id from the page address, or returns null when it is missing.
function readListingIdFromQueryString() {
  return new URLSearchParams(window.location.search).get("id");
}

// Creates a detail line such as "Price: $40".
function createDetailRow(label, value) {
  const detailRow = document.createElement("p");
  detailRow.className = "detail-row";
  detailRow.textContent = label + ": " + value;
  return detailRow;
}

// Shows all details of the listing in the panel.
function renderListingDetails(listing) {
  const listingTitle = document.createElement("h1");
  listingTitle.textContent = listing.title;
  listingDetailPanel.replaceChildren(listingTitle);

  if (listing.photoUrls.length > 0) {
    const listingPhoto = document.createElement("img");
    listingPhoto.className = "detail-photo";
    listingPhoto.src = listing.photoUrls[0];
    listingPhoto.alt = listing.title;
    listingDetailPanel.appendChild(listingPhoto);
  }

  listingDetailPanel.appendChild(createDetailRow("Description", listing.description || "No description"));
  listingDetailPanel.appendChild(createDetailRow("Price", "$" + listing.price));
  listingDetailPanel.appendChild(createDetailRow("Category", listing.categoryName || "Uncategorized"));
  listingDetailPanel.appendChild(createDetailRow("Seller", listing.sellerName));
  listingDetailPanel.appendChild(createDetailRow("Status", listing.status));
}

// Shows a "Listing not found" message with a link back to the home page.
function renderListingNotFound() {
  const notFoundMessage = document.createElement("p");
  notFoundMessage.className = "error-banner";
  notFoundMessage.textContent = "Listing not found.";
  const homeLink = document.createElement("a");
  homeLink.href = "index.html";
  homeLink.textContent = "Back to home";
  listingDetailPanel.replaceChildren(notFoundMessage, homeLink);
}

// Loads the listing from the API, showing the not-found message on any failure.
async function loadListing() {
  const listingId = readListingIdFromQueryString();
  if (!listingId) {
    renderListingNotFound();
    return;
  }
  try {
    renderListingDetails(await sendApiRequest("/api/listings/" + encodeURIComponent(listingId)));
  } catch (loadError) {
    renderListingNotFound();
  }
}

loadListing();
