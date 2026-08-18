import { getConnectionWithSecrets } from "~/models/connection.server";
import { getSettings, renderPostTemplate } from "~/models/settings.server";
import { logActivity } from "~/models/activityLog.server";
import type { SyncableProduct } from "./social/types";
import {
  syncProductToCatalog,
  removeProductFromCatalog,
  postToFacebookPage,
  postToInstagram,
} from "./social/meta.server";
import { syncProductToTikTokShop, postToTikTok } from "./social/tiktok.server";
import { postProductUpdate as postYouTubeUpdate } from "./social/youtube.server";

/**
 * Called from the products/create and products/update webhook routes.
 * Pushes the product into every connected + catalog-sync-enabled platform,
 * and (if enabled) fires the "new product" auto-post on create.
 */
export async function handleProductUpsert(
  shop: string,
  product: SyncableProduct,
  isNew: boolean
) {
  const settings = await getSettings(shop);
  const caption = renderPostTemplate(settings.postTemplate, {
    title: product.title,
    price: `${product.price} ${product.currency}`,
    url: product.url,
  });

  await Promise.all([
    runMeta(shop, product, caption, settings, isNew),
    runTikTok(shop, product, caption, settings, isNew),
    runYouTube(shop, product, caption, settings, isNew),
  ]);
}

export async function handleProductDelete(shop: string, productId: string, sku?: string) {
  const meta = await getConnectionWithSecrets(shop, "meta");
  if (!meta) return;
  const catalogId = meta.metadata?.catalogId as string | undefined;
  if (!catalogId) return;
  try {
    await removeProductFromCatalog(meta.accessToken, catalogId, sku || productId);
    await logActivity({ shop, platform: "meta", action: "catalog_sync", productId, status: "success", message: "Removed from catalog" });
  } catch (err) {
    await logActivity({ shop, platform: "meta", action: "error", productId, status: "error", message: (err as Error).message });
  }
}

async function runMeta(
  shop: string,
  product: SyncableProduct,
  caption: string,
  settings: Awaited<ReturnType<typeof getSettings>>,
  isNew: boolean
) {
  const conn = await getConnectionWithSecrets(shop, "meta");
  if (!conn) return;
  const catalogId = (conn.metadata?.catalogId as string) || process.env.META_CATALOG_ID;
  const pageId = conn.metadata?.pageId as string | undefined;
  const igUserId = conn.metadata?.igUserId as string | undefined;

  if (settings.syncProductsMeta && catalogId) {
    try {
      await syncProductToCatalog(conn.accessToken, catalogId, product);
      await logActivity({ shop, platform: "meta", action: "catalog_sync", productId: product.id, status: "success" });
    } catch (err) {
      await logActivity({ shop, platform: "meta", action: "error", productId: product.id, status: "error", message: (err as Error).message });
    }
  }

  if (isNew && settings.autoPostFacebook && pageId) {
    const result = await postToFacebookPage(conn.accessToken, pageId, caption, product.imageUrl);
    await logActivity({ shop, platform: "meta", action: "auto_post", productId: product.id, status: result.ok ? "success" : "error", message: result.error });
  }

  if (isNew && settings.autoPostInstagram && igUserId && product.imageUrl) {
    const result = await postToInstagram(conn.accessToken, igUserId, caption, product.imageUrl);
    await logActivity({ shop, platform: "meta", action: "auto_post", productId: product.id, status: result.ok ? "success" : "error", message: result.error });
  }
}

async function runTikTok(
  shop: string,
  product: SyncableProduct,
  caption: string,
  settings: Awaited<ReturnType<typeof getSettings>>,
  isNew: boolean
) {
  const conn = await getConnectionWithSecrets(shop, "tiktok");
  if (!conn) return;
  const shopId = (conn.metadata?.shopId as string) || process.env.TIKTOK_SHOP_ID;

  if (settings.syncProductsTikTok && shopId) {
    try {
      await syncProductToTikTokShop(conn.accessToken, shopId, product);
      await logActivity({ shop, platform: "tiktok", action: "catalog_sync", productId: product.id, status: "success" });
    } catch (err) {
      await logActivity({ shop, platform: "tiktok", action: "error", productId: product.id, status: "error", message: (err as Error).message });
    }
  }

  if (isNew && settings.autoPostTikTok && product.imageUrl) {
    const result = await postToTikTok(conn.accessToken, caption, product.imageUrl);
    await logActivity({ shop, platform: "tiktok", action: "auto_post", productId: product.id, status: result.ok ? "success" : "error", message: result.error });
  }
}

async function runYouTube(
  shop: string,
  product: SyncableProduct,
  caption: string,
  settings: Awaited<ReturnType<typeof getSettings>>,
  isNew: boolean
) {
  const conn = await getConnectionWithSecrets(shop, "youtube");
  if (!conn) return;

  // YouTube Shopping product sync is configured by linking Merchant Center
  // in YouTube Studio (see docs/SOCIAL_CHANNEL_SETUP.md) rather than a
  // direct write API, so `syncProductsYouTube` only gates the auto-post here.
  if (isNew && settings.autoPostYouTube) {
    const result = await postYouTubeUpdate(conn.accessToken, conn.refreshToken, product, caption);
    await logActivity({ shop, platform: "youtube", action: "auto_post", productId: product.id, status: result.ok ? "success" : "error", message: result.error });
  }
}
