# Demo setup requests

> **Note:** the admin user, the demo customer (id `dd4ef416-7758-40ad-a106-d2a3fb4d9cc3`), 4 categories and 6 listings (5 LIVE, 1 PENDING_REVIEW) already exist in the database, so steps 1-3 only need to be run on a fresh database (repeating them returns a 409). Admin id for the `X-Admin-Id` header: `827ad0b6-ae12-4a9a-b0f3-43ac40664e4b`.

Run these once, in order, from your API client against `http://localhost:8080`.
Set `ADMIN_EMAILS` to the admin email **before** starting the app and before step 1.
Replace each `<PLACEHOLDER>` with the `id` returned by an earlier call.

## 1. Create the admin user

`POST /api/users`
```json
{ "email": "admin@example.edu", "displayName": "Demo Admin" }
```
Save the returned `id` as `<ADMIN_USER_ID>`.

## 2. Create and verify the customer

`POST /api/users`
```json
{ "email": "customer@example.edu", "displayName": "Demo Customer" }
```
Save the returned `id` as `<CUSTOMER_USER_ID>` and paste it into `DEMO_SELLER_USER_ID` in `static/create-listing.js`.

`POST /api/users/<CUSTOMER_USER_ID>/verify-edu` (no body)

## 3. Create categories

Header on each call: `X-Admin-Id: <ADMIN_USER_ID>`

`POST /api/categories`
```json
{ "name": "Textbooks" }
```
`POST /api/categories`
```json
{ "name": "Electronics" }
```
`POST /api/categories`
```json
{ "name": "Furniture" }
```
Save the returned ids as `<TEXTBOOKS_CATEGORY_ID>`, `<ELECTRONICS_CATEGORY_ID>`, `<FURNITURE_CATEGORY_ID>`.

## 4. Create listings and approve them

`POST /api/listings` (repeat with different bodies)
```json
{
  "sellerId": "<CUSTOMER_USER_ID>",
  "title": "Calculus Textbook",
  "description": "9th edition, good condition",
  "price": 40,
  "categoryId": "<TEXTBOOKS_CATEGORY_ID>",
  "photoUrls": []
}
```
```json
{
  "sellerId": "<CUSTOMER_USER_ID>",
  "title": "Desk Lamp",
  "description": "Works great",
  "price": 12,
  "categoryId": "<FURNITURE_CATEGORY_ID>",
  "photoUrls": []
}
```
```json
{
  "sellerId": "<CUSTOMER_USER_ID>",
  "title": "TI-84 Calculator",
  "description": "Like new",
  "price": 65,
  "categoryId": "<ELECTRONICS_CATEGORY_ID>",
  "photoUrls": []
}
```
Approve all but one so it stays pending. Header: `X-Admin-Id: <ADMIN_USER_ID>`

`PUT /api/listings/<LISTING_ID>/approve` (no body)
