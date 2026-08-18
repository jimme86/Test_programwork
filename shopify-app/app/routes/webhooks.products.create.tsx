import type { ActionFunctionArgs } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { handleProductUpsert } from "~/services/productSync.server";
import { toSyncableProduct } from "~/services/shopifyProduct.server";

export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, payload } = await authenticate.webhook(request);
  const product = toSyncableProduct(shop, payload);
  // Fire-and-forget: acknowledge the webhook immediately, sync in the background.
  handleProductUpsert(shop, product, /* isNew */ true).catch((err) =>
    console.error(`[products/create] sync failed for ${shop}:`, err)
  );
  return new Response(null, { status: 200 });
};
