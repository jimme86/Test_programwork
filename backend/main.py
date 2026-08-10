from __future__ import annotations

import os
from pathlib import Path
from typing import Optional

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from pydantic import BaseModel

import store
from agent import ChatAgent

load_dotenv()

app = FastAPI(title="AI Webshop Sales Agent")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

_agent: Optional[ChatAgent] = None


def get_agent() -> ChatAgent:
    global _agent
    if _agent is None:
        if not os.environ.get("ANTHROPIC_API_KEY"):
            raise HTTPException(
                status_code=500,
                detail="ANTHROPIC_API_KEY is not set. Copy backend/.env.example to backend/.env and add your key.",
            )
        _agent = ChatAgent()
    return _agent


def session_from_header(x_session_id: Optional[str]) -> str:
    if not x_session_id:
        raise HTTPException(status_code=400, detail="Missing X-Session-Id header")
    return x_session_id


# ---------- Catalog & cart REST API (used by the storefront UI directly) ----------

@app.get("/api/session")
def create_session():
    return {"session_id": store.new_session_id()}


@app.get("/api/products")
def api_list_products(query: str = "", category: str = "", max_price: Optional[float] = None):
    if query or category or max_price is not None:
        products = store.search_products(query=query, category=category, max_price=max_price)
    else:
        products = store.list_products()
    return {"products": [p.to_dict() for p in products]}


@app.get("/api/products/{product_id}")
def api_get_product(product_id: str):
    product = store.get_product(product_id)
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product.to_dict()


@app.get("/api/cart")
def api_get_cart(x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    return store.cart_details(session_id)


class CartAddRequest(BaseModel):
    product_id: str
    quantity: int = 1


@app.post("/api/cart/add")
def api_add_to_cart(body: CartAddRequest, x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    result = store.add_to_cart(session_id, body.product_id, body.quantity)
    if "error" in result:
        raise HTTPException(status_code=400, detail=result["error"])
    return result


class CartRemoveRequest(BaseModel):
    product_id: str
    quantity: Optional[int] = None


@app.post("/api/cart/remove")
def api_remove_from_cart(body: CartRemoveRequest, x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    result = store.remove_from_cart(session_id, body.product_id, body.quantity)
    if "error" in result:
        raise HTTPException(status_code=400, detail=result["error"])
    return result


class CheckoutRequest(BaseModel):
    customer_name: str = ""
    email: str = ""


@app.post("/api/checkout")
def api_checkout(body: CheckoutRequest, x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    result = store.checkout(session_id, body.customer_name, body.email)
    if "error" in result:
        raise HTTPException(status_code=400, detail=result["error"])
    return result


# ---------- Chat agent API ----------

class ChatRequest(BaseModel):
    message: str


@app.post("/api/chat")
def api_chat(body: ChatRequest, x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    agent = get_agent()
    if not body.message.strip():
        raise HTTPException(status_code=400, detail="Empty message")
    return agent.send(session_id, body.message)


@app.post("/api/chat/reset")
def api_chat_reset(x_session_id: Optional[str] = Header(default=None)):
    session_id = session_from_header(x_session_id)
    if _agent is not None:
        _agent.reset(session_id)
    return {"ok": True}


# ---------- Static storefront ----------

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"
if FRONTEND_DIR.exists():
    app.mount("/static", StaticFiles(directory=str(FRONTEND_DIR)), name="static")

    @app.get("/")
    def index():
        return FileResponse(str(FRONTEND_DIR / "index.html"))
