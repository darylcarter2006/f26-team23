// Vendor application page: submits a new vendor application to the API.

const vendorApplicationForm = document.getElementById("vendorApplicationForm");
const businessNameInput = document.getElementById("businessNameInput");
const businessDescriptionInput = document.getElementById("businessDescriptionInput");
const whatSellingInput = document.getElementById("whatSellingInput");
const licensesHeldInput = document.getElementById("licensesHeldInput");
const desiredLocationInput = document.getElementById("desiredLocationInput");
const teamSizeInput = document.getElementById("teamSizeInput");
const studentIdInput = document.getElementById("studentIdInput");
const submitApplicationButton = document.getElementById("submitApplicationButton");
const formErrorBanner = document.getElementById("formErrorBanner");
const formSuccessBanner = document.getElementById("formSuccessBanner");

// Shows an error message above the form and hides any success message.
function showFormError(errorMessage) {
  formErrorBanner.textContent = errorMessage;
  formErrorBanner.classList.remove("hidden");
  formSuccessBanner.classList.add("hidden");
}

// Shows the success message and hides any error message.
function showFormSuccess(createdApplication) {
  formSuccessBanner.textContent =
    "Application for \"" + createdApplication.businessName + "\" submitted. Status: " +
    createdApplication.status + ". An admin will review it soon.";
  formSuccessBanner.classList.remove("hidden");
  formErrorBanner.classList.add("hidden");
}

// Returns a message describing what is wrong with the form, or null when it is valid.
function findFormProblem() {
  if (businessNameInput.value.trim() === "") {
    return "Please enter your business name.";
  }
  if (whatSellingInput.value.trim() === "") {
    return "Please describe what you are selling.";
  }
  if (studentIdInput.value.trim() === "") {
    return "Please enter your student ID.";
  }
  if (teamSizeInput.value !== "" && (!Number.isInteger(Number(teamSizeInput.value)) || Number(teamSizeInput.value) < 1)) {
    return "Team size must be a whole number of at least 1.";
  }
  return null;
}

// Builds the JSON body for the application from the form fields.
function buildApplicationRequestBody() {
  return {
    userId: getApplicantUserId(),
    businessName: businessNameInput.value.trim(),
    businessDescription: businessDescriptionInput.value.trim(),
    whatSelling: whatSellingInput.value.trim(),
    licensesHeld: licensesHeldInput.value.trim(),
    desiredSellingLocation: desiredLocationInput.value.trim(),
    teamSize: teamSizeInput.value === "" ? null : Number(teamSizeInput.value),
    studentId: studentIdInput.value.trim(),
  };
}

// Sends the application to the API, then shows the success message or the server's error.
async function submitApplication(submitEvent) {
  submitEvent.preventDefault();
  const formProblem = findFormProblem();
  if (formProblem) {
    showFormError(formProblem);
    return;
  }

  submitApplicationButton.disabled = true;
  try {
    const createdApplication = await sendApiRequest("/api/vendor-applications", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(buildApplicationRequestBody()),
    });
    showFormSuccess(createdApplication);
    vendorApplicationForm.reset();
  } catch (submitError) {
    showFormError(submitError.message);
  } finally {
    submitApplicationButton.disabled = false;
  }
}

vendorApplicationForm.addEventListener("submit", submitApplication);
