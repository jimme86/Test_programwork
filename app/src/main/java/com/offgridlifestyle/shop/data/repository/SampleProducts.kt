package com.offgridlifestyle.shop.data.repository

import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.model.ProductVariant

/**
 * Built-in demo catalog used whenever Shopify credentials aren't configured
 * (see ShopifyConfig / local.properties.example). Lets the app run and look
 * complete out of the box, and doubles as a Compose preview data source.
 */
object SampleProducts {

    private fun image(seed: String) = "https://picsum.photos/seed/$seed/900/900"

    val categories = listOf(
        "Solar Power", "Water & Filtration", "Cooking & Heating", "Lighting", "Camping & Shelter", "Tools"
    )

    val all: List<Product> = listOf(
        Product(
            id = "gid://demo/Product/1",
            handle = "portable-solar-panel-100w",
            title = "100W Portable Solar Panel",
            description = "A folding monocrystalline panel built for basecamp and van life. " +
                "Weatherproof, lightweight, and pairs with any standard power station via MC4 or DC output.",
            category = "Solar Power",
            imageUrls = listOf(image("solar-panel-1"), image("solar-panel-2")),
            currencyCode = "EUR",
            priceRange = 219.0..219.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/1a", "Default", 219.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/2",
            handle = "1000wh-power-station",
            title = "1000Wh Portable Power Station",
            description = "LiFePO4 battery bank with pure sine wave inverter, enough to run a fridge, " +
                "laptop, and lights for a full day off-grid. Solar-chargeable in 4-6 hours.",
            category = "Solar Power",
            imageUrls = listOf(image("power-station-1"), image("power-station-2")),
            currencyCode = "EUR",
            priceRange = 649.0..799.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/2a", "1000Wh", 649.0, "EUR", true),
                ProductVariant("gid://demo/Variant/2b", "1500Wh", 799.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/3",
            handle = "gravity-water-filter",
            title = "Gravity-Fed Water Filter, 8L",
            description = "Removes 99.99% of bacteria, cysts, and microplastics with no pump and no power. " +
                "Two-bucket design filters up to 8 liters per hour, ideal for cabins and basecamps.",
            category = "Water & Filtration",
            imageUrls = listOf(image("water-filter-1")),
            currencyCode = "EUR",
            priceRange = 89.0..89.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/3a", "Default", 89.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/4",
            handle = "personal-water-filter-straw",
            title = "Personal Filter Straw",
            description = "Pocket-sized emergency filter good for 4,000L. Filters straight from streams, " +
                "lakes, or a bottle. A staple of any bug-out or hiking kit.",
            category = "Water & Filtration",
            imageUrls = listOf(image("filter-straw-1")),
            currencyCode = "EUR",
            priceRange = 24.0..24.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/4a", "Default", 24.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/5",
            handle = "folding-wood-stove",
            title = "Folding Titanium Wood Stove",
            description = "Pocket-flat when packed, sturdy when set up. Burns twigs and scrap wood so you " +
                "never run out of fuel — pairs perfectly with the kettle below.",
            category = "Cooking & Heating",
            imageUrls = listOf(image("wood-stove-1"), image("wood-stove-2")),
            currencyCode = "EUR",
            priceRange = 54.0..54.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/5a", "Default", 54.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/6",
            handle = "camp-kettle-1l",
            title = "Stainless Camp Kettle, 1L",
            description = "Boils fast over any open flame or camp stove. Folding handle, drip-free spout, " +
                "fits neatly over the folding wood stove.",
            category = "Cooking & Heating",
            imageUrls = listOf(image("kettle-1")),
            currencyCode = "EUR",
            priceRange = 29.0..29.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/6a", "Default", 29.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/7",
            handle = "solar-lantern",
            title = "Solar-Rechargeable Camp Lantern",
            description = "Collapsible, waterproof, and charges fully in a day of sun. 300 lumens with " +
                "dimmable warm and cool white modes plus a USB power-bank output.",
            category = "Lighting",
            imageUrls = listOf(image("lantern-1"), image("lantern-2")),
            currencyCode = "EUR",
            priceRange = 34.0..34.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/7a", "Default", 34.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/8",
            handle = "rechargeable-headlamp",
            title = "USB-C Rechargeable Headlamp",
            description = "500 lumens, IPX6 water resistant, motion-sensor controls so you can keep your " +
                "hands free while cooking, fixing gear, or hiking after dark.",
            category = "Lighting",
            imageUrls = listOf(image("headlamp-1")),
            currencyCode = "EUR",
            priceRange = 27.0..27.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/8a", "Default", 27.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/9",
            handle = "4-season-tunnel-tent",
            title = "4-Season Tunnel Tent, 2-Person",
            description = "Storm-rated to 80km/h winds with a full snow-skirt and vestibule for gear " +
                "storage. Aluminium poles hold their shape in real weather.",
            category = "Camping & Shelter",
            imageUrls = listOf(image("tent-1"), image("tent-2")),
            currencyCode = "EUR",
            priceRange = 389.0..389.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/9a", "Default", 389.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/10",
            handle = "down-sleeping-bag",
            title = "-10°C Down Sleeping Bag",
            description = "650-fill hydrophobic down keeps you warm even in damp conditions. Compresses " +
                "to the size of a loaf of bread for easy packing.",
            category = "Camping & Shelter",
            imageUrls = listOf(image("sleeping-bag-1")),
            currencyCode = "EUR",
            priceRange = 199.0..229.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/10a", "Regular", 199.0, "EUR", true),
                ProductVariant("gid://demo/Variant/10b", "Long", 229.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/11",
            handle = "multi-tool",
            title = "18-in-1 Stainless Multi-Tool",
            description = "Pliers, knife, saw, wire cutters, and more in a compact belt-sheathed body. " +
                "Built to be the one tool you never leave at home.",
            category = "Tools",
            imageUrls = listOf(image("multitool-1")),
            currencyCode = "EUR",
            priceRange = 45.0..45.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/11a", "Default", 45.0, "EUR", true)
            ),
            availableForSale = true
        ),
        Product(
            id = "gid://demo/Product/12",
            handle = "folding-camp-shovel",
            title = "Folding Camp Shovel & Axe Set",
            description = "Powder-coated carbon steel shovel and axe with a shared carry pouch — for fire " +
                "pits, trail clearing, or emergency vehicle recovery.",
            category = "Tools",
            imageUrls = listOf(image("shovel-1")),
            currencyCode = "EUR",
            priceRange = 39.0..39.0,
            variants = listOf(
                ProductVariant("gid://demo/Variant/12a", "Default", 39.0, "EUR", true)
            ),
            availableForSale = true
        )
    )
}
