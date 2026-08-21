# Test_programwork — AI Webshop Sales Agent

An AI sales agent ("Aiko") embedded in a small demo webshop. It's built with
the **Claude API** using tool-use, so it doesn't just chat — it can actually
search the product catalog, add items to a shopper's cart, and complete
checkout on their behalf, the way a good in-store salesperson would.

## What's here

- `backend/` — FastAPI app
  - `store.py` — in-memory product catalog, cart, and order storage (no DB needed)
  - `agent.py` — the Claude-powered sales agent (system prompt + tool-use loop)
  - `main.py` — REST API (`/api/products`, `/api/cart`, `/api/checkout`, `/api/chat`) and serves the storefront
- `frontend/` — a plain HTML/CSS/JS storefront with a product grid, cart panel,
  and a floating "Ask Aiko" chat widget that talks to the agent

This ships with a demo catalog (headphones, backpacks, etc.) so it runs
standalone. To point it at a real store, swap the functions in `store.py`
for calls to your actual platform (Shopify, WooCommerce, your own DB/API).

## How the agent decides to sell

`backend/agent.py` gives Claude five tools: `search_products`, `get_product`,
`view_cart`, `add_to_cart`, `remove_from_cart`, and `checkout`. The system
prompt tells it to act like a helpful (not pushy) salesperson: ask one
clarifying question when needed, ground every claim in real catalog data,
call `add_to_cart` the moment a shopper shows buying intent instead of just
saying it will, suggest at most one relevant add-on, and confirm the cart
before checking out.

## Setup

1. **Python 3.10+** and pip.
2. Install dependencies:
   ```bash
   cd backend
   pip install -r requirements.txt
   ```
3. Add your Anthropic API key:
   ```bash
   cp .env.example .env
   # then edit .env and set ANTHROPIC_API_KEY=sk-ant-...
   ```
4. Run the server:
   ```bash
   uvicorn main:app --reload --port 8000
   ```
5. Open http://localhost:8000 — browse the shop, or click **Ask Aiko** in the
   bottom-right corner and try things like:
   - "I need headphones under $100"
   - "add it to my cart"
   - "what else goes well with that?"
   - "ok, check out"

## API summary

| Method | Path                | Purpose                          |
|--------|---------------------|-----------------------------------|
| GET    | `/api/products`     | List/search products (`query`, `category`, `max_price`) |
| GET    | `/api/products/{id}`| Product detail                    |
| GET    | `/api/cart`         | Current cart (needs `X-Session-Id` header) |
| POST   | `/api/cart/add`     | `{product_id, quantity}`          |
| POST   | `/api/cart/remove`  | `{product_id, quantity?}`         |
| POST   | `/api/checkout`     | `{customer_name?, email?}` → creates a demo order |
| POST   | `/api/chat`         | `{message}` → agent reply + any tool actions taken |
| POST   | `/api/chat/reset`   | Clears the agent's conversation memory for this session |

All cart/chat endpoints are scoped by the `X-Session-Id` header, which the
frontend generates per browser via `localStorage`.

## Social Media Automation

`backend/social/` turns the drafted campaign into a recurring agent job:

- `content_calendar.json` — the post queue (date, platform, caption, image, link, status)
- `publisher.py` — calls the Facebook Graph API, Instagram Graph API, and
  Pinterest API v5 to actually publish a due post, and marks it `published`
  in the calendar so it's never posted twice

A Routine is scheduled (Mon/Wed/Fri) to resume this session, check the
calendar for anything due, and publish it — that's the "agent posts on a
schedule" loop. Each run is idempotent: it only touches `scheduled` entries
whose date has arrived, and skips gracefully (reporting what's missing,
never crashing or spamming) if credentials aren't configured yet.

### Getting credentials so it actually posts

Nothing publishes for real until these exist — until then the scheduled
run just reports "waiting on credentials". None of this can be done via
API; each requires you personally, once:

1. **Facebook Page + Instagram Business account** — create the Page (see
   the account-setup checklist earlier in this project's history), then
   convert your Instagram account to a Business account and link it to
   that Page in Meta Business Suite.
2. **Meta developer app** — create one at developers.facebook.com, add
   the Page as a test asset (no App Review needed while you're the only
   user posting to your own Page), and generate a long-lived Page access
   token with `pages_manage_posts` + `instagram_content_publish` scopes.
3. **Pinterest developer app** — create one at developers.pinterest.com,
   generate an access token with the `pins:write` scope, and note your
   target board's ID.
4. Add all five values from `backend/.env.example`'s social section to
   **this Claude Code Environment's configuration** (not just your local
   `.env`) — that's what makes them visible to the scheduled run, since
   each firing resumes this environment rather than your local machine.

### Adding future posts

Append new entries with `"status": "scheduled"` to `content_calendar.json`
and push — the next scheduled run will pick them up. New posts get drafted
the same way the first batch was: fresh keyword/product research, grounded
in whatever's actually live in the store at the time.

## Notes / next steps

- Checkout is a **demo/no-payment flow** — it creates an in-memory order
  record and clears the cart. Wire in a real payment processor (Stripe,
  etc.) before using this with real money.
- Storage is in-memory and resets when the server restarts. Swap `store.py`
  for a real database for production use.
- Model defaults to `claude-sonnet-5`; override with `CLAUDE_MODEL` in `.env`.
