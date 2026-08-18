import type { LoaderFunctionArgs } from "@remix-run/node";
import { json } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { getConnectionWithSecrets } from "~/models/connection.server";
import { getRecentInstagramMedia } from "~/services/social/meta.server";

/**
 * Backs the storefront "social feed" theme block (extensions/social-feed-embed).
 * Shopify forwards https://offgrid-lifestyle.com/apps/social-feed/* here after
 * verifying the app proxy HMAC signature, so `authenticate.public.appProxy`
 * below is what proves the request really came from Shopify.
 */
export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.public.appProxy(request);
  if (!session) return json({ items: [] }, { status: 200 });

  const meta = await getConnectionWithSecrets(session.shop, "meta");
  if (!meta?.metadata?.igUserId) {
    return json({ items: [] });
  }

  try {
    const media = await getRecentInstagramMedia(
      meta.accessToken,
      meta.metadata.igUserId as string,
      8
    );
    return json({ items: media });
  } catch (err) {
    console.error("social feed proxy fetch failed:", err);
    return json({ items: [] });
  }
};
