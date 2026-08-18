# Offgrid Lifestyle — Social Channel Setup Checklist

This is the manual, one-time setup for the four channels. It has to be done
by a real person as the business owner (phone/email verification, ToS
acceptance) — it cannot be automated by the Shopify plugin. Once each
account exists and you've registered a developer app, the plugin's
**Connections** page can OAuth-connect to it.

Suggested handle across all platforms: **@offgridlifestyle** (fallback:
`@offgrid.lifestyle` / `@offgridlifestyleco` if taken).

## 1. Instagram

1. Create a **Business** account (Settings → Account type → switch to
   Professional → Business), linked to a Facebook Page (see step 2 — do
   Facebook first if you don't have a Page yet).
2. Profile photo: the square logo mark (see `docs/BRAND_ASSETS.md`).
3. Bio (150 char limit):
   > 🌲 Off-grid living, made simple. Solar, van life & sustainable gear.
   > ⚡ Shop the kit ⬇️
4. Add the storefront link: `https://offgrid-lifestyle.com`, or use a
   link-in-bio if you'll rotate links.
5. Turn on **Shopping**: Settings → Business → Shopping → connect the
   catalog you'll create in step 2.4 (Meta Commerce Manager). Instagram
   Shopping requires the catalog to be approved first, which can take a
   few days.

## 2. Facebook

1. Create a **Facebook Page** for the brand (Pages → Create new Page),
   category "Shopping & Retail" or "Outdoor & Sporting Goods Company".
2. Profile photo + cover photo (see `docs/BRAND_ASSETS.md`).
3. About section: business hours, website `offgrid-lifestyle.com`, contact
   email.
4. In **Commerce Manager** (business.facebook.com/commerce), create a
   catalog, e.g. "Offgrid Lifestyle Catalog". This is the `META_CATALOG_ID`
   the plugin syncs Shopify products into. Alternatively, install Meta's own
   "Facebook & Instagram" Shopify sales channel first (App Store) to
   generate the catalog automatically, and just point this plugin's env var
   at that catalog id.
5. Register a **Meta for Developers** app at developers.facebook.com:
   - App type: Business.
   - Add products: "Facebook Login for Business" and "Instagram Graph API".
   - Under App Roles, add yourself as Admin; add the Page + catalog to the
     app's assigned assets in Business Settings.
   - Copy the App ID / App Secret into `META_APP_ID` / `META_APP_SECRET`
     in `shopify-app/.env`.
   - Add the redirect URI from `.env.example` (`META_REDIRECT_URI`) under
     Facebook Login for Business → Settings → Valid OAuth Redirect URIs.
   - Submit for **App Review** for `catalog_management`,
     `instagram_content_publish`, `pages_manage_posts` before going live —
     these are restricted permissions Meta must approve.

## 3. TikTok

1. Create a TikTok account, switch to a **Business Account** (Settings →
   Account → Switch to Business Account).
2. Profile photo (see `docs/BRAND_ASSETS.md`), bio, and website link:
   > Off-grid gear for real adventures 🏕️🔋
   > offgrid-lifestyle.com
3. Apply for **TikTok Shop** at partner.tiktokshop.com (business
   verification, bank details, product categories). This is what
   `TIKTOK_SHOP_ID` refers to once approved.
4. Register an app at developers.tiktok.com ("TikTok for Developers"):
   - Add the **Content Posting API** and **TikTok Shop** products.
   - Copy Client Key / Client Secret into `TIKTOK_APP_ID` /
     `TIKTOK_APP_SECRET`.
   - Add the redirect URI from `.env.example`.
   - Direct posting (no manual approval step in the TikTok inbox) requires
     TikTok to grant your app "unaudited/audited client" status for the
     `video.publish` scope — start in sandbox/unaudited mode, request audit
     before launch.

## 4. YouTube

1. Create the channel from a dedicated Google/Brand account (Google
   Account → create a **Brand Account** named "Offgrid Lifestyle" so
   ownership isn't tied to one person's personal Gmail).
2. Channel art: profile picture + banner (see `docs/BRAND_ASSETS.md`).
3. Channel description:
   > Off-grid living, solar setups, van builds & sustainable gear reviews.
   > Shop what you see: offgrid-lifestyle.com
4. In **YouTube Studio → Customization → Basic info**, add the storefront
   link, and enable the **Store** tab: Studio → Earn → Shopping → connect a
   **Google Merchant Center** account, then link the same product feed you
   use for Google Shopping (Merchant Center can import a feed directly from
   the offgrid-lifestyle.com Shopify store via the free Google & YouTube
   sales channel app, or via a scheduled feed URL).
5. In **Google Cloud Console**, create a project + OAuth consent screen +
   OAuth 2.0 Client ID ("Web application"):
   - Enable the **YouTube Data API v3**.
   - Add the redirect URI from `.env.example` (`YOUTUBE_REDIRECT_URI`).
   - Copy Client ID / Secret into `YOUTUBE_CLIENT_ID` /
     `YOUTUBE_CLIENT_SECRET`.
   - While the consent screen is in "Testing" mode only accounts you add as
     test users can connect — publish it (or keep it internal if using a
     Workspace account) before going live.

## After all four are created

Go to the app's **Connections** page in the Shopify admin
(`/admin/apps/offgrid-social-sync/app/connections` once installed) and click
**Connect** on each platform to finish linking them to the plugin.
