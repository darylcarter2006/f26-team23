# Spartan Marketplace — Backend

Java/Spring Boot backend for the Vendor and Admin domains. Plain Maven
project, Spring Web + Spring Data JDBC, connects directly to the Supabase
Postgres database over JDBC.

Nothing here talks to `frontend/` yet — this is being built and tested
standalone with a REST client (curl, Postman, etc.) before it's ever wired
up to the UI.

## Running it locally against your own Postgres

This is the fastest loop while building — no Supabase credentials needed.

1. Install Postgres locally if you don't have it, and make sure a `postgres`
   role with password `postgres` can log in over TCP (that's what
   `application.yml` defaults to). If your local install uses a different
   password, either change it to match or override `DB_PASSWORD` (see below).
2. Create the database and load the schema:
   ```
   createdb spartan_marketplace
   psql -d spartan_marketplace -f ../database/schema.sql
   ```
3. Seed one test vendor, so there's something to hit — there's no
   application-approval flow built yet, so this stands in for it:
   ```sql
   insert into users (id, email, auth_id)
     values ('11111111-1111-1111-1111-111111111111', 'jordan@uncg.edu', 'test|jordan');

   insert into vendors (id, user_id, business_name, business_description, photos)
     values (
       '22222222-2222-2222-2222-222222222222',
       '11111111-1111-1111-1111-111111111111',
       'Spartan Tech Exchange',
       'Refurbished electronics and study gear for campus.',
       array['https://example.com/storefront1.jpg']
     );
   ```
4. Run it:
   ```
   mvn spring-boot:run
   ```
5. Try it:
   ```
   curl http://localhost:8080/api/vendors/22222222-2222-2222-2222-222222222222

   curl -X PATCH http://localhost:8080/api/vendors/22222222-2222-2222-2222-222222222222 \
     -H "Content-Type: application/json" \
     -d '{"businessName":"Spartan Tech Exchange","businessDescription":"Updated description","photos":["https://example.com/new.jpg"]}'
   ```

I verified this exact flow (schema load, seed insert, TCP+password login as
the app's default config expects) against a local Postgres instance before
handing this off — that part's confirmed working. What I could **not**
verify here: whether the Java itself actually compiles. This sandbox's
network policy blocks Maven Central, so `mvn compile` couldn't pull the
Spring Boot dependencies. Run `mvn compile` (or just open the project in
your IDE) as the very first thing — if anything doesn't build, let me know
and I'll fix it before you spend time debugging what might just be a typo.

## Pointing at the real Supabase database instead

Set these as environment variables rather than editing `application.yml`
(so real credentials never get committed):

```
DB_URL=jdbc:postgresql://<your-supabase-host>:5432/postgres
DB_USER=<the service_role Postgres user>
DB_PASSWORD=<the service_role password>
```

Both are on the Supabase dashboard under Project Settings > Database. Note
the database name there is `postgres` by default, not `spartan_marketplace`
— that name is only for the local dev default above.

## What's built so far

- `GET /api/vendors/{id}` — fetch a vendor's profile
- `PATCH /api/vendors/{id}` — update business name, description, photos

No auth/ownership check yet (anyone who knows a vendor's UUID can read or
edit it right now) — that depends on real login existing, which isn't built.
Flagged in the code; needs to be locked down before this ever talks to the
real frontend.

## Suggested next slices, in order

1. Vendor Products — CRUD on `products`/`product_photos` (backlog stories 35, 42-44, 54)
2. Vendor Buy Requests — list pending, accept, reject (the new "Vendor Buy
   Requests Page" story)
3. Vendor Analytics — read-only aggregation queries (stories 36, 45-47)
4. Vendor Messaging — the thread-creation logic is more fiddly (see story 37's
   "exactly one conversation per pair" constraint), so it's last
