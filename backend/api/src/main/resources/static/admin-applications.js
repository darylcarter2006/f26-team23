// Admin vendor applications page: lists applications and lets the admin approve or reject pending ones.

const statusFilterSelect = document.getElementById("statusFilterSelect");
const applicationsTable = document.getElementById("applicationsTable");
const applicationsTableBody = document.getElementById("applicationsTableBody");
const applicationCount = document.getElementById("applicationCount");
const emptyState = document.getElementById("emptyState");
const errorBanner = document.getElementById("errorBanner");
const successBanner = document.getElementById("successBanner");

// Shows an error message above the table and hides any success message.
function showError(errorMessage) {
  errorBanner.textContent = errorMessage;
  errorBanner.classList.remove("hidden");
  successBanner.classList.add("hidden");
}

// Shows a success message above the table and hides any error message.
function showSuccess(successMessage) {
  successBanner.textContent = successMessage;
  successBanner.classList.remove("hidden");
  errorBanner.classList.add("hidden");
}

// Builds the request options for an admin call, sending the demo admin's id in the X-Admin-Id header.
function buildAdminRequestOptions(httpMethod) {
  return { method: httpMethod, headers: { "X-Admin-Id": DEMO_ADMIN_USER_ID } };
}

// Formats an ISO timestamp from the API as a short local date, or "-" when missing.
function formatSubmittedDate(isoTimestamp) {
  if (!isoTimestamp) {
    return "-";
  }
  return new Date(isoTimestamp).toLocaleDateString();
}

// Creates a table cell containing plain text.
function createTextCell(cellText) {
  const tableCell = document.createElement("td");
  tableCell.textContent = cellText || "-";
  return tableCell;
}

// Creates the colored status chip for an application status like PENDING.
function createStatusChip(applicationStatus) {
  const statusChip = document.createElement("span");
  statusChip.className = "status-chip " + applicationStatus.toLowerCase();
  statusChip.textContent = applicationStatus.charAt(0) + applicationStatus.slice(1).toLowerCase();
  return statusChip;
}

// Creates one small action button that decides an application when clicked.
function createDecisionButton(buttonLabel, buttonClass, application, decision) {
  const decisionButton = document.createElement("button");
  decisionButton.type = "button";
  decisionButton.className = "mini-btn " + buttonClass;
  decisionButton.textContent = buttonLabel;
  decisionButton.addEventListener("click", () => decideApplication(application, decision, decisionButton));
  return decisionButton;
}

// Builds the table row for one application.
function createApplicationRow(application) {
  const applicationRow = document.createElement("tr");

  const businessCell = document.createElement("td");
  const businessNameText = document.createElement("strong");
  businessNameText.textContent = application.businessName;
  businessCell.appendChild(businessNameText);
  if (application.businessDescription) {
    const descriptionText = document.createElement("div");
    descriptionText.className = "muted";
    descriptionText.textContent = application.businessDescription;
    businessCell.appendChild(descriptionText);
  }
  applicationRow.appendChild(businessCell);

  applicationRow.appendChild(createTextCell(application.whatSelling));
  applicationRow.appendChild(createTextCell(application.teamSize));
  applicationRow.appendChild(createTextCell(application.studentId));
  applicationRow.appendChild(createTextCell(formatSubmittedDate(application.createdAt)));

  const statusCell = document.createElement("td");
  statusCell.appendChild(createStatusChip(application.status));
  applicationRow.appendChild(statusCell);

  const actionsCell = document.createElement("td");
  actionsCell.className = "actions-cell";
  if (application.status === "PENDING") {
    const actionButtons = document.createElement("div");
    actionButtons.className = "row-actions";
    actionButtons.appendChild(createDecisionButton("Approve", "approve", application, "approve"));
    actionButtons.appendChild(createDecisionButton("Reject", "reject", application, "reject"));
    actionsCell.appendChild(actionButtons);
  }
  applicationRow.appendChild(actionsCell);

  return applicationRow;
}

// Draws the list of applications, or the empty message when there are none.
function renderApplications(applications) {
  applicationsTableBody.replaceChildren(...applications.map(createApplicationRow));
  applicationCount.textContent = applications.length + (applications.length === 1 ? " application" : " applications");
  const hasApplications = applications.length > 0;
  applicationsTable.classList.toggle("hidden", !hasApplications);
  emptyState.classList.toggle("hidden", hasApplications);
}

// Loads applications for the selected status from the API and draws them.
async function loadApplications() {
  const selectedStatus = statusFilterSelect.value;
  const requestPath = "/api/vendor-applications" + (selectedStatus ? "?status=" + encodeURIComponent(selectedStatus) : "");
  try {
    const applications = await sendApiRequest(requestPath, buildAdminRequestOptions("GET"));
    renderApplications(applications);
  } catch (loadError) {
    renderApplications([]);
    showError(loadError.message);
  }
}

// Approves or rejects one application, then reloads the list from the API.
async function decideApplication(application, decision, clickedButton) {
  clickedButton.disabled = true;
  try {
    await sendApiRequest(
      "/api/vendor-applications/" + encodeURIComponent(application.id) + "/" + decision,
      buildAdminRequestOptions("PUT")
    );
    const decisionPastTense = decision === "approve" ? "approved" : "rejected";
    showSuccess("\"" + application.businessName + "\" was " + decisionPastTense + ".");
  } catch (decisionError) {
    showError(decisionError.message);
  }
  await loadApplications();
}

statusFilterSelect.addEventListener("change", () => {
  errorBanner.classList.add("hidden");
  successBanner.classList.add("hidden");
  loadApplications();
});
loadApplications();
