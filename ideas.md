Put all of our ideas here.

Any features that each of us want to implement put here including benifits and limitations.

Database: Supabase (postgreSQL)
Backend: Java, JavaAPi
Frontend: Javascript
Hosted: Vercel


Idea:
    Create a marketplace for college campuses

Scope Decisions (v1):
    No in-app payment processing - all purchases are physical/in-person transactions arranged through in-site messaging. No money ever moves through the platform.
    No bank account linking (Plaid/Chime), no vendor balance/withdraw, no refunds - cut, since there is no platform money to attach a bank to, hold, or refund.
    Recommendation/personalization algorithm - deferred, owned by Daryl C., to be scoped once the scrum board is set up.
    Revenue/financial analytics - deferred, out of scope until payments exist.
    Automated content moderation (banned words/images, auto-approval rules) - deferred, v1 review is manual.

Signup & Eligibility:
    Google OAuth Login (open to any Google account)
    .edu Email Verification:
        Confirmation code sent to the student's campus email
        Must be verified before full marketplace access is granted
    Every account starts as a Customer. Vendor is an upgraded role, unlocked via application + approval (see Vendor Application below).

Vendor Application:
    Business Name
    Business Description
    What They're Selling
    Licenses Held
    Desired Selling Location
    Team Size
    Student ID
    Reviewed manually by Admin via the Admin Dashboard (Manage Vendor Access)
    Once approved, Vendor can publish Products with no per-listing review (unless restricted by Admin)

Home Page:
    Google oauth Login:
        Vendor:
            Profile:
                Vendor Public Info
                Business Description
                Photos
                Access
            Product: (ongoing vendor inventory - separate entity from Customer Listings, see Data Model Notes)
                Photos:
                    Captions
                Edit Prices
                Manage Stock (live stock count, decremented via Request to Buy - see Market Place below)
                Edit Description
                View Public Reviews
            Customer Analytics:
                Transaction History
                Customer Feedback
                Report History (this vendor's full report history, visible to Admin)
            Messaging:
                In-site 1:1 chat with customers (this is also what "Vendor-Customer Forum" referred to - one messaging system, not two)
        Customer:
            Customer Info:
                Vendor Visible Info
            Individual Listings: (one-off sale - separate entity from Vendor Products, see Data Model Notes)
                Create Listing -> Manual Admin Review Queue -> Approved -> Live in Marketplace
                Seller manually marks Listing "Sold" when it's gone
            Previous Interactions:
                Vendor/Seller Info
                Product/Listing Info
            Market Place:
                Search & Discovery:
                    Browse by Category
                    Filter by Price Range
                Products / Listings:
                    View Photos
                    View Prices
                    View Description
                    View Stock (Products only)
                    View Reviews
                    Write Reviews
                    Request to Buy (Products only):
                        Places a temporary hold on stock (decrements immediately, before vendor responds)
                        Vendor Accepts or Rejects
                        Accept -> proceeds to in-site messaging to arrange pickup
                        Reject -> held stock released back
            Safety & Meetup Location:
                Recommended On-Campus Meetup Spot (dropdown, Admin-managed list)
                "Other" option:
                    Liability warning shown (off-campus transactions not recommended, platform not liable for incidents)
                    Requires checkbox acknowledgment before proceeding
            Report:
                Report a chat/transaction -> feeds into Admin review queue
        Admin:
            Manage Vendor Access:
                Review Vendor Applications (via Admin Dashboard)
                Restrict/Ban Vendor
                Direct Vendor Communication
            Manage User Access:
                Edit user preference algorithm (deferred - see Scope Decisions)
                Restrict/Ban User
                Direct User Communication
            Manage Recommended Meetup Locations:
                Add/Remove on-campus locations shown in the meetup dropdown
            Review/Chat Moderation:
                Manual review queue for Listings and Reports (v1)
                View a user's/vendor's full report history (not isolated single reports)
                Review-Bombing Detection: more than 10 bad reviews on one vendor within a 24-hour period -> auto-flag for investigation
                Serial Bad-Reviewer Detection: more than 3 bad reviews from one user within a week -> auto-task Admin for follow-up
                Temporarily Hide Listings (manual action only, not automatic for now)
            Usage Statistics: (health/growth focus, not revenue)
                Active Users / New Signups over time
                Review Queue Volume (pending Listings, pending Vendor Applications)
                Reports Received + Resolutions
                Most Active Categories / Vendors

Notifications:
    In-App (all of the below):
        New Buy Request Received
        Buy Request Accepted/Rejected
        New Chat Message
        Listing Approved/Rejected
        Account Restricted/Banned
    Email (sent to student's campus email; all except New Chat Message):
        New Buy Request Received
        Buy Request Accepted/Rejected
        Listing Approved/Rejected
        Account Restricted/Banned

Data Model Notes:
    User: single account, role = Customer (default) or Vendor (upgraded via approved application)
    Listing: individual one-off customer sale (separate table from Product)
    Product: vendor's ongoing stocked item (separate table from Listing)
