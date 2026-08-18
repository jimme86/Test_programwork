export interface SyncableProduct {
  id: string; // Shopify GID or numeric id
  title: string;
  description: string;
  vendor: string;
  price: string;
  currency: string;
  url: string; // canonical product URL on offgrid-lifestyle.com
  imageUrl?: string;
  sku?: string;
  availability: "in_stock" | "out_of_stock";
}

export interface PostResult {
  platform: "meta_facebook" | "meta_instagram" | "tiktok" | "youtube";
  ok: boolean;
  externalId?: string;
  error?: string;
}
