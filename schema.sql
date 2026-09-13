-- ============================================================================
-- Spartan Marketplace — Core schema (v1)
-- Covers: Core Data Model, Vendor Experience, Admin Experience domains
-- Target: Supabase (Postgres)
-- ============================================================================

create extension if not exists pgcrypto;  -- for gen_random_uuid()

-- ============================================================================
-- Support tables
-- ============================================================================

create table categories (
    id          uuid primary key default gen_random_uuid(),
    name        text not null unique
);

create table meetup_locations (
    id          uuid primary key default gen_random_uuid(),
    name        text not null,
    description text,
    active      boolean not null default true
);

-- ============================================================================
-- Accounts
-- ============================================================================

create table users (
    id              uuid primary key default gen_random_uuid(),
    email           text not null unique,
    auth_id         text not null unique,          -- Supabase auth / Google OAuth subject
    edu_verified_at timestamptz,                    -- null until .edu verification completes
    status          text not null default 'active'
                        check (status in ('active', 'restricted', 'banned')),
    created_at      timestamptz not null default now()
);

create index users_status_idx on users (status);

-- ============================================================================
-- Vendor domain
-- ============================================================================

create table vendors (
    -- existence of a row here IS vendor status; standing (active/restricted/
    -- banned) lives on users.status via user_id, not duplicated here
    id                    uuid primary key default gen_random_uuid(),
    user_id               uuid not null unique references users (id),
    business_name         text not null,
    business_description  text,
    photos                text[] not null default '{}',
    created_at            timestamptz not null default now()
);

create table vendor_applications (
    id                       uuid primary key default gen_random_uuid(),
    user_id                  uuid not null references users (id),
    business_name            text not null,
    business_description     text,
    what_selling             text not null,
    licenses_held            text,
    desired_selling_location text,
    team_size                integer,
    student_id               text not null,
    status                   text not null default 'pending'
                                 check (status in ('pending', 'approved', 'rejected')),
    reviewed_by              uuid references users (id),
    reviewed_at              timestamptz,
    created_at               timestamptz not null default now()
);

create index vendor_applications_status_idx on vendor_applications (status);

create table vendor_team_members (
    id         uuid primary key default gen_random_uuid(),
    vendor_id  uuid not null references vendors (id) on delete cascade,
    user_id    uuid not null references users (id),
    role       text not null default 'member'
                   check (role in ('owner', 'member')),
    added_at   timestamptz not null default now(),
    unique (vendor_id, user_id)
);

create table products (
    id           uuid primary key default gen_random_uuid(),
    vendor_id    uuid not null references vendors (id) on delete cascade,
    title        text not null,
    description  text,
    price        integer not null check (price >= 0),   -- cents
    stock_count  integer not null default 0 check (stock_count >= 0),
    status       text not null default 'active'
                     check (status in ('active', 'hidden')),
    category_id  uuid references categories (id),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now()
);

create index products_vendor_idx on products (vendor_id);
create index products_category_idx on products (category_id);
create index products_status_idx on products (status);

create table product_photos (
    id          uuid primary key default gen_random_uuid(),
    product_id  uuid not null references products (id) on delete cascade,
    url         text not null,
    caption     text,
    sort_order  integer not null default 0
);

create index product_photos_product_idx on product_photos (product_id);

-- ============================================================================
-- Customer listings (one-off sales — separate from vendor products)
-- ============================================================================

create table listings (
    id          uuid primary key default gen_random_uuid(),
    seller_id   uuid not null references users (id),
    title       text not null,
    description text,
    price       integer not null check (price >= 0),   -- cents
    category_id uuid references categories (id),
    status      text not null default 'pending_review'
                    check (status in ('pending_review', 'live', 'sold', 'rejected', 'hidden')),
    reviewed_by uuid references users (id),
    created_at  timestamptz not null default now()
);

create index listings_seller_idx on listings (seller_id);
create index listings_category_idx on listings (category_id);
create index listings_status_idx on listings (status);

create table listing_photos (
    id          uuid primary key default gen_random_uuid(),
    listing_id  uuid not null references listings (id) on delete cascade,
    url         text not null,
    sort_order  integer not null default 0
);

create index listing_photos_listing_idx on listing_photos (listing_id);

-- ============================================================================
-- Messaging (persistent thread per pair of users)
-- ============================================================================

create table conversations (
    id              uuid primary key default gen_random_uuid(),
    user_a_id       uuid not null references users (id),
    user_b_id       uuid not null references users (id),
    created_at      timestamptz not null default now(),
    last_message_at timestamptz not null default now(),
    -- canonical ordering + uniqueness so the same two people can't end up
    -- with two separate threads
    constraint conversations_order_chk check (user_a_id < user_b_id),
    constraint conversations_pair_uq unique (user_a_id, user_b_id)
);

create table messages (
    id              uuid primary key default gen_random_uuid(),
    conversation_id uuid not null references conversations (id) on delete cascade,
    sender_id       uuid not null references users (id),
    body            text not null,
    created_at      timestamptz not null default now()
);

create index messages_conversation_idx on messages (conversation_id, created_at);

-- ============================================================================
-- Transactions (unified two-stage lifecycle for both products and listings)
-- ============================================================================

