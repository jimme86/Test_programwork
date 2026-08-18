import type { ActionFunctionArgs, LoaderFunctionArgs } from "@remix-run/node";
import { json } from "@remix-run/node";
import { useLoaderData, useFetcher } from "@remix-run/react";
import { useState } from "react";
import {
  Page,
  Card,
  BlockStack,
  Text,
  Checkbox,
  TextField,
  Button,
  Divider,
} from "@shopify/polaris";
import { authenticate } from "~/shopify.server";
import { getSettings, updateSettings } from "~/models/settings.server";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const settings = await getSettings(session.shop);
  return json({ settings });
};

export const action = async ({ request }: ActionFunctionArgs) => {
  const { session } = await authenticate.admin(request);
  const form = await request.formData();
  const bool = (name: string) => form.get(name) === "true";

  await updateSettings(session.shop, {
    syncProductsMeta: bool("syncProductsMeta"),
    syncProductsTikTok: bool("syncProductsTikTok"),
    syncProductsYouTube: bool("syncProductsYouTube"),
    autoPostInstagram: bool("autoPostInstagram"),
    autoPostFacebook: bool("autoPostFacebook"),
    autoPostTikTok: bool("autoPostTikTok"),
    autoPostYouTube: bool("autoPostYouTube"),
    postTemplate: String(form.get("postTemplate") || ""),
  });
  return json({ ok: true });
};

export default function Settings() {
  const { settings } = useLoaderData<typeof loader>();
  const fetcher = useFetcher();
  const [form, setForm] = useState(settings);

  const set = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  const submit = () => {
    const body = new FormData();
    Object.entries(form).forEach(([k, v]) => body.set(k, String(v)));
    fetcher.submit(body, { method: "post" });
  };

  return (
    <Page title="Sync & auto-post settings" backAction={{ url: "/app" }}>
      <BlockStack gap="400">
        <Card>
          <BlockStack gap="300">
            <Text as="h2" variant="headingMd">
              Catalog sync
            </Text>
            <Text as="p" tone="subdued">
              Keep each connected platform's shop catalog up to date whenever a product is
              created, updated, or deleted in Shopify.
            </Text>
            <Checkbox
              label="Sync to Facebook & Instagram Shop catalog"
              checked={form.syncProductsMeta}
              onChange={(v) => set("syncProductsMeta", v)}
            />
            <Checkbox
              label="Sync to TikTok Shop"
              checked={form.syncProductsTikTok}
              onChange={(v) => set("syncProductsTikTok", v)}
            />
            <Checkbox
              label="Sync to YouTube Shopping"
              checked={form.syncProductsYouTube}
              onChange={(v) => set("syncProductsYouTube", v)}
            />
          </BlockStack>
        </Card>

        <Card>
          <BlockStack gap="300">
            <Text as="h2" variant="headingMd">
              Auto-post new products
            </Text>
            <Text as="p" tone="subdued">
              When a new product is published, automatically post it to the platforms below
              using the caption template.
            </Text>
            <Checkbox
              label="Post to Instagram"
              checked={form.autoPostInstagram}
              onChange={(v) => set("autoPostInstagram", v)}
            />
            <Checkbox
              label="Post to Facebook Page"
              checked={form.autoPostFacebook}
              onChange={(v) => set("autoPostFacebook", v)}
            />
            <Checkbox
              label="Post to TikTok"
              checked={form.autoPostTikTok}
              onChange={(v) => set("autoPostTikTok", v)}
            />
            <Checkbox
              label="Post update to YouTube"
              checked={form.autoPostYouTube}
              onChange={(v) => set("autoPostYouTube", v)}
            />
            <Divider />
            <TextField
              label="Caption template"
              helpText="Available placeholders: {{title}}, {{price}}, {{url}}"
              value={form.postTemplate}
              onChange={(v) => set("postTemplate", v)}
              autoComplete="off"
              multiline={2}
            />
          </BlockStack>
        </Card>

        <Button variant="primary" onClick={submit} loading={fetcher.state !== "idle"}>
          Save settings
        </Button>
      </BlockStack>
    </Page>
  );
}
