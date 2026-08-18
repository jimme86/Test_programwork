import type { ActionFunctionArgs } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { handleProductDelete } from "~/services/productSync.server";

export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, payload } = await authenticate.webhook(request);
  const productId = String((payload as { id: number | string }).id);
  handleProductDelete(shop, productId).catch((err) =>
    console.error(`[products/delete] removal failed for ${shop}:`, err)
  );
  return new Response(null, { status: 200 });
};
