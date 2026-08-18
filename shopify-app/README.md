# Offgrid Lifestyle · Social Sync

A custom Shopify app (Remix + Polaris, embedded in the Shopify admin) that
connects the **offgrid-lifestyle.com** store to Instagram, Facebook, TikTok
and YouTube:

- **Catalog sync** — pushes products to the Facebook & Instagram Shop
  catalog and TikTok Shop whenever a product is created, updated, or deleted.
- **Auto-post** — optionally posts new products to Instagram, Facebook,
  TikTok, and a YouTube video description update, using a configurable
  caption template (`{{title}}`, `{{price}}`, `{{url}}`).
- **Connections dashboard** — OAuth "Connect" buttons for each platform,
  status, and disconnect, inside the Shopify admin.
- **Storefront social feed embed** — a theme app extension block merchants
  can drop into any section to show the latest Instagram posts on
  offgrid-lifestyle.com, served through a signed Shopify app proxy so no API
  keys reach the browser.

## Project layout

```
app/
  routes/
    app.tsx, app._index.tsx        Embedded admin dashboard
    app.connections*.tsx           OAuth connect/callback + connections UI
    app.settings.tsx               Sync/auto-post toggles + caption template
    app.activity.tsx               Audit log of syncs/posts
    webhooks.products.*.tsx        Shopify product webhooks -> sync pipeline
    api.proxy.$.tsx                Storefront social feed data (app proxy)
  services/
    social/meta.server.ts          Facebook & Instagram Graph API
    social/tiktok.server.ts        TikTok Shop + Content Posting API
    social/youtube.server.ts       YouTube OAuth + Data API
    productSync.server.ts          Orchestrates sync/auto-post across platforms
    encryption.server.ts           AES-256-GCM at-rest token encryption
  models/                          Prisma-backed data access (connections, settings, logs)
prisma/schema.prisma               Session, SocialConnection, SyncSettings, ActivityLog
extensions/social-feed-embed/      Theme app extension (storefront feed block)
```

## Setup

1. **Create the social accounts first.** See
   [`../docs/SOCIAL_CHANNEL_SETUP.md`](../docs/SOCIAL_CHANNEL_SETUP.md) for the
   step-by-step checklist (Instagram, Facebook, TikTok, YouTube) — this has
   to be done by a human as the account owner.
2. **Register developer apps** on each platform to get API credentials
   (also covered in the checklist): Meta for Developers, TikTok for
   Developers / TikTok Shop Partner Center, Google Cloud Console.
3. Copy `.env.example` to `.env` and fill in the Shopify + platform
   credentials. Generate `TOKEN_ENCRYPTION_KEY` with `openssl rand -hex 32`.
4. Install the Shopify CLI if you don't have it: `npm install -g @shopify/cli`.
5. `npm install`
6. `shopify app config link` to connect this codebase to your Partner Dashboard app.
7. `npm run dev` to start a local dev tunnel and install the app on your dev store.
8. Once ready for the live store: `npm run deploy` and install the app on
   the offgrid-lifestyle.com Shopify store.

## How auto-post decides what to send

`app/services/productSync.server.ts` is the single place all four platforms
are wired together — each webhook handler calls it, it reads the shop's
`SyncSettings`, and fans out to the enabled platform service modules. Every
attempt (success or failure) is written to `ActivityLog`, visible on the
**Activity** page in the admin.

## Notes / current limitations

- TikTok and Instagram auto-posting require an image URL on the product; a
  product without an image is skipped and logged.
- YouTube's Community Posts API is allowlisted per-channel by Google, so
  `postProductUpdate` currently appends the product blurb to the description
  of the channel's most recent upload. Swap in the real Community Posts call
  once your channel is approved — see the comment in
  `app/services/social/youtube.server.ts`.
- YouTube Shopping's product feed is linked via Merchant Center in YouTube
  Studio (manual, one-time) rather than a write API — see the setup doc.
- This scaffold uses SQLite for simplicity; swap the Prisma datasource to
  Postgres/MySQL before running multiple app instances in production.
