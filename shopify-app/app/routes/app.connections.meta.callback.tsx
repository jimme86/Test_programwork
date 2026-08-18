import type { LoaderFunctionArgs } from "@remix-run/node";
import { redirect } from "@remix-run/node";
import { exchangeMetaCode } from "~/services/social/meta.server";
import { upsertConnection } from "~/models/connection.server";
import { logActivity } from "~/models/activityLog.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);
  const code = url.searchParams.get("code");
  const state = url.searchParams.get("state");
  if (!code || !state) throw new Response("Missing code/state from Meta.", { status: 400 });

  const { shop } = JSON.parse(Buffer.from(state, "base64url").toString("utf8")) as { shop: string };

  try {
    const { access_token, expires_in } = await exchangeMetaCode(code);

    // Discover the Page (and its linked Instagram Business account) this
    // token can manage, so auto-posting knows where to publish.
    const pagesRes = await fetch(
      `https://graph.facebook.com/v21.0/me/accounts?fields=id,name,instagram_business_account&access_token=${access_token}`
    );
    const pages = (await pagesRes.json()) as {
      data?: { id: string; name: string; instagram_business_account?: { id: string } }[];
    };
    const page = pages.data?.[0];

    await upsertConnection({
      shop,
      platform: "meta",
      accountId: page?.id,
      accountName: page?.name,
      accessToken: access_token,
      expiresAt: new Date(Date.now() + expires_in * 1000),
      metadata: {
        pageId: page?.id,
        igUserId: page?.instagram_business_account?.id,
        catalogId: process.env.META_CATALOG_ID,
      },
    });
    await logActivity({ shop, platform: "meta", action: "connect", status: "success" });
  } catch (err) {
    await logActivity({ shop, platform: "meta", action: "connect", status: "error", message: (err as Error).message });
  }

  // TODO: replace with the deep link to this app's embedded Connections page
  // once you know your app handle, e.g. https://{shop}/admin/apps/{handle}/app/connections
  return redirect(`https://${shop}/admin/apps`);
};
