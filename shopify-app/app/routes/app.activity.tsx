import type { LoaderFunctionArgs } from "@remix-run/node";
import { json } from "@remix-run/node";
import { useLoaderData } from "@remix-run/react";
import { Page, Card, IndexTable, Badge, Text } from "@shopify/polaris";
import { authenticate } from "~/shopify.server";
import { getRecentActivity } from "~/models/activityLog.server";

const PLATFORM_LABEL: Record<string, string> = {
  meta: "Facebook & Instagram",
  tiktok: "TikTok",
  youtube: "YouTube",
  system: "System",
};

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const activity = await getRecentActivity(session.shop, 100);
  return json({ activity });
};

export default function Activity() {
  const { activity } = useLoaderData<typeof loader>();

  return (
    <Page title="Activity log" backAction={{ url: "/app" }}>
      <Card padding="0">
        <IndexTable
          resourceName={{ singular: "event", plural: "events" }}
          itemCount={activity.length}
          headings={[
            { title: "When" },
            { title: "Platform" },
            { title: "Action" },
            { title: "Product" },
            { title: "Status" },
            { title: "Message" },
          ]}
          selectable={false}
        >
          {activity.map((entry, index) => (
            <IndexTable.Row id={entry.id} key={entry.id} position={index}>
              <IndexTable.Cell>
                <Text as="span" tone="subdued">
                  {new Date(entry.createdAt).toLocaleString()}
                </Text>
              </IndexTable.Cell>
              <IndexTable.Cell>{PLATFORM_LABEL[entry.platform] ?? entry.platform}</IndexTable.Cell>
              <IndexTable.Cell>{entry.action}</IndexTable.Cell>
              <IndexTable.Cell>{entry.productId ?? "—"}</IndexTable.Cell>
              <IndexTable.Cell>
                <Badge tone={entry.status === "success" ? "success" : "critical"}>
                  {entry.status}
                </Badge>
              </IndexTable.Cell>
              <IndexTable.Cell>{entry.message ?? ""}</IndexTable.Cell>
            </IndexTable.Row>
          ))}
        </IndexTable>
      </Card>
    </Page>
  );
}
