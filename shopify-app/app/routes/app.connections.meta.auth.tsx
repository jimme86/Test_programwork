import type { LoaderFunctionArgs } from "@remix-run/node";
import { redirect } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import { getMetaOAuthUrl } from "~/services/social/meta.server";

// Started as a top-level navigation (Connect button uses `external`) so it
// escapes the embedded admin iframe before redirecting to Meta's OAuth
// consent screen. `state` carries the shop so the callback route can attach
// the resulting connection to the right store without a live session.
export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const state = Buffer.from(JSON.stringify({ shop: session.shop, nonce: crypto.randomUUID() })).toString(
    "base64url"
  );
  return redirect(getMetaOAuthUrl(state));
};
