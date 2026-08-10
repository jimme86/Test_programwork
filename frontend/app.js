const API_BASE = ""; // same origin

function getSessionId() {
  let id = localStorage.getItem("session_id");
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem("session_id", id);
  }
  return id;
}
const SESSION_ID = getSessionId();

function headers(extra = {}) {
  return { "Content-Type": "application/json", "X-Session-Id": SESSION_ID, ...extra };
}

function money(n) {
  return `$${Number(n).toFixed(2)}`;
}

// ---------- Products ----------

let allProducts = [];

async function loadProducts() {
  const query = document.getElementById("search-input").value;
  const category = document.getElementById("category-select").value;
  const params = new URLSearchParams();
  if (query) params.set("query", query);
  if (category) params.set("category", category);
  const res = await fetch(`${API_BASE}/api/products?${params.toString()}`);
  const data = await res.json();
  allProducts = data.products;
  renderProducts(allProducts);
  populateCategories(allProducts);
}

function populateCategories(products) {
  const select = document.getElementById("category-select");
  const current = select.value;
  const categories = [...new Set(products.map((p) => p.category))].sort();
  select.innerHTML = `<option value="">All categories</option>` +
    categories.map((c) => `<option value="${c}">${c}</option>`).join("");
  select.value = current;
}

function renderProducts(products) {
  const grid = document.getElementById("product-grid");
  grid.innerHTML = products.map((p) => `
    <div class="product-card">
      <div class="product-emoji">${p.emoji}</div>
      <div class="product-name">${p.name}</div>
      <div class="product-desc">${p.description}</div>
      <div class="product-price">
        <span class="price-now">${money(p.effective_price)}</span>
        ${p.on_sale ? `<span class="price-was">${money(p.price)}</span><span class="sale-badge">SALE</span>` : ""}
      </div>
      <button class="add-btn" data-id="${p.id}">Add to cart</button>
    </div>
  `).join("");

  grid.querySelectorAll(".add-btn").forEach((btn) => {
    btn.addEventListener("click", () => addToCart(btn.dataset.id, 1));
  });
}

// ---------- Cart ----------

async function refreshCart() {
  const res = await fetch(`${API_BASE}/api/cart`, { headers: headers() });
  const cart = await res.json();
  renderCart(cart);
}

function renderCart(cart) {
  document.getElementById("cart-count").textContent = cart.item_count || 0;
  document.getElementById("cart-subtotal").textContent = money(cart.subtotal || 0);
  const itemsEl = document.getElementById("cart-items");
  if (!cart.items || cart.items.length === 0) {
    itemsEl.innerHTML = `<p style="color: var(--muted)">Your cart is empty.</p>`;
    return;
  }
  itemsEl.innerHTML = cart.items.map((item) => `
    <div class="cart-item">
      <div style="font-size: 1.4rem">${item.emoji}</div>
      <div style="flex: 1">
        <div class="cart-item-name">${item.name}</div>
        <div class="cart-item-meta">${item.quantity} × ${money(item.effective_price)} = ${money(item.line_total)}</div>
      </div>
      <button class="icon-btn" data-id="${item.id}" title="Remove">✕</button>
    </div>
  `).join("");

  itemsEl.querySelectorAll("button[data-id]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await fetch(`${API_BASE}/api/cart/remove`, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify({ product_id: btn.dataset.id }),
      });
      refreshCart();
    });
  });
}

async function addToCart(productId, quantity = 1) {
  await fetch(`${API_BASE}/api/cart/add`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({ product_id: productId, quantity }),
  });
  refreshCart();
}

async function doCheckout() {
  const res = await fetch(`${API_BASE}/api/checkout`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({}),
  });
  const data = await res.json();
  if (!res.ok) {
    alert(data.detail || "Checkout failed");
    return;
  }
  alert(`Order ${data.order.order_id} confirmed! Total: ${money(data.order.subtotal)}`);
  refreshCart();
}

// ---------- Chat (AI sales agent) ----------

function addChatMessage(text, cls) {
  const messages = document.getElementById("chat-messages");
  const div = document.createElement("div");
  div.className = `chat-msg ${cls}`;
  div.textContent = text;
  messages.appendChild(div);
  messages.scrollTop = messages.scrollHeight;
}

async function sendChatMessage(text) {
  addChatMessage(text, "user");
  const res = await fetch(`${API_BASE}/api/chat`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({ message: text }),
  });
  const data = await res.json();
  if (!res.ok) {
    addChatMessage(data.detail || "Something went wrong.", "agent");
    return;
  }
  for (const event of data.events || []) {
    if (event.tool === "add_to_cart" && event.result?.ok) {
      addChatMessage(`🛒 Added to cart`, "event");
    }
    if (event.tool === "checkout" && event.result?.ok) {
      addChatMessage(`✅ Order ${event.result.order.order_id} placed!`, "event");
    }
  }
  addChatMessage(data.reply, "agent");
  renderCart(data.cart);
}

// ---------- Wiring ----------

document.addEventListener("DOMContentLoaded", () => {
  loadProducts();
  refreshCart();

  document.getElementById("search-input").addEventListener("input", debounce(loadProducts, 250));
  document.getElementById("category-select").addEventListener("change", loadProducts);

  document.getElementById("cart-toggle").addEventListener("click", () => {
    document.getElementById("cart-panel").classList.remove("hidden");
  });
  document.getElementById("cart-close").addEventListener("click", () => {
    document.getElementById("cart-panel").classList.add("hidden");
  });
  document.getElementById("checkout-btn").addEventListener("click", doCheckout);

  document.getElementById("chat-fab").addEventListener("click", () => {
    document.getElementById("chat-panel").classList.toggle("hidden");
  });
  document.getElementById("chat-close").addEventListener("click", () => {
    document.getElementById("chat-panel").classList.add("hidden");
  });
  document.getElementById("chat-form").addEventListener("submit", (e) => {
    e.preventDefault();
    const input = document.getElementById("chat-input");
    const text = input.value.trim();
    if (!text) return;
    input.value = "";
    sendChatMessage(text);
  });
});

function debounce(fn, ms) {
  let t;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), ms);
  };
}
