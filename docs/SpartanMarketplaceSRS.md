# Requirements – Spartan Marketplace

**Project Name:** Spartan Marketplace \
**Team:** Daryl Carter (Customer/SysAdmin), Jonathan Hardeman III (Provider/Vendor) \
**Course:** CSC 340\
**Version:** 1.0\
**Date:** 2026-09-17

---

## 1. Overview
**Vision.** Spartan Marketplace is a campus-only marketplace web app that gives students a safer, campus-scoped alternative to general marketplace apps and public group chats — students browse, list, and arrange in-person purchases with each other (no in-app payments) through built-in vendor vetting, moderation, and on-campus meetup safety features.

**Glossary**
- **Listing:** A one-off item a Customer sells directly; goes live only after manual Admin review.
- **Product:** A Vendor's ongoing, stocked catalog item with a live inventory count; published without per-item review unless the Vendor is restricted.
- **Vendor Application:** The form a Customer submits (business name, description, what they're selling, licenses held, desired selling location, team size, student ID) to be upgraded to a Vendor account.
- **Request to Buy:** A Customer action on a Product that places a temporary hold on stock immediately, pending Vendor Accept/Reject.
- **.edu Verification:** A confirmation code sent to a student's campus email; required before any Authenticated Action, independent of browsing.
- **Meetup Location:** An Admin-managed list of recommended on-campus pickup spots shown when a Customer arranges a purchase.
- **Review-Bombing Detection:** Automatic flag raised when a Vendor receives more than 10 bad reviews within 24 hours.
- **Serial Bad-Reviewer Detection:** Automatic Admin follow-up task created when a single user leaves more than 3 bad reviews within a week.

**Primary Users / Roles.**
- **Customer** — Browse the marketplace, buy from Vendors or sell one-off items, and leave reviews, with minimal friction and campus-verified trust.
- **Provider (Vendor)** — Run an ongoing small business on campus: publish stocked Products, track orders/stock, and see customer analytics.
- **SysAdmin** — Keep the marketplace trustworthy by vetting Vendors, moderating listings/reports, and monitoring platform health.

**Scope (this semester).**
- Google OAuth login gated by .edu email verification, with every account starting as a Customer.
- Public, no-login browse/search/detail view for Listings and Products.
- Customer one-off Individual Listings with manual Admin review before going live.
- Vendor Application flow (Customer → Vendor upgrade) with manual Admin approval.
- Vendor Product catalog management with live stock counts and Request-to-Buy hold/release.
- In-site 1:1 Vendor–Customer messaging for arranging pickup and replying to feedback.
- Reviews, reporting (listings, chats/transactions), and an Admin moderation/report queue.
- Automatic review-bombing and serial-bad-reviewer detection that flags/tasks Admins.
- Admin usage statistics dashboard (active users, queue volume, reports, most active categories/vendors).
- In-app and campus-email notifications for buy requests, listing status, and account actions.

**Out of scope (deferred).**
- In-app payment processing — all purchases are arranged and paid for face-to-face.
- Bank account linking (e.g., Plaid/Chime), vendor balance/withdraw, and refunds — there is no platform money to attach a bank to, hold, or refund.
- Recommendation/personalization algorithm — deferred, to be scoped once the team's scrum board is set up.
- Revenue/financial analytics — deferred until payments exist.
- Automated content moderation (banned words/images, auto-approval rules) — v1 review is manual only.

> This document is **requirements‑level** and solution‑neutral; design decisions (UI layouts, API endpoints, schemas) are documented separately.

---

## 2. Functional Requirements (User Stories)
Write each story as: **As a `<role>`, I want `<capability>`, so that `<benefit>`.** Each story includes at least one **Given/When/Then** scenario.

### 2.1 Customer Stories
- **US-1 — Create and manage a Customer profile**
  _Story:_ As a customer, I want my account to become a Customer automatically after signing in with Google and verifying my .edu email, so that I can start using the marketplace without a separate signup form.
  _Acceptance:_
  ```gherkin
  Scenario: First login creates a Customer account
    Given a student has never logged into Spartan Marketplace
    When  they complete Google OAuth login and enter the confirmation code sent to their campus email
    Then  a Customer account is created for them and they can edit their public Customer Info
  ```

- **US-2 — Browse and search listings/products without an account**
  _Story:_ As a customer (or visitor), I want to browse, filter, and sort marketplace listings without logging in, so that I can decide what's available before committing to sign up.
  _Acceptance:_
  ```gherkin
  Scenario: Anonymous visitor filters listings by category and price
    Given a visitor is on the Public Browse & Search page with no account
    When  they filter by a category and a price range and sort by Newest
    Then  matching listing/product cards are shown with no login prompt
  ```

- **US-3 — View full listing/product detail**
  _Story:_ As a customer, I want to open a full listing or product detail page, so that I can see photos, description, stock, and existing reviews before deciding to buy.
  _Acceptance:_
  ```gherkin
  Scenario: Viewing a product detail page
    Given a product exists in the marketplace
    When  a customer opens its detail view
    Then  they see its photos, description, current stock count, and existing reviews
  ```

- **US-4 — Submit a Request to Buy on a Vendor's product**
  _Story:_ As a customer, I want to submit a Request to Buy on a product, so that I can reserve it while the vendor decides whether to accept.
  _Acceptance:_
  ```gherkin
  Scenario: Stock is held immediately on request
    Given a customer is logged in and .edu-verified, and a product has available stock
    When  they submit a Request to Buy
    Then  the stock count is decremented immediately and the request is sent to the vendor for Accept/Reject

  Scenario: Vendor rejects the request
    Given a Request to Buy is pending vendor decision
    When  the vendor rejects it
    Then  the held stock is released back to the product's available count
  ```

- **US-5 — Create an Individual Listing**
  _Story:_ As a customer, I want to create a one-off Individual Listing for something I'm selling, so that other students can discover and buy it.
  _Acceptance:_
  ```gherkin
  Scenario: New listing awaits admin review
    Given a logged-in, .edu-verified customer submits a new Individual Listing
    When  the submission is saved
    Then  it enters the manual Admin review queue and is not publicly visible until approved
  ```

- **US-6 — Mark an Individual Listing as sold**
  _Story:_ As a customer, I want to manually mark my listing "Sold" once it's gone, so that other students stop trying to buy something no longer available.
  _Acceptance:_
  ```gherkin
  Scenario: Seller closes out a completed listing
    Given a customer's Individual Listing is live and has been sold in person
    When  the customer marks it "Sold" from their listings page
    Then  the listing no longer appears as available in Browse & Search results
  ```

- **US-7 — Write a review after an interaction**
  _Story:_ As a customer, I want to write a review after interacting with a vendor or completing a listing purchase, so that I can share feedback with other students.
  _Acceptance:_
  ```gherkin
  Scenario: Review submitted after a completed transaction
    Given a logged-in, .edu-verified customer has an interaction history with a vendor/listing
    When  they submit a review from the Market Place Actions flow
    Then  the review is attached to that vendor/listing and becomes visible on its detail page
  ```

- **US-8 — Choose a safe meetup location**
  _Story:_ As a customer, I want to pick a recommended on-campus meetup spot when arranging a purchase, so that I can meet the seller somewhere safe.
  _Acceptance:_
  ```gherkin
  Scenario: Customer selects "Other" meetup location
    Given a customer is arranging pickup for an accepted purchase
    When  they choose "Other" instead of an Admin-managed on-campus location
    Then  a liability warning is shown and they must check an acknowledgment box before proceeding
  ```

- **US-9 — Report a chat or transaction**
  _Story:_ As a customer, I want to report a suspicious chat or transaction, so that Admins can review it and take action if needed.
  _Acceptance:_
  ```gherkin
  Scenario: Reported item enters the admin queue
    Given a logged-in customer is viewing a chat or transaction
    When  they submit a report on it
    Then  it is added to the Admin review queue and linked to the reporting user's report history
  ```

### 2.2 Provider Stories
- **US-20 — Apply to become a Vendor**
  _Story:_ As a customer, I want to submit a Vendor Application with my business info, so that I can be considered for an upgrade to a Vendor account.
  _Acceptance:_
  ```gherkin
  Scenario: Application submitted for admin review
    Given a logged-in, .edu-verified customer fills out business name, description, what they're selling, licenses held, desired selling location, team size, and student ID
    When  they submit the Vendor Application
    Then  it enters the pending queue on the Admin Dashboard and the customer's account remains a Customer until a decision is made
  ```

- **US-21 — Manage Vendor Profile**
  _Story:_ As a provider, I want to edit my Vendor's public info, business description, and photos, so that customers browsing the marketplace can learn about my business before buying.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor updates their public profile
    Given an approved vendor is on their Vendor Profile page
    When  they update the business description and upload a new photo
    Then  the changes are immediately reflected on the vendor's public-facing profile
  ```

- **US-22 — Manage Vendor team access**
  _Story:_ As a provider, I want to add or remove team members on my Vendor account, so that more than one person can help run the business without sharing a single login.
  _Acceptance:_
  ```gherkin
  Scenario: Owner adds a team member
    Given a vendor account has an owner and no other members
    When  the owner adds another verified student as a team member
    Then  that student can access the Vendor Dashboard for that business
  ```

- **US-23 — Publish and manage a Product catalog**
  _Story:_ As a provider, I want to create and edit ongoing Products with a price, description, and live stock count, so that customers can browse and buy from my catalog without me re-listing items one at a time.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor updates stock on an existing product
    Given an approved vendor has a Product with available stock
    When  they edit the stock count or price from the Vendor Product page
    Then  the updated values are immediately reflected in Public Browse & Search
  ```

- **US-24 — Accept or reject a Request to Buy**
  _Story:_ As a provider, I want to accept or reject a customer's Request to Buy, so that I control what actually leaves my stock.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor accepts a pending request
    Given a customer's Request to Buy is pending on one of the vendor's products
    When  the vendor accepts it
    Then  in-site messaging opens between the vendor and customer to arrange pickup

  Scenario: Vendor rejects a pending request
    Given a customer's Request to Buy is pending on one of the vendor's products
    When  the vendor rejects it
    Then  the held stock is released back to the product's available count
  ```

- **US-25 — Confirm a completed sale**
  _Story:_ As a provider, I want to confirm that a sale was completed after meeting the buyer, so that the transaction is closed out and the buyer becomes eligible to leave a review.
  _Acceptance:_
  ```gherkin
  Scenario: Both sides confirm the sale
    Given a vendor and customer arranged a pickup through in-site messaging
    When  both the vendor and the customer confirm the sale was completed
    Then  the transaction is marked complete and a review can be submitted for it
  ```

- **US-26 — View Customer Analytics**
  _Story:_ As a provider, I want to see my transaction history, customer feedback, and report history in one place, so that I can understand how my business is doing and spot problems early.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor opens their analytics page
    Given an approved vendor has completed transactions and received reviews
    When  they open the Customer Analytics page on their Vendor Dashboard
    Then  they see their transaction history, customer feedback, and their own report history
  ```

- **US-27 — Message customers directly**
  _Story:_ As a provider, I want in-site 1:1 messaging with a customer, so that I can arrange pickup details and answer questions without sharing personal contact info.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor replies to a customer message
    Given a customer has sent the vendor a message about a product
    When  the vendor replies from the Vendor Messaging page
    Then  the customer sees the reply in the same conversation thread
  ```

### 2.3 SysAdmin Stories
- **US-30 — Review Vendor Applications**
  _Story:_ As a sysadmin, I want to review pending Vendor Applications, so that I can approve legitimate businesses and keep untrustworthy sellers off the platform.
  _Acceptance:_
  ```gherkin
  Scenario: Admin approves a pending application
    Given a Vendor Application is pending in the Admin Dashboard
    When  the admin reviews it and clicks Approve
    Then  the applicant's account is upgraded to Vendor and they gain access to the Vendor Dashboard
  ```

- **US-31 — Restrict or ban an account**
  _Story:_ As a sysadmin, I want to restrict or ban a vendor or customer account, so that I can stop policy-violating behavior on the platform.
  _Acceptance:_
  ```gherkin
  Scenario: Admin bans a user after repeated reports
    Given a user has multiple resolved reports against them in their report history
    When  the admin bans the account from Manage User Access
    Then  the user can no longer log in to perform Authenticated Actions, and they receive an Account Restricted/Banned notification
  ```

- **US-32 — Moderate new Individual Listings**
  _Story:_ As a sysadmin, I want to manually review new Individual Listings before they go live, so that inappropriate or fraudulent listings never reach the public marketplace.
  _Acceptance:_
  ```gherkin
  Scenario: Admin approves a queued listing
    Given a customer's Individual Listing is sitting in the manual review queue
    When  the admin approves it
    Then  the listing becomes publicly visible in Browse & Search and the customer receives a Listing Approved notification
  ```

- **US-33 — Temporarily hide a Vendor's listings**
  _Story:_ As a sysadmin, I want to temporarily hide a vendor's products, so that I can act quickly on a suspected issue without permanently removing the vendor.
  _Acceptance:_
  ```gherkin
  Scenario: Admin hides a vendor's catalog
    Given a vendor's products are currently live
    When  the admin manually hides that vendor's listings
    Then  the products no longer appear in Browse & Search until the admin un-hides them
  ```

- **US-34 — Moderate reported chats and transactions**
  _Story:_ As a sysadmin, I want a review queue for reported chats/transactions with full report history per user, so that I can make informed moderation decisions rather than reacting to isolated reports.
  _Acceptance:_
  ```gherkin
  Scenario: Admin reviews a reported chat
    Given a customer has reported a chat, and that user has prior reports on file
    When  the admin opens the report from the moderation queue
    Then  they can see the full report history for both parties involved before deciding on an action
  ```

- **US-35 — Automatic review-bombing detection**
  _Story:_ As a sysadmin, I want the system to auto-flag a vendor who receives an unusual spike of bad reviews, so that I can investigate potential review-bombing without having to monitor every vendor manually.
  _Acceptance:_
  ```gherkin
  Scenario: Vendor crosses the review-bombing threshold
    Given a vendor has received 9 bad reviews in the last 24 hours
    When  an 11th bad review is submitted within that same 24-hour window
    Then  the vendor is auto-flagged for investigation in the Admin moderation queue
  ```

- **US-36 — Automatic serial bad-reviewer detection**
  _Story:_ As a sysadmin, I want the system to auto-create a follow-up task when one user leaves too many bad reviews in a short period, so that I can investigate patterns of abusive reviewing.
  _Acceptance:_
  ```gherkin
  Scenario: User crosses the serial bad-reviewer threshold
    Given a customer has left 3 bad reviews within the past 7 days
    When  they submit a 4th bad review within that same 7-day window
    Then  an admin follow-up task is automatically created for that user
  ```

- **US-37 — View platform usage statistics**
  _Story:_ As a sysadmin, I want a usage statistics dashboard, so that I can monitor platform health and growth over time.
  _Acceptance:_
  ```gherkin
  Scenario: Admin opens the usage statistics view
    Given the platform has active users, pending review-queue items, and resolved reports
    When  the admin opens Admin Usage Statistics
    Then  they see active users/new signups over time, review-queue volume, reports received and resolutions, and the most active categories/vendors
  ```

---

## 3. Non‑Functional Requirements (make them measurable)
- **Performance:** Public Browse & Search results (filter/sort) return in under 2 seconds for a catalog of up to 5,000 active listings/products.
- **Availability/Reliability:** The marketplace (public browse and authenticated actions) targets 99% uptime during the semester; a Request to Buy's stock hold/release must never leave stock in a permanently inconsistent state (no double-holds, no lost releases).
- **Security/Privacy:** All write/action endpoints (review, Request to Buy, message, save, create listing, report) require an authenticated, .edu-verified session; read access to listing/product browse and detail pages requires no authentication. Only the confirmation code (not full credentials) is transmitted to campus email for .edu verification.
- **Usability:** A first-time visitor can find and open a listing detail page from the homepage in 3 clicks or fewer, with no login required.

---

## 4. Assumptions, Constraints, and Policies
- No money moves through the platform; every purchase is arranged in-app but paid for face-to-face, so there is no payment, escrow, or refund policy to enforce.
- Every account starts as a Customer; Vendor status is only granted through an approved Vendor Application — there is no self-service upgrade.
- All moderation (Individual Listing review, report/chat review, vendor restriction) is manual in v1; no automated content moderation exists yet.
- Login is limited to Google OAuth, and any Authenticated Action additionally requires .edu email verification, regardless of browsing access.
- Off-campus meetups are allowed but require the customer to acknowledge a liability warning first; the platform is not liable for off-campus incidents.
- Review-bombing (>10 bad reviews on one vendor within 24 hours) and serial bad-reviewing (>3 bad reviews from one user within a week) are the only automatically detected abuse patterns in v1; all other moderation is admin-initiated.

---

## 5. Milestones (course‑aligned)
- **M1 Requirements** — this file + stories opened as issues. 
- **M2 High‑fidelity prototype** — core customer/provider flows fully interactive. 
- **M3 Design** — architecture, schema, API outline. 
- **M4 Backend API** — key endpoints + tests. 
- **M5 Increment** — ≥2 use cases end‑to‑end. 
- **M6 Final** — complete system & documentation. 

---

## 6. Change Management
- Stories are living artifacts; changes are tracked via repository issues and linked pull requests.  
- Major changes should update this SRS.
