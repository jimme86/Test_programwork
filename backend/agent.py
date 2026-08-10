"""
The sales agent: wraps the Claude API with tool-use so the model can search
the catalog, manage the shopper's cart, and complete checkout on its own,
instead of just describing what it would do.
"""
from __future__ import annotations

import json
import os
from typing import Optional

from anthropic import Anthropic

import store

MODEL = os.environ.get("CLAUDE_MODEL", "claude-sonnet-5")
MAX_TOOL_ROUNDS = 6

SYSTEM_PROMPT = """\
You are Aiko, the friendly AI sales assistant for this webshop. Your goal is \
to help visitors find the right product and complete a purchase, the way a \
great in-store salesperson would.

Guidelines:
- Ask a short clarifying question if you don't know enough to recommend well \
  (budget, use case, category), but don't interrogate the shopper — one \
  question at a time, and only when it genuinely narrows things down.
- Always ground product claims in the actual catalog via the tools. Never \
  invent products, prices, or stock levels.
- Proactively mention relevant sales/discounts (on_sale / effective_price).
- When a shopper shows intent to buy ("I'll take it", "add it", "yes"), use \
  the add_to_cart tool immediately rather than just saying you will.
- Suggest at most one relevant add-on/cross-sell per turn, only when it's \
  genuinely useful — no pressure tactics, no fake urgency, no dark patterns.
- Before calling checkout, briefly confirm the cart contents and total with \
  the shopper.
- Keep replies short and conversational (2-5 sentences), formatted for a \
  chat widget, not a document.
- If asked something unrelated to the shop, answer briefly and steer back.
"""

TOOLS = [
    {
        "name": "search_products",
        "description": "Search the product catalog by free-text query, category, and/or max price. Use this to find products matching what the shopper wants.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Free text search, e.g. 'headphones' or 'gift under 30'."},
                "category": {"type": "string", "description": "Optional exact category filter, e.g. 'audio', 'home', 'apparel'."},
                "max_price": {"type": "number", "description": "Optional maximum price filter."},
            },
        },
    },
    {
        "name": "get_product",
        "description": "Get full details for a single product by its id.",
        "input_schema": {
            "type": "object",
            "properties": {"product_id": {"type": "string"}},
            "required": ["product_id"],
        },
    },
    {
        "name": "view_cart",
        "description": "View the shopper's current cart contents and subtotal.",
        "input_schema": {"type": "object", "properties": {}},
    },
    {
        "name": "add_to_cart",
        "description": "Add a product to the shopper's cart.",
        "input_schema": {
            "type": "object",
            "properties": {
                "product_id": {"type": "string"},
                "quantity": {"type": "integer", "minimum": 1, "default": 1},
            },
            "required": ["product_id"],
        },
    },
    {
        "name": "remove_from_cart",
        "description": "Remove a product (or a quantity of it) from the shopper's cart.",
        "input_schema": {
            "type": "object",
            "properties": {
                "product_id": {"type": "string"},
                "quantity": {"type": "integer", "minimum": 1, "description": "Omit to remove all of this item."},
            },
            "required": ["product_id"],
        },
    },
    {
        "name": "checkout",
        "description": "Finalize the purchase for everything currently in the cart. Only call this after the shopper has confirmed they want to complete the order.",
        "input_schema": {
            "type": "object",
            "properties": {
                "customer_name": {"type": "string"},
                "email": {"type": "string"},
            },
        },
    },
]


def _run_tool(session_id: str, name: str, tool_input: dict) -> dict:
    if name == "search_products":
        results = store.search_products(
            query=tool_input.get("query", ""),
            category=tool_input.get("category", ""),
            max_price=tool_input.get("max_price"),
        )
        return {"results": [p.to_dict() for p in results]}
    if name == "get_product":
        product = store.get_product(tool_input["product_id"])
        return product.to_dict() if product else {"error": "not found"}
    if name == "view_cart":
        return store.cart_details(session_id)
    if name == "add_to_cart":
        return store.add_to_cart(session_id, tool_input["product_id"], tool_input.get("quantity", 1))
    if name == "remove_from_cart":
        return store.remove_from_cart(session_id, tool_input["product_id"], tool_input.get("quantity"))
    if name == "checkout":
        return store.checkout(session_id, tool_input.get("customer_name", ""), tool_input.get("email", ""))
    return {"error": f"unknown tool '{name}'"}


class ChatAgent:
    def __init__(self, api_key: Optional[str] = None):
        self.client = Anthropic(api_key=api_key or os.environ.get("ANTHROPIC_API_KEY"))
        # session_id -> list of message dicts (Anthropic Messages API format)
        self._histories: dict[str, list[dict]] = {}

    def reset(self, session_id: str) -> None:
        self._histories.pop(session_id, None)

    def send(self, session_id: str, user_message: str) -> dict:
        history = self._histories.setdefault(session_id, [])
        history.append({"role": "user", "content": user_message})

        events = []  # tool activity, useful for the UI (e.g. "added to cart")
        final_text = ""

        for _ in range(MAX_TOOL_ROUNDS):
            response = self.client.messages.create(
                model=MODEL,
                max_tokens=1024,
                system=SYSTEM_PROMPT,
                tools=TOOLS,
                messages=history,
            )

            history.append({"role": "assistant", "content": response.content})

            if response.stop_reason != "tool_use":
                final_text = "".join(
                    block.text for block in response.content if block.type == "text"
                )
                break

            tool_results = []
            for block in response.content:
                if block.type != "tool_use":
                    continue
                result = _run_tool(session_id, block.name, block.input)
                events.append({"tool": block.name, "input": block.input, "result": result})
                tool_results.append({
                    "type": "tool_result",
                    "tool_use_id": block.id,
                    "content": json.dumps(result),
                })
            history.append({"role": "user", "content": tool_results})
        else:
            final_text = "Sorry, I got a bit stuck processing that — could you rephrase?"

        return {
            "reply": final_text,
            "events": events,
            "cart": store.cart_details(session_id),
        }
