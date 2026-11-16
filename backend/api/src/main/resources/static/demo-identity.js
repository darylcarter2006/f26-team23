// Demo identities for the vendor pages. Real login is not built yet, so the pages act as these users.

// The demo admin user in this database (admin@example.edu). Used as the X-Admin-Id header on admin actions.
const DEMO_ADMIN_USER_ID = "14ab8673-ff56-4c6f-8c43-12d0703adfed";

// The verified customer who applies to become a vendor (vendor1@example.edu).
// Open vendor-apply.html?userId=<id> to apply as a different verified user.
const DEFAULT_APPLICANT_USER_ID = "16ae7048-aa02-45a0-a9a2-43768ab5697c";

// Returns the applicant's user id from the ?userId= query parameter, or the default demo applicant.
function getApplicantUserId() {
  const queryUserId = new URLSearchParams(window.location.search).get("userId");
  return queryUserId ? queryUserId.trim() : DEFAULT_APPLICANT_USER_ID;
}
