import type { ActionFunctionArgs, LoaderFunctionArgs } from "@remix-run/node";
import { json } from "@remix-run/node";
import { useLoaderData, useFetcher } from "@remix-run/react";
import { Page, Card, BlockStack, InlineStack, Text, Button, Badge } from "@shopify/polaris";
import { authenticate } from "~/shopify.server";
import { getConnections, removeConnection } from "~/models/connection.server";
import { logActivity } from "~/models/activityLog.server";

const PLATFORMS = [
  { key: "meta", label: "Facebook & Instagram", connectPath: "/app/connections/meta/auth" },
  { key: "tiktok", label: "TikTok", connectPath: "/app/connections/tiktok/auth" },
  { key: "youtube", label: "YouTube", connectPath: "/app/connections/youtube/auth" },
] as const;

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const connections = await getConnections(session.shop);
  return json({ connections });
};

export const action = async ({ request }: ActionFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const formData = await request.formData();
  const platform = formData.get("platform") as "meta" | "tiktok" | "youtube";
  await removeConnection(session.shop, platform);
  await logActivity({ shop: session.shop, platform, action: "disconnect", status: "success" });
  return json({ ok: true });
};

export default function Connections() {
  const { connections } = useLoaderData<typeof loader>();
  const fetcher = useFetcher();
  const byPlatform = Object.fromEntries(connections.map((c) => [c.platform, c]));

  return (
    <Page title="Channel connections" backAction={{ url: "/app" }}>
      <BlockStack gap="400">
        {PLATFORMS.map(({ key, label, connectPath }) => {
          const connection = byPlatform[key];
          return (
            <Card key={key}>
              <InlineStack align="space-between" blockAlign="center">
                <BlockStack gap="100">
                  <Text as="h3" variant="headingSm">
                    {label}
                  </Text>
                  {connection ? (
                    <InlineStack gap="200" blockAlign="center">
                      <Badge tone="success">Connected</Badge>
                      <Text as="span" tone="subdued">
                        {connection.accountName || connection.accountId}
                      </Text>
                    </InlineStack>
                  ) : (
                    <Badge>Not connected</Badge>
                  )}
                </BlockStack>
                {connection ? (
                  <fetcher.Form method="post">
                    <input type="hidden" name="platform" value={key} />
                    <Button tone="critical" submit variant="tertiary">
                      Disconnect
                    </Button>
                  </fetcher.Form>
                ) : (
                  <Button url={connectPath} variant="primary" external>
                    Connect
                  </Button>
                )}
              </InlineStack>
            </Card>
          );
        })}
        <Text as="p" tone="subdued">
          Connecting a channel opens that platform's OAuth consent screen. You must already
          have a Facebook Page + Instagram Business account, a TikTok for Business account, and
          a YouTube channel set up — see docs/SOCIAL_CHANNEL_SETUP.md for the one-time setup
          checklist.
        </Text>
      </BlockStack>
    </Page>
  );
}
