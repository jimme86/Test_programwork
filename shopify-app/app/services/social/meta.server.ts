import type { SyncableProduct, PostResult } from "./types";

// Facebook & Instagram integration via the Meta Graph API.
// Docs: https://developers.facebook.com/docs/commerce-platform (catalog)
//       https://developers.facebook.com/docs/instagram-api (content publishing)
const GRAPH_API_VERSION = "v21.0";
const GRAPH_BASE = `https://graph.facebook.com/${GRAPH_API_VERSION}`;

export function getMetaOAuthUrl(state: string) {
  const params = new URLSearchParams({
    client_id: process.env.META_APP_ID || "",
    redirect_uri: process.env.META_REDIRECT_URI || "",
    state,
    scope: [
      "catalog_management",
      "pages_show_list",
      "pages_read_engagement",
      "pages_manage_posts",
      "instagram_basic",
      "instagram_content_publish",
      "business_management",
    ].join(","),
    response_type: "code",
  });
  return `https://www.facebook.com/${GRAPH_API_VERSION}/dialog/oauth?${params}`;
}

export async function exchangeMetaCode(code: string) {
  const params = new URLSearchParams({
    client_id: process.env.META_APP_ID || "",
    client_secret: process.env.META_APP_SECRET || "",
    redirect_uri: process.env.META_REDIRECT_URI || "",
    code,
  });
  const res = await fetch(`${GRAPH_BASE}/oauth/access_token?${params}`);
  if (!res.ok) throw new Error(`Meta token exchange failed: ${await res.text()}`);
  const shortLived = (await res.json()) as { access_token: string };

  // Exchange for a long-lived (~60 day) token so the shop doesn't have to
  // reconnect constantly; refresh it on a schedule before it expires.
  const longLivedParams = new URLSearchParams({
    grant_type: "fb_exchange_token",
    client_id: process.env.META_APP_ID || "",
    client_secret: process.env.META_APP_SECRET || "",
    fb_exchange_token: shortLived.access_token,
  });
  const longRes = await fetch(`${GRAPH_BASE}/oauth/access_token?${longLivedParams}`);
  if (!longRes.ok) throw new Error(`Meta long-lived exchange failed: ${await longRes.text()}`);
  return (await longRes.json()) as { access_token: string; expires_in: number };
}

/** Upserts a product into the connected Commerce Manager catalog (powers Facebook & Instagram Shop). */
export async function syncProductToCatalog(
  accessToken: string,
  catalogId: string,
  product: SyncableProduct
) {
  const body = {
    requests: [
      {
        method: "UPDATE",
        retailer_id: product.sku || product.id,
        data: {
          name: product.title,
          description: product.description,
          url: product.url,
          image_url: product.imageUrl,
          price: `${product.price} ${product.currency}`,
          availability: product.availability,
          brand: product.vendor,
          condition: "new",
        },
      },
    ],
  };

  const res = await fetch(`${GRAPH_BASE}/${catalogId}/items_batch`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...body, access_token: accessToken }),
  });
  if (!res.ok) throw new Error(`Meta catalog sync failed: ${await res.text()}`);
  return res.json();
}

export async function removeProductFromCatalog(
  accessToken: string,
  catalogId: string,
  retailerId: string
) {
  const body = { requests: [{ method: "DELETE", retailer_id: retailerId }] };
  const res = await fetch(`${GRAPH_BASE}/${catalogId}/items_batch`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...body, access_token: accessToken }),
  });
  if (!res.ok) throw new Error(`Meta catalog removal failed: ${await res.text()}`);
  return res.json();
}

/** Publishes a photo post to a connected Facebook Page. */
export async function postToFacebookPage(
  accessToken: string,
  pageId: string,
  caption: string,
  imageUrl?: string
): Promise<PostResult> {
  try {
    const endpoint = imageUrl ? `${GRAPH_BASE}/${pageId}/photos` : `${GRAPH_BASE}/${pageId}/feed`;
    const params: Record<string, string> = imageUrl
      ? { url: imageUrl, caption, access_token: accessToken }
      : { message: caption, access_token: accessToken };
    const res = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams(params),
    });
    const json = (await res.json()) as { id?: string; post_id?: string; error?: { message: string } };
    if (!res.ok || json.error) throw new Error(json.error?.message || "Unknown Facebook API error");
    return { platform: "meta_facebook", ok: true, externalId: json.post_id || json.id };
  } catch (err) {
    return { platform: "meta_facebook", ok: false, error: (err as Error).message };
  }
}

/**
 * Publishes a single-image post to a connected Instagram Business account.
 * This is a two-step Graph API flow: create a media container, then publish it.
 */
export async function postToInstagram(
  accessToken: string,
  igUserId: string,
  caption: string,
  imageUrl: string
): Promise<PostResult> {
  try {
    const createRes = await fetch(`${GRAPH_BASE}/${igUserId}/media`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ image_url: imageUrl, caption, access_token: accessToken }),
    });
    const created = (await createRes.json()) as { id?: string; error?: { message: string } };
    if (!createRes.ok || created.error || !created.id) {
      throw new Error(created.error?.message || "Failed to create IG media container");
    }

    const publishRes = await fetch(`${GRAPH_BASE}/${igUserId}/media_publish`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ creation_id: created.id, access_token: accessToken }),
    });
    const published = (await publishRes.json()) as { id?: string; error?: { message: string } };
    if (!publishRes.ok || published.error) {
      throw new Error(published.error?.message || "Failed to publish IG media");
    }
    return { platform: "meta_instagram", ok: true, externalId: published.id };
  } catch (err) {
    return { platform: "meta_instagram", ok: false, error: (err as Error).message };
  }
}

/** Recent media for the storefront social feed embed (used by the app proxy route). */
export async function getRecentInstagramMedia(accessToken: string, igUserId: string, limit = 8) {
  const params = new URLSearchParams({
    fields: "id,caption,media_type,media_url,permalink,timestamp",
    limit: String(limit),
    access_token: accessToken,
  });
  const res = await fetch(`${GRAPH_BASE}/${igUserId}/media?${params}`);
  if (!res.ok) throw new Error(`Failed to fetch Instagram media: ${await res.text()}`);
  const json = (await res.json()) as { data: unknown[] };
  return json.data;
}
