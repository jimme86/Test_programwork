import type { ActionFunctionArgs } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { handleProductUpsert } from "~/services/productSync.server";
import { toSyncableProduct } from "~/services/shopifyProduct.server";

export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, payload } = await authenticate.webhook(request);
  const product = toSyncableProduct(shop, payload);
  // Updates only re-sync the catalog entry; auto-post is for new products only.
  handleProductUpsert(shop, product, /* isNew */ false).catch((err) =>
    console.error(`[products/update] sync failed for ${shop}:`, err)
  );
  return new Response(null, { status: 200 });
};
