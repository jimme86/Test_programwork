import type { LoaderFunctionArgs } from "@remix-run/node";
import { redirect } from "@remix-run/node";
import { exchangeYouTubeCode, getChannelForToken } from "~/services/social/youtube.server";
import { upsertConnection } from "~/models/connection.server";
import { logActivity } from "~/models/activityLog.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);
  const code = url.searchParams.get("code");
  const state = url.searchParams.get("state");
  if (!code || !state) throw new Response("Missing code/state from Google.", { status: 400 });

  const { shop } = JSON.parse(Buffer.from(state, "base64url").toString("utf8")) as { shop: string };

  try {
    const tokens = await exchangeYouTubeCode(code);
    if (!tokens.access_token) throw new Error("Google did not return an access token.");

    const channel = await getChannelForToken(tokens.access_token, tokens.refresh_token);

    await upsertConnection({
      shop,
      platform: "youtube",
      accountId: channel?.id ?? undefined,
      accountName: channel?.title ?? undefined,
      accessToken: tokens.access_token,
      refreshToken: tokens.refresh_token ?? undefined,
      expiresAt: tokens.expiry_date ? new Date(tokens.expiry_date) : undefined,
    });
    await logActivity({ shop, platform: "youtube", action: "connect", status: "success" });
  } catch (err) {
    await logActivity({ shop, platform: "youtube", action: "connect", status: "error", message: (err as Error).message });
  }

  return redirect(`https://${shop}/admin/apps`);
};
