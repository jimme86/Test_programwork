# Deploying the plugin to the live offgrid-lifestyle.com store

1. **Host the app.** Deploy `shopify-app/` to any Node host that supports
   long-running processes (Fly.io, Render, Railway, a VPS). It needs a
   persistent database — swap the Prisma `datasource` in
   `prisma/schema.prisma` from SQLite to Postgres/MySQL for anything beyond
   a quick test, then `npx prisma migrate deploy`.
2. **Set environment variables** on the host from `shopify-app/.env.example`
   — Shopify API key/secret, `SHOPIFY_APP_URL` (your host's HTTPS URL),
   `TOKEN_ENCRYPTION_KEY`, and the Meta/TikTok/YouTube app credentials from
   `docs/SOCIAL_CHANNEL_SETUP.md`.
3. **Create the app in the Shopify Partner Dashboard** (or run
   `shopify app config link` from `shopify-app/` to do it via CLI), pointing
   `application_url` and the OAuth redirect URLs in `shopify.app.toml` at
   your hosted URL.
4. **Deploy the app config + extensions**: `npm run deploy` from
   `shopify-app/`. This registers the webhooks and the theme app extension
   (the storefront social feed block) with Shopify.
5. **Install the app** on the offgrid-lifestyle.com Shopify store (Partner
   Dashboard → your app → "Select store" for a custom/single-merchant app,
   or via the install link if distributing privately).
6. **Add the social feed block** to the storefront: Online Store → Themes →
   Customize → Add section/block → "Social feed" (under Apps), on any page
   (e.g. the homepage, right above the footer).
7. **Connect the channels**: open the app in the Shopify admin →
   Connections → Connect each of Facebook & Instagram, TikTok, YouTube
   (requires the accounts + developer app credentials from
   `docs/SOCIAL_CHANNEL_SETUP.md` to already exist).
8. **Turn on sync/auto-post**: app → Settings → enable catalog sync and/or
   auto-post per platform, and adjust the caption template.
