import type { LoaderFunctionArgs } from "@remix-run/node";
import { json } from "@remix-run/node";
import { useLoaderData, Link } from "@remix-run/react";
import { Page, Layout, Card, BlockStack, Text, Badge, InlineGrid, Button } from "@shopify/polaris";
import { authenticate } from "~/shopify.server";
import { getConnections } from "~/models/connection.server";
import { getRecentActivity } from "~/models/activityLog.server";

const PLATFORM_LABEL: Record<string, string> = {
  meta: "Facebook & Instagram",
  tiktok: "TikTok",
  youtube: "YouTube",
};

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const [connections, activity] = await Promise.all([
    getConnections(session.shop),
    getRecentActivity(session.shop, 5),
  ]);
  return json({ shop: session.shop, connections, activity });
};

export default function Dashboard() {
  const { shop, connections, activity } = useLoaderData<typeof loader>();
  const connectedPlatforms = new Set(connections.map((c) => c.platform));

  return (
    <Page title="Offgrid Lifestyle · Social Sync" subtitle={shop}>
      <Layout>
        <Layout.Section>
          <Card>
            <BlockStack gap="400">
              <Text as="h2" variant="headingMd">
                Connected channels
              </Text>
              <InlineGrid columns={{ xs: 1, sm: 3 }} gap="400">
                {(["meta", "tiktok", "youtube"] as const).map((platform) => (
                  <Card key={platform} background="bg-surface-secondary">
                    <BlockStack gap="200">
                      <Text as="h3" variant="headingSm">
                        {PLATFORM_LABEL[platform]}
                      </Text>
                      <Badge tone={connectedPlatforms.has(platform) ? "success" : "attention"}>
                        {connectedPlatforms.has(platform) ? "Connected" : "Not connected"}
                      </Badge>
                    </BlockStack>
                  </Card>
                ))}
              </InlineGrid>
              <Button url="/app/connections" variant="primary">
                Manage connections
              </Button>
            </BlockStack>
          </Card>
        </Layout.Section>

        <Layout.Section>
          <Card>
            <BlockStack gap="300">
              <Text as="h2" variant="headingMd">
                Recent activity
              </Text>
              {activity.length === 0 && <Text as="p">Nothing synced yet.</Text>}
              {activity.map((entry) => (
                <Text as="p" key={entry.id}>
                  <strong>{PLATFORM_LABEL[entry.platform] ?? entry.platform}</strong>{" "}
                  {entry.action} — {entry.status}
                  {entry.message ? `: ${entry.message}` : ""}
                </Text>
              ))}
              <Link to="/app/activity">View full activity log →</Link>
            </BlockStack>
          </Card>
        </Layout.Section>
      </Layout>
    </Page>
  );
}
