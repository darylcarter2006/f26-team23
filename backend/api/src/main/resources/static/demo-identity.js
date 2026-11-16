// Demo identities for the vendor pages. Real login is not built yet, so the pages act as these users.

// The demo admin created by demo-requests.md. Used as the X-Admin-Id header on admin actions.
const DEMO_ADMIN_USER_ID = "827ad0b6-ae12-4a9a-b0f3-43ac40664e4b";

// The verified customer who applies to become a vendor. This is the demo customer from demo-requests.md;
// replace it with your own verified user's id, or open vendor-apply.html?userId=<id> to try another user.
const DEFAULT_APPLICANT_USER_ID = "dd4ef416-7758-40ad-a106-d2a3fb4d9cc3";

// Returns the applicant's user id from the ?userId= query parameter, or the default demo applicant.
function getApplicantUserId() {
  const queryUserId = new URLSearchParams(window.location.search).get("userId");
  return queryUserId ? queryUserId.trim() : DEFAULT_APPLICANT_USER_ID;
}
