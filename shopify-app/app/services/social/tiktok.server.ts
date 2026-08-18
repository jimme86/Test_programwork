import type { SyncableProduct, PostResult } from "./types";

// TikTok for Business integration: TikTok Shop Partner API (catalog/product
// sync) + Content Posting API (auto-post). Docs:
// https://partner.tiktokshop.com/docv2  |  https://developers.tiktok.com/doc/content-posting-api-get-started
const TIKTOK_AUTH_BASE = "https://www.tiktok.com/v2/auth/authorize";
const TIKTOK_TOKEN_URL = "https://open.tiktokapis.com/v2/oauth/token/";
const TIKTOK_API_BASE = "https://open.tiktokapis.com/v2";

export function getTikTokOAuthUrl(state: string) {
  const params = new URLSearchParams({
    client_key: process.env.TIKTOK_APP_ID || "",
    redirect_uri: process.env.TIKTOK_REDIRECT_URI || "",
    state,
    scope: "user.info.basic,video.publish,product.catalog",
    response_type: "code",
  });
  return `${TIKTOK_AUTH_BASE}?${params}`;
}

export async function exchangeTikTokCode(code: string) {
  const res = await fetch(TIKTOK_TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      client_key: process.env.TIKTOK_APP_ID || "",
      client_secret: process.env.TIKTOK_APP_SECRET || "",
      code,
      grant_type: "authorization_code",
      redirect_uri: process.env.TIKTOK_REDIRECT_URI || "",
    }),
  });
  if (!res.ok) throw new Error(`TikTok token exchange failed: ${await res.text()}`);
  return (await res.json()) as {
    access_token: string;
    refresh_token: string;
    expires_in: number;
    open_id: string;
  };
}

export async function refreshTikTokToken(refreshToken: string) {
  const res = await fetch(TIKTOK_TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      client_key: process.env.TIKTOK_APP_ID || "",
      client_secret: process.env.TIKTOK_APP_SECRET || "",
      grant_type: "refresh_token",
      refresh_token: refreshToken,
    }),
  });
  if (!res.ok) throw new Error(`TikTok token refresh failed: ${await res.text()}`);
  return (await res.json()) as { access_token: string; refresh_token: string; expires_in: number };
}

/** Upserts a product into the connected TikTok Shop catalog. */
export async function syncProductToTikTokShop(
  accessToken: string,
  shopId: string,
  product: SyncableProduct
) {
  const res = await fetch(
    `https://open-api.tiktokglobalshop.com/product/202309/products`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-tts-access-token": accessToken,
      },
      body: JSON.stringify({
        shop_id: shopId,
        product_name: product.title,
        description: product.description,
        main_images: product.imageUrl ? [{ uri: product.imageUrl }] : [],
        skus: [
          {
            seller_sku: product.sku || product.id,
            price: { amount: product.price, currency: product.currency },
            inventory: [{ quantity: product.availability === "in_stock" ? 1 : 0 }],
          },
        ],
      }),
    }
  );
  if (!res.ok) throw new Error(`TikTok Shop catalog sync failed: ${await res.text()}`);
  return res.json();
}

/**
 * Content Posting API: publishes a photo post (product announcement) to the
 * connected creator/business account. TikTok video/photo posts created via
 * the API for non pre-approved apps land in the user's inbox as a draft
 * unless the app has been granted direct-post scope.
 */
export async function postToTikTok(
  accessToken: string,
  caption: string,
  imageUrl: string
): Promise<PostResult> {
  try {
    const res = await fetch(`${TIKTOK_API_BASE}/post/publish/content/init/`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=UTF-8",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({
        post_info: {
          title: caption,
          privacy_level: "PUBLIC_TO_EVERYONE",
        },
        source_info: {
          source: "PULL_FROM_URL",
          photo_images: [imageUrl],
          photo_cover_index: 0,
        },
        post_mode: "DIRECT_POST",
        media_type: "PHOTO",
      }),
    });
    const json = (await res.json()) as {
      data?: { publish_id?: string };
      error?: { code?: string; message?: string };
    };
    if (!res.ok || (json.error && json.error.code !== "ok")) {
      throw new Error(json.error?.message || "Unknown TikTok API error");
    }
    return { platform: "tiktok", ok: true, externalId: json.data?.publish_id };
  } catch (err) {
    return { platform: "tiktok", ok: false, error: (err as Error).message };
  }
}
