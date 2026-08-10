"""
In-memory demo webshop data: product catalog, per-session carts, and orders.

This is intentionally simple (no database) so the whole project can be run
with zero external setup beyond an Anthropic API key. Swap this module out
for real database / e-commerce platform calls (Shopify, WooCommerce, etc.)
when connecting the agent to a real store.
"""
from __future__ import annotations

import itertools
import time
import uuid
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Product:
    id: str
    name: str
    category: str
    price: float
    description: str
    tags: list[str] = field(default_factory=list)
    stock: int = 25
    emoji: str = "🛍️"
    on_sale: bool = False
    sale_price: Optional[float] = None

    def effective_price(self) -> float:
        return self.sale_price if (self.on_sale and self.sale_price is not None) else self.price

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "name": self.name,
            "category": self.category,
            "price": self.price,
            "effective_price": self.effective_price(),
            "on_sale": self.on_sale,
            "description": self.description,
            "tags": self.tags,
            "stock": self.stock,
            "emoji": self.emoji,
        }


PRODUCTS: list[Product] = [
    Product("p1", "Aurora Wireless Headphones", "audio", 129.0,
            "Over-ear headphones with active noise cancellation and 40h battery life.",
            tags=["wireless", "noise-cancelling", "travel"], emoji="🎧", on_sale=True, sale_price=99.0),
    Product("p2", "Pulse Fitness Tracker", "wearables", 79.0,
            "Waterproof fitness band with heart-rate, sleep tracking, and 10-day battery.",
            tags=["fitness", "health", "waterproof"], emoji="⌚"),
    Product("p3", "Nimbus Mechanical Keyboard", "computing", 149.0,
            "Hot-swappable mechanical keyboard with hot backlighting and tactile switches.",
            tags=["keyboard", "gaming", "productivity"], emoji="⌨️"),
    Product("p4", "Drift Bluetooth Speaker", "audio", 59.0,
            "Compact splash-proof speaker with 12h playtime and punchy bass.",
            tags=["bluetooth", "portable", "outdoor"], emoji="🔊"),
    Product("p5", "Halo Smart Desk Lamp", "home", 45.0,
            "Adjustable LED desk lamp with wireless charging base and touch dimming.",
            tags=["lighting", "desk", "wireless-charging"], emoji="💡"),
    Product("p6", "Voyager Travel Backpack", "accessories", 89.0,
            "Anti-theft 25L backpack with USB charging port and laptop sleeve.",
            tags=["travel", "laptop", "commute"], emoji="🎒"),
    Product("p7", "Zephyr Ultralight Jacket", "apparel", 119.0,
            "Packable windbreaker, water-resistant, fits in its own pocket.",
            tags=["outdoor", "packable", "rain"], emoji="🧥"),
    Product("p8", "Cascade Stainless Bottle", "home", 25.0,
            "Insulated 750ml bottle, keeps drinks cold 24h or hot 12h.",
            tags=["hydration", "eco", "gift"], emoji="🍶", on_sale=True, sale_price=19.0),
    Product("p9", "Orbit Wireless Charger Pad", "computing", 29.0,
            "15W fast wireless charging pad, compatible with most phone cases.",
            tags=["charging", "desk", "phone"], emoji="🔌"),
    Product("p10", "Summit Trail Running Shoes", "apparel", 139.0,
            "Lightweight trail runners with reinforced grip and breathable mesh.",
            tags=["running", "outdoor", "shoes"], emoji="👟"),
    Product("p11", "Lumen Smart Bulb 4-Pack", "home", 39.0,
            "Color-changing WiFi smart bulbs, works with voice assistants.",
            tags=["smart-home", "lighting"], emoji="💡"),
    Product("p12", "Basecamp Camping Tent (2P)", "outdoor", 159.0,
            "2-person freestanding tent, sets up in under 5 minutes.",
            tags=["camping", "outdoor", "hiking"], emoji="⛺"),
]

_PRODUCTS_BY_ID = {p.id: p for p in PRODUCTS}

# session_id -> {product_id: quantity}
CARTS: dict[str, dict[str, int]] = {}

# order_id -> order dict
ORDERS: dict[str, dict] = {}


def list_products() -> list[Product]:
    return PRODUCTS


def search_products(query: str = "", category: str = "", max_price: Optional[float] = None) -> list[Product]:
    query = (query or "").lower().strip()
    category = (category or "").lower().strip()
    results = []
    for p in PRODUCTS:
        if category and p.category.lower() != category:
            continue
        if max_price is not None and p.effective_price() > max_price:
            continue
        if query:
            haystack = " ".join([p.name, p.description, p.category, *p.tags]).lower()
            if query not in haystack:
                continue
        results.append(p)
    return results


def get_product(product_id: str) -> Optional[Product]:
    return _PRODUCTS_BY_ID.get(product_id)


def get_cart(session_id: str) -> dict[str, int]:
    return CARTS.setdefault(session_id, {})


def cart_details(session_id: str) -> dict:
    cart = get_cart(session_id)
    items = []
    subtotal = 0.0
    for pid, qty in cart.items():
        product = _PRODUCTS_BY_ID.get(pid)
        if not product:
            continue
        line_total = product.effective_price() * qty
        subtotal += line_total
        items.append({
            **product.to_dict(),
            "quantity": qty,
            "line_total": round(line_total, 2),
        })
    return {"items": items, "subtotal": round(subtotal, 2), "item_count": sum(cart.values())}


def add_to_cart(session_id: str, product_id: str, quantity: int = 1) -> dict:
    product = get_product(product_id)
    if not product:
        return {"error": f"No product with id '{product_id}'"}
    if quantity <= 0:
        return {"error": "Quantity must be positive"}
    cart = get_cart(session_id)
    cart[product_id] = cart.get(product_id, 0) + quantity
    return {"ok": True, "cart": cart_details(session_id)}


def remove_from_cart(session_id: str, product_id: str, quantity: Optional[int] = None) -> dict:
    cart = get_cart(session_id)
    if product_id not in cart:
        return {"error": f"'{product_id}' is not in the cart"}
    if quantity is None or quantity >= cart[product_id]:
        del cart[product_id]
    else:
        cart[product_id] -= quantity
    return {"ok": True, "cart": cart_details(session_id)}


def clear_cart(session_id: str) -> None:
    CARTS[session_id] = {}


def checkout(session_id: str, customer_name: str = "", email: str = "") -> dict:
    details = cart_details(session_id)
    if not details["items"]:
        return {"error": "Cart is empty, nothing to check out."}
    order_id = f"ORD-{uuid.uuid4().hex[:8].upper()}"
    order = {
        "order_id": order_id,
        "created_at": time.time(),
        "customer_name": customer_name or "Guest",
        "email": email or None,
        "items": details["items"],
        "subtotal": details["subtotal"],
        "status": "confirmed",
    }
    ORDERS[order_id] = order
    clear_cart(session_id)
    return {"ok": True, "order": order}


def new_session_id() -> str:
    return uuid.uuid4().hex
