## Title
> Spartan Marketplace

## Team Members
> Daryl Carter

> Jonathan Hardeman III

## Description 
> Spartan Marketplace is a campus-only marketplace web app where students can buy and sell with each other in person, with no in-app payment processing — all purchases are arranged and paid for face-to-face, coordinated through in-site messaging. The motivation is to give students a safer, campus-scoped alternative to posting on general marketplace apps or public group chats, with built-in vendor vetting, moderation, and on-campus meetup safety features. The app solves two related problems: (1) students with a one-off item to sell have nowhere trustworthy and campus-specific to list it, and (2) students running an ongoing small business (e.g. a recurring service or product) have no lightweight way to reach other students with proper stock tracking and analytics. Anyone can browse and search listings/products with no account; logging in with a Google account verified against a campus .edu email is required only to take an action (message a seller, buy, list, review, or apply to become a vendor).
>

## App Functions
1. Customer (the user with the customer role):
    1. Create/modify customer profile - Every account starts as a Customer automatically after Google OAuth login + .edu email verification (a confirmation code sent to the student's campus email); customers manage their basic public info (shown to vendors/sellers they interact with) from their Customer Info page.
    2. View available services - Customers (and any visitor, logged in or not) can browse and search the Public Browse & Search page: filter by category and price range, sort results (e.g. Newest), and open a full listing/product detail view (photos, description, stock, existing reviews) with no account required.
    3. Subscribe to available services - There are no recurring subscriptions; instead, a customer submits a "Request to Buy" on a vendor's product, which places a temporary hold on stock (decremented immediately, before the vendor responds) until the vendor accepts (moving to in-site messaging to arrange pickup) or rejects (releasing the held stock back). Customers can also create their own one-off Individual Listings, which go live after a manual admin review.
    4. Write reviews for subscribed services - After interacting with a vendor/product or completed listing, a logged-in, .edu-verified customer can write a review from the Market Place Actions flow; a customer can also report a chat or transaction, which feeds into the admin review queue.
2. Provider (the user with the provider role):
    1. Create/modify/remove provider profile - A customer applies to become a Vendor by submitting a Vendor Application (business name, description, what they're selling, licenses held, desired selling location, team size, student ID) for manual admin review; once approved, they manage their public Vendor Profile (public info, business description, photos, and team Access for adding people to the business account) from the Vendor Dashboard.
    2. Create services - Approved vendors publish Products (with photos, description, price, and a live stock count) from the Vendor Product page with no per-listing review required, unless the vendor account has been restricted by an admin.
    3. View customer statistics - The Vendor Customer Analytics page shows the vendor's transaction history, customer feedback, and their own full report history (also visible to admins).
    4. Reply to reviews - Vendors respond to customer feedback through the existing Vendor-to-Customer in-site 1:1 messaging system; a dedicated "reply directly under a review" feature is not yet scoped as its own story (currently a gap flagged in the backlog).
3. SysAdmin (the user with the admin role if applicable):
    1. Manage user access - From the Admin Dashboard, admins review and approve/reject pending Vendor Applications, restrict or ban a Vendor or Customer account, and message a vendor or user directly (Manage Vendor Access / Manage User Access).
    2. Moderate services - New Individual Listings sit in a manual admin review queue before going live, and admins can temporarily hide a vendor's listings/products at any time as a manual (non-automatic) action.
    3. Moderate reviews - Admins get a manual review queue for reported chats/transactions and can see a user's or vendor's full report history (not just isolated reports); the system auto-flags a vendor for investigation after more than 10 bad reviews within 24 hours (review-bombing) and auto-tasks an admin to follow up after a single user leaves more than 3 bad reviews within a week (serial bad-reviewer pattern).
    4. View usage statistics - The Admin Usage Statistics view tracks platform health/growth: active users and new signups over time, review-queue volume (pending listings and pending vendor applications), reports received and how they were resolved, and the most active categories/vendors.
