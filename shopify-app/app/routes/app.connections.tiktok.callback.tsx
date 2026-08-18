import type { LoaderFunctionArgs } from "@remix-run/node";
import { redirect } from "@remix-run/node";
import { exchangeTikTokCode } from "~/services/social/tiktok.server";
import { upsertConnection } from "~/models/connection.server";
import { logActivity } from "~/models/activityLog.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);
  const code = url.searchParams.get("code");
  const state = url.searchParams.get("state");
  if (!code || !state) throw new Response("Missing code/state from TikTok.", { status: 400 });

  const { shop } = JSON.parse(Buffer.from(state, "base64url").toString("utf8")) as { shop: string };

  try {
    const tokens = await exchangeTikTokCode(code);
    await upsertConnection({
      shop,
      platform: "tiktok",
      accountId: tokens.open_id,
      accessToken: tokens.access_token,
      refreshToken: tokens.refresh_token,
      expiresAt: new Date(Date.now() + tokens.expires_in * 1000),
      metadata: { shopId: process.env.TIKTOK_SHOP_ID },
    });
    await logActivity({ shop, platform: "tiktok", action: "connect", status: "success" });
  } catch (err) {
    await logActivity({ shop, platform: "tiktok", action: "connect", status: "error", message: (err as Error).message });
  }

  return redirect(`https://${shop}/admin/apps`);
};
