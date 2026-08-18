# OffGrid Lifestyle — Android Webshop App

A native Android app for the OffGrid Lifestyle webshop: browse solar power,
water filtration, cooking, lighting, shelter, and tools gear, add items to a
cart, and submit an order request. Built with Kotlin, Jetpack Compose, and
Material 3, wired to sell through your **Shopify** store's Storefront API.

## Status / scope

- ✅ Product catalog, categories, search, product detail with variants
- ✅ Cart (persisted on-device) — add, update quantity, remove
- ✅ Checkout screen that collects shipping/contact details and submits an
  order **request** (no live payment processing — see "Checkout scope" below)
- ✅ Shopify Storefront API (GraphQL) integration, with an automatic
  built-in demo catalog when no store is configured
- ⏳ Not included yet: real payment processing, push notifications, order
  history/login. See "Next steps" for how to add them.

## Quick start

1. Open this folder in Android Studio (Koala/2024.1+ recommended).
2. Let Android Studio sync Gradle — it will download the Android SDK
   platform/build tools automatically if needed.
3. Run the `app` module on an emulator or device (minSdk 26 / Android 8.0+).
4. The app runs immediately in **Demo Mode** with a bundled sample catalog —
   no configuration required.

### Connecting your real Shopify store

The app reads Shopify credentials from `local.properties`, which is
git-ignored so secrets never get committed.

1. In Shopify admin: **Settings → Apps and sales channels → Develop apps**
   → create an app → enable the **Storefront API** → generate a
   **Storefront API access token** (the public one, not the Admin API key).
2. Copy `local.properties.example` to `local.properties` in the project root.
3. Fill in:
   ```properties
   SHOPIFY_SHOP_DOMAIN=your-store.myshopify.com
   SHOPIFY_STOREFRONT_TOKEN=your-storefront-access-token
   ```
4. Rebuild. `ShopifyConfig.isConfigured` becomes `true` and
   `AppContainer` automatically switches from `MockProductRepository` to
   `ShopifyProductRepository` — no code changes needed. The "Demo Mode"
   banner on the home screen disappears once you're talking to the real
   store.

Product **categories** are derived from each product's Shopify
`productType` field, and category filtering / search map to Storefront
`query:` search syntax (`product_type:'X' AND title:*Y*`). If you use
Shopify **collections** instead of product types for merchandising, that's
a straightforward swap in `ShopifyProductRepository` — the GraphQL query
scaffolding for collections is already in `GraphQLQueries.COLLECTIONS_QUERY`.

## Checkout scope

This build intentionally does **not** process payments. The Checkout screen
collects the customer's contact/shipping details and, on submit, packages
them with the cart into an `OrderRequest` — today that's just shown back to
the user as a confirmation ("we'll reach out to confirm shipping and
payment"). To go further:

- **Fastest path:** swap `CheckoutViewModel.submit()` to POST the
  `OrderRequest` to your own backend/email webhook so the store owner gets
  notified, while payment is still collected manually (bank transfer, invoice, etc).
- **Full self-checkout:** integrate the Shopify **Cart/Checkout API** (create
  a cart via Storefront API `cartCreate`, then hand off to Shopify's hosted
  checkout URL via a WebView or Custom Tab) — this is the standard way
  Shopify-backed apps take payment without a PCI-compliance burden. Or wire
  up Stripe/Mollie/Adyen directly if you want an in-app native payment sheet.

## Architecture

```
app/src/main/java/com/offgridlifestyle/shop/
├── data/
│   ├── model/       # Domain models (Product, CartItem, OrderRequest) — UI-facing
│   ├── remote/       # Shopify GraphQL client, queries, and response DTOs
│   ├── repository/   # ProductRepository interface + Mock/Shopify implementations
│   └── cart/         # CartRepository — in-memory + DataStore-persisted cart
├── ui/
│   ├── theme/         # Material 3 color scheme, typography (offgrid palette)
│   ├── navigation/     # Single NavHost + bottom nav bar wiring all screens
│   ├── components/     # Shared composables (ProductCard, TopBar, EmptyState)
│   └── <feature>/       # home, catalog, productdetail, cart, checkout, about
│                          — each a Screen (Compose) + ViewModel (StateFlow) pair
├── OffGridApp.kt        # Application class + AppContainer (manual DI)
└── MainActivity.kt
```

- **MVVM**, unidirectional data flow: ViewModels expose `StateFlow<UiState>`,
  screens `collectAsState()` and call ViewModel functions for user actions.
- **Manual DI** via `AppContainer` (`OffGridApp.kt`) instead of Hilt/Koin —
  the app is small enough that a DI framework would add more ceremony than
  value. `AppContainer` is the one place that decides `ShopifyProductRepository`
  vs `MockProductRepository`.
- **Networking**: hand-written GraphQL over OkHttp + kotlinx.serialization
  (`data/remote`) rather than Apollo GraphQL, specifically so the project
  builds without needing network access to a live store's schema at build
  time (Apollo's codegen needs a downloaded `schema.graphql`). If you'd
  rather use Apollo for stronger type-safety once you have a live store,
  swapping it in is confined to `data/remote` and `ShopifyProductRepository`.
- **Cart persistence**: `CartRepository` keeps cart state in a `StateFlow`
  and mirrors it to Jetpack DataStore (as serialized JSON) so it survives
  app restarts — there's no server-side cart since checkout doesn't take
  payment yet.

## Tech stack

Kotlin 2.0, Jetpack Compose + Material 3, Navigation Compose, Coil (image
loading), OkHttp + kotlinx.serialization (Shopify GraphQL), Jetpack DataStore
(cart persistence), Gradle version catalogs (`gradle/libs.versions.toml`).

## A note on this build

This project was generated in a sandboxed environment without access to
Google's Maven repository (`dl.google.com`), so the Android Gradle Plugin
and AndroidX artifacts could not be downloaded to run a full `./gradlew
build` here. The code was written carefully by hand and checked for
structural issues (import correctness, brace/paren balance, DSL usage
patterns cross-checked against known-good AndroidX APIs), but **please run
a full build and a smoke test on a device/emulator** as your first step —
and treat that as part of reviewing this PR rather than a formality, since
it hasn't been through a compiler yet.

## Next steps / ideas

- Swap the checkout stub for Shopify's Cart API + hosted checkout handoff
- Add sign-in (Shopify Customer Accounts API) for order history
- Push notifications for order/shipping updates
- Wishlist / saved items
- Product reviews
