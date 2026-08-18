import type { ActionFunctionArgs } from "@remix-run/node";
import { authenticate } from "~/shopify.server";
import prisma from "~/db.server";

export const action = async ({ request }: ActionFunctionArgs) => {
  const { shop, session } = await authenticate.webhook(request);

  // Clean up everything tied to this shop, including the connected social
  // accounts' stored (encrypted) tokens.
  if (session) {
    await prisma.session.deleteMany({ where: { shop } });
  }
  await prisma.socialConnection.deleteMany({ where: { shop } });
  await prisma.syncSettings.deleteMany({ where: { shop } });

  return new Response(null, { status: 200 });
};