create table transactions (
    id                          uuid primary key default gen_random_uuid(),
    product_id                  uuid references products (id),
    listing_id                  uuid references listings (id),
    buyer_id                    uuid not null references users (id),
    seller_id                   uuid not null references users (id),
    conversation_id             uuid not null references conversations (id),
    meetup_location_id          uuid references meetup_locations (id),  -- null = "Other"
    liability_acknowledged_at   timestamptz,                             -- set only for "Other"

    -- stage 1: intent / meetup agreed by both sides
    buyer_intent_confirmed_at   timestamptz,
    seller_intent_confirmed_at  timestamptz,

    -- stage 2: sale completion confirmed by both sides (gates reviews)
    buyer_sale_confirmed_at     timestamptz,
    seller_sale_confirmed_at    timestamptz,

    status                      text not null default 'active'
                                    check (status in ('active', 'completed', 'cancelled', 'expired')),
    created_at                  timestamptz not null default now(),

    -- exactly one of product_id / listing_id is set
    constraint transactions_target_chk check (
        (product_id is not null and listing_id is null) or
        (product_id is null and listing_id is not null)
    )
);

create index transactions_buyer_idx on transactions (buyer_id);
create index transactions_seller_idx on transactions (seller_id);
create index transactions_conversation_idx on transactions (conversation_id);
create index transactions_status_idx on transactions (status);
-- speeds up the "one side confirmed, other didn't" reminder-job scan
create index transactions_pending_intent_idx on transactions (seller_intent_confirmed_at)
    where buyer_intent_confirmed_at is not null and seller_intent_confirmed_at is null;
create index transactions_pending_sale_idx on transactions (seller_sale_confirmed_at)
    where buyer_sale_confirmed_at is not null and seller_sale_confirmed_at is null;

-- ============================================================================
-- Vendor product buy requests (stock-hold + accept/reject, product-only)
-- ============================================================================

create table buy_requests (
    id             uuid primary key default gen_random_uuid(),
    product_id     uuid not null references products (id),
    buyer_id       uuid not null references users (id),
    quantity       integer not null default 1 check (quantity > 0),
    status         text not null default 'pending'
                       check (status in ('pending', 'accepted', 'rejected')),
    requested_at   timestamptz not null default now(),
    responded_at   timestamptz,
    expires_at     timestamptz,                       -- stock-hold timeout; app sets on insert
    transaction_id uuid references transactions (id)   -- set once accepted
);

create index buy_requests_product_idx on buy_requests (product_id);
create index buy_requests_buyer_idx on buy_requests (buyer_id);
create index buy_requests_status_idx on buy_requests (status);
-- for the "vendor never responded" expiry sweep
create index buy_requests_pending_expiry_idx on buy_requests (expires_at)
    where status = 'pending';

-- ============================================================================
-- Reviews (vendor + product only — listings are not reviewable)
-- ============================================================================

create table reviews (
    id             uuid primary key default gen_random_uuid(),
    transaction_id uuid not null unique references transactions (id),
    reviewer_id    uuid not null references users (id),
    vendor_id      uuid not null references vendors (id),   -- denormalized for fast rollups
    product_id     uuid not null references products (id),
    rating         integer not null check (rating between 1 and 5),
    comment        text,
    created_at     timestamptz not null default now()
);

-- for review-bombing detection: count per vendor in a rolling window
create index reviews_vendor_created_idx on reviews (vendor_id, created_at);
-- for serial-bad-reviewer detection: count per reviewer in a rolling window
create index reviews_reviewer_created_idx on reviews (reviewer_id, created_at);

-- ============================================================================
-- Reports (moderation)
-- ============================================================================

create table reports (
    id              uuid primary key default gen_random_uuid(),
    reporter_id     uuid not null references users (id),
    transaction_id  uuid references transactions (id),
    conversation_id uuid references conversations (id),
    reason          text not null,
    status          text not null default 'pending'
                        check (status in ('pending', 'reviewed', 'dismissed', 'actioned')),
    reviewed_by     uuid references users (id),
    created_at      timestamptz not null default now()
);

create index reports_reporter_idx on reports (reporter_id);
create index reports_status_idx on reports (status);

-- ============================================================================
-- Triggers
-- ============================================================================

-- keep products.updated_at current on any change
create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at := now();
    return new;
end;
$$ language plpgsql;

create trigger products_set_updated_at
    before update on products
    for each row
    execute function set_updated_at();

-- when both sides confirm a sale: mark the transaction completed, and if it
-- was a listing sale, take the listing down (flip it to 'sold')
create or replace function complete_transaction_on_dual_confirmation()
returns trigger as $$
begin
    if new.buyer_sale_confirmed_at is not null
       and new.seller_sale_confirmed_at is not null
       and (old.buyer_sale_confirmed_at is null or old.seller_sale_confirmed_at is null) then

        new.status := 'completed';

        if new.listing_id is not null then
            update listings set status = 'sold' where id = new.listing_id;
        end if;
    end if;
    return new;
end;
$$ language plpgsql;

create trigger transactions_complete_on_dual_confirmation
    before update on transactions
    for each row
    execute function complete_transaction_on_dual_confirmation();

-- ============================================================================
-- Row Level Security — deny-all backstop
-- ============================================================================
-- All real access control lives in the Java backend, which always connects
-- as service_role (service_role bypasses RLS unconditionally, so none of
-- this affects it). Enabling RLS with zero policies on every table means
-- the anon/authenticated keys — public by design, shipped in the frontend
-- bundle — get zero rows on every read and are rejected on every write, no
-- matter what. This closes the "someone finds the anon key and queries the
-- database directly" hole with no ongoing maintenance and no change to how
-- the backend talks to the database.

alter table categories           enable row level security;
alter table meetup_locations     enable row level security;
alter table users                enable row level security;
alter table vendors              enable row level security;
alter table vendor_applications  enable row level security;
alter table vendor_team_members  enable row level security;
alter table products             enable row level security;
alter table product_photos       enable row level security;
alter table listings             enable row level security;
alter table listing_photos       enable row level security;
alter table conversations        enable row level security;
alter table messages             enable row level security;
alter table transactions         enable row level security;
alter table buy_requests         enable row level security;
alter table reviews              enable row level security;
alter table reports              enable row level security;
