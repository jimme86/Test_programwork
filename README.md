# Off-Grid Centrum — Shopify theme

Een eigen Shopify Online Store 2.0-thema, gebouwd in de stijl van [offgridcentrum.nl](https://www.offgridcentrum.nl/nl): een Nederlandse specialist in onafhankelijke energie (zonnepanelen, thuisbatterijen, omvormers en complete off-grid sets van o.a. Victron en Hithium).

> **Let op — geen 1-op-1 kopie.** De live site kon vanuit deze omgeving niet automatisch worden opgehaald (geblokkeerd door het netwerkbeleid van deze sessie). Dit thema is daarom opnieuw opgebouwd op basis van de bekende propositie, productcategorieën en toon van offgridcentrum.nl (via zoekresultaten/metadata), niet door de site 1-op-1 te kopiëren. Vervang teksten, kleuren, logo en afbeeldingen naar wens voordat je live gaat — zie "Aanpassen" hieronder.

## Wat zit erin

Een compleet, direct bruikbaar Shopify-thema (Online Store 2.0 / JSON-templates):

- **Home**: hero, USP-balk, categorieblokken, uitgelichte collectie, off-grid-calculator CTA met statistieken, tekstblok, klantreviews, blog/kennisbank-teaser.
- **Product**: galerij met thumbnails, varianten (pills), aantal-stepper, AJAX "in winkelwagen", USP's, specificaties-accordion, gerelateerde producten.
- **Collectie**: banner, filters (facets), sortering, paginering.
- **Winkelwagen**: zijpaneel (drawer) + volledige winkelwagenpagina, gratis-verzendbalk.
- **Content**: pagina, contactformulier, blog + artikel (met reacties), zoeken, 404, cadeaubon.
- **Account**: inloggen, registreren, wachtwoord herstellen/activeren, bestellingen, adressen.
- Instelbaar in **Thema-aanpassen**: kleuren, typografie (font pickers), paginabreedte, hoekafronding, logo, social links, contactgegevens, winkelwagentype, gratis-verzenddrempel.

## Ontwerprichting

- **Kleuren**: donkergroen (`#0F3D2E`) als merkkleur, zon-oranje (`#F5A524`) als accent, zand/crème achtergrond — een duurzame, technische maar warme uitstraling passend bij zonne-energie/off-grid.
- **Typografie**: Poppins (koppen) + Inter (body) via Shopify's font-picker, makkelijk te wijzigen in **Thema-instellingen → Typografie**.
- **Toon**: Nederlandstalig, direct en servicegericht ("800+ installaties", "plug-and-play geleverd", persoonlijk advies) — in lijn met de positionering van offgridcentrum.nl.

Alle kleuren, teksten en de meeste labels staan in de sectie-instellingen of `config/settings_schema.json`, dus zijn zonder code aan te passen via de Theme Editor.

## Structuur

```
config/      Thema-instellingen (kleuren, fonts, social, contact)
layout/      theme.liquid + password.liquid
locales/     Nederlandse vertaalstrings (nl.default.json)
sections/    Herbruikbare, instelbare secties
snippets/    Kleine herbruikbare componenten (product-card, price, icons, cart-drawer...)
templates/   JSON-templates per paginatype + klant-/cadeaubonpagina's
assets/      base.css (styling) + theme.js (cart drawer, mobiel menu, galerij, stepper)
```

## Lokaal ontwikkelen / preview

Vereist [Shopify CLI](https://shopify.dev/docs/api/shopify-cli) en een (development) Shopify-winkel.

```bash
npx @shopify/cli theme dev --store jouw-winkel.myshopify.com
```

Valideren op fouten:

```bash
npx @shopify/cli theme check
```

Live zetten:

```bash
npx @shopify/cli theme push --store jouw-winkel.myshopify.com
```

## Aanpassen voor jouw winkel

1. **Logo & favicon** uploaden via Thema-instellingen → Logo.
2. **Menu's**: maak in Shopify Admin → Navigatie een menu met handle `main-menu` (koppelingen naar je collecties, off-grid calculator, etc.) — de header pakt dit automatisch op.
3. **Collecties**: maak collecties aan voor bijv. Zonnepanelen, Thuisbatterijen, Omvormers, Off-grid sets, Laden & omvormen, Accessoires en koppel ze aan de categorie- en filterblokken op de homepage.
4. **Contactpagina**: maak een pagina met handle `contact` — deze gebruikt automatisch het contactformulier-template (`templates/page.contact.json`).
5. **Kleuren/fonts/copy**: volledig aan te passen via de Theme Editor, geen code nodig.
6. **Afbeeldingen**: upload eigen productfoto's, hero-beeld en categoriefoto's — dit thema bevat bewust geen afbeeldingen van de originele site.
