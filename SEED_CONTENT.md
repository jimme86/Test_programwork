# Content-opzet — mirrors offgridcentrum.nl structuur

Dit thema bevat geen productdata (dat leeft in Shopify Admin, niet in de theme-code). Onderstaande opzet volgt de structuur en taal van offgridcentrum.nl zo dicht mogelijk, gebaseerd op wat publiek vindbaar is (paginanamen/metabeschrijvingen via zoekmachines — de site zelf kon niet automatisch worden opgehaald door het netwerkbeleid van deze sessie). Gebruik dit als checklist bij het inrichten van de winkel; pas cijfers/prijzen aan naar de actuele situatie.

## Collecties (Shopify Admin → Producten → Collecties)

| Collectie | Handle (suggestie) | Gebruikt in |
|---|---|---|
| Zonnepanelen | `zonnepanelen` | Categorieblok home, header-menu |
| Thuisbatterijen | `thuisbatterijen` | Categorieblok home, header-menu |
| Omvormers | `omvormers` | Categorieblok home, header-menu |
| Off-grid sets | `off-grid-sets` | Categorieblok home, uitgelichte collectie |
| Laden & omvormen | `laden-omvormen` | Categorieblok home |
| Accessoires | `accessoires` | Categorieblok home |

## Pagina's (Shopify Admin → Online store → Pagina's)

| Pagina | Handle | Template |
|---|---|---|
| Contact | `contact` | `page.contact.json` (automatisch) — contactformulier + gegevens |
| Off-grid Portaal | `off-grid-portaal` | `page.off-grid-portaal.json` (automatisch) — stappenplan + CTA |
| Verzenden & bezorging | `verzenden` | `page.json` (standaard) |
| Klachten | `klachten` | `page.json` (standaard) |
| Over ons | `over-ons` | `page.json` (standaard) |

Voor "Verzenden & bezorging" en "Klachten": schrijf eigen, actuele tekst (verzendtermijnen, retourbeleid, klachtenprocedure) — dit is juridisch/operationeel van aard en dus bewust niet vooringevuld.

## Merken (sectie "Merken" op home)

Gebaseerd op merken die in productnamen/paginatitels van de branche voorkomen: **Victron Energy**, **Hithium**, **Pylontech**, **Solarge**. Voeg logo's toe via de blokinstellingen, of laat leeg voor een tekstuele merknaam.

## Voorbeeld off-grid sets (als richtlijn voor productinvoer)

Gebaseerd op het type samengestelde sets dat in deze branche gangbaar is (pas capaciteiten/prijzen aan naar je eigen assortiment):

1. **Off-grid set 7,2 kWh** — lithium accu, ~4000 W omvormer, 10 zonnepanelen. Geschikt voor een kleinere woning, camper of vakantiehuis.
2. **Off-grid set 12 kWh** — lithium accu, ~8000 W omvormer, 12 zonnepanelen. Geschikt voor gemiddeld huishoudelijk verbruik.
3. **Off-grid set 14,4 kWh** — lithium accu, ~6500 W omvormer, 18 zonnepanelen. Geschikt voor grotere woningen of hoger verbruik.

Beschrijf bij elk product: geleverde componenten, of het plug-and-play/voorbekabeld is, garantietermijn, en voor wie het geschikt is (huis / camper / boot / bedrijf) — dat is de toon die in deze sector goed converteert.

## Navigatiemenu (Shopify Admin → Navigatie → menu met handle `main-menu`)

```
Webshop
  ├─ Zonnepanelen
  ├─ Thuisbatterijen
  ├─ Omvormers
  ├─ Off-grid sets
  ├─ Laden & omvormen
  └─ Accessoires
Off-grid Portaal
Kennisbank (blog)
Over ons
Contact
```

## Wil je een écht identieke kopie?

Stuur me de paginatekst (copy/paste of screenshots) van de pagina's die je exact wilt overnemen — bijvoorbeeld de homepage-hero, productbeschrijvingen of de FAQ — en ik verwerk die letterlijk in dit thema. Zonder die input kan ik de site niet automatisch ophalen vanuit deze sessie (netwerkbeleid blokkeert offgridcentrum.nl).
