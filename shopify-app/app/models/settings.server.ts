import prisma from "~/db.server";

const DEFAULTS = {
  syncProductsMeta: true,
  syncProductsTikTok: true,
  syncProductsYouTube: true,
  autoPostInstagram: false,
  autoPostFacebook: false,
  autoPostTikTok: false,
  autoPostYouTube: false,
  postTemplate: "New in the shop: {{title}} 🌲⚡ {{url}}",
};

export async function getSettings(shop: string) {
  const existing = await prisma.syncSettings.findUnique({ where: { shop } });
  if (existing) return existing;
  return prisma.syncSettings.create({ data: { shop, ...DEFAULTS } });
}

export async function updateSettings(shop: string, patch: Partial<typeof DEFAULTS>) {
  return prisma.syncSettings.upsert({
    where: { shop },
    create: { shop, ...DEFAULTS, ...patch },
    update: patch,
  });
}

/** Fills {{title}}, {{price}}, {{url}} placeholders in the auto-post caption template. */
export function renderPostTemplate(
  template: string,
  product: { title: string; price?: string; url: string }
) {
  return template
    .replaceAll("{{title}}", product.title)
    .replaceAll("{{price}}", product.price ?? "")
    .replaceAll("{{url}}", product.url);
}
