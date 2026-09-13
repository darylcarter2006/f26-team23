## Title
Spartan MarketPlace

##Team Members
Jonathan Hardeman

Daryl Carter

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

Access Model (Facebook Marketplace pattern):
    Public Browse (no login required):
        Anyone landing on the site can search, filter by category, sort, and view listing/product cards (title, price, condition/location) with no account.
        Viewing a full listing/product detail page (photos, description, stock, existing reviews) is also public.
    Authenticated Actions (require Login + .edu Verification - see Signup & Eligibility below):
        Messaging a vendor/seller, submitting a Request to Buy, writing a review, saving/favoriting a listing, viewing "My Listings" or "Messages", creating an Individual Listing, applying to become a Vendor, and reporting a chat/transaction all require a logged-in, .edu-verified account.
    In short: browsing is open to everyone, acting on something requires signing in first.

Signup & Eligibility:
    Google OAuth Login (open to any Google account)
    .edu Email Verification:
        Confirmation code sent to the student's campus email
        Must be verified before any Authenticated Action is available (see Access Model above) - browsing itself never requires this
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
    Public Browse (no login required, see Access Model above):
        Student Marketplace landing page - Sign Up / Log In buttons visible but browsing does not require either
        Search & Discovery:
            Browse by Category
            Filter by Price Range
            Sort (e.g. Newest)
        View Listing/Product Cards (title, price, condition/location summary)
        View Full Listing/Product Detail:
            Photos
            Description
            Stock (Products only)
            Existing Reviews
    Google oauth Login (required for everything below - see Access Model above):
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
            Market Place (Authenticated Actions only - browsing the Market Place itself is public, see Access Model above):
                Write Reviews
                Request to Buy (Products only):
                    Places a temporary hold on stock (decrements immediately, before vendor responds)
                    Vendor Accepts or Rejects
                    Accept -> proceeds to in-site messaging to arrange pickup
                    Reject -> held stock released back
            Saved Items: (seen in the navigation mockup - not yet discussed in detail, confirm scope: a simple favorites/wishlist list of listings)
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
    Listing/Product read access (browse + detail view) is public - no auth check required on these GETs. All write/action endpoints (review, request-to-buy, message, save, create listing, report) require an authenticated, .edu-verified session.
