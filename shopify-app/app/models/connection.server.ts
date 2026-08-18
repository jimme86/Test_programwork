import prisma from "~/db.server";
import { encryptToken, decryptToken } from "~/services/encryption.server";

export type Platform = "meta" | "tiktok" | "youtube";

export interface ConnectionInput {
  shop: string;
  platform: Platform;
  accountId?: string;
  accountName?: string;
  accessToken: string;
  refreshToken?: string;
  expiresAt?: Date;
  metadata?: Record<string, unknown>;
}

export async function upsertConnection(input: ConnectionInput) {
  return prisma.socialConnection.upsert({
    where: { shop_platform: { shop: input.shop, platform: input.platform } },
    create: {
      shop: input.shop,
      platform: input.platform,
      accountId: input.accountId,
      accountName: input.accountName,
      accessToken: encryptToken(input.accessToken),
      refreshToken: input.refreshToken ? encryptToken(input.refreshToken) : undefined,
      expiresAt: input.expiresAt,
      metadata: input.metadata ? JSON.stringify(input.metadata) : undefined,
    },
    update: {
      accountId: input.accountId,
      accountName: input.accountName,
      accessToken: encryptToken(input.accessToken),
      refreshToken: input.refreshToken ? encryptToken(input.refreshToken) : undefined,
      expiresAt: input.expiresAt,
      metadata: input.metadata ? JSON.stringify(input.metadata) : undefined,
    },
  });
}

export async function getConnections(shop: string) {
  const rows = await prisma.socialConnection.findMany({ where: { shop } });
  return rows.map((row) => ({
    ...row,
    metadata: row.metadata ? JSON.parse(row.metadata) : {},
  }));
}

/** Returns the connection with its access/refresh tokens decrypted, ready to call the platform API. */
export async function getConnectionWithSecrets(shop: string, platform: Platform) {
  const row = await prisma.socialConnection.findUnique({
    where: { shop_platform: { shop, platform } },
  });
  if (!row) return null;
  return {
    ...row,
    accessToken: decryptToken(row.accessToken),
    refreshToken: row.refreshToken ? decryptToken(row.refreshToken) : null,
    metadata: row.metadata ? JSON.parse(row.metadata) : {},
  };
}

export async function removeConnection(shop: string, platform: Platform) {
  return prisma.socialConnection.deleteMany({ where: { shop, platform } });
}
