import type { SyncableProduct } from "./social/types";

const STORE_DOMAIN = process.env.SHOP_CUSTOM_DOMAIN || "offgrid-lifestyle.com";

interface ShopifyProductWebhookPayload {
  id: number | string;
  title: string;
  body_html?: string;
  vendor?: string;
  handle: string;
  status?: string;
  variants?: { price?: string; sku?: string; inventory_quantity?: number }[];
  image?: { src?: string };
  images?: { src?: string }[];
}

/** Normalizes the raw products/create|update webhook payload into the shape the social services expect. */
export function toSyncableProduct(_shop: string, payload: unknown): SyncableProduct {
  const p = payload as ShopifyProductWebhookPayload;
  const firstVariant = p.variants?.[0];
  const inStock = (firstVariant?.inventory_quantity ?? 0) > 0;

  return {
    id: String(p.id),
    title: p.title,
    description: stripHtml(p.body_html ?? ""),
    vendor: p.vendor ?? "Offgrid Lifestyle",
    price: firstVariant?.price ?? "0.00",
    currency: "EUR",
    url: `https://${STORE_DOMAIN}/products/${p.handle}`,
    imageUrl: p.image?.src ?? p.images?.[0]?.src,
    sku: firstVariant?.sku,
    availability: inStock ? "in_stock" : "out_of_stock",
  };
}

function stripHtml(html: string) {
  return html.replace(/<[^>]*>/g, "").trim();
}
