import type { LoaderFunctionArgs } from "@remix-run/node";
import { redirect } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { getTikTokOAuthUrl } from "~/services/social/tiktok.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const state = Buffer.from(JSON.stringify({ shop: session.shop, nonce: crypto.randomUUID() })).toString(
    "base64url"
  );
  return redirect(getTikTokOAuthUrl(state));
};
