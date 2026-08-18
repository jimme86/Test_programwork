import prisma from "~/db.server";
import type { Platform } from "./connection.server";

export async function logActivity(entry: {
  shop: string;
  platform: Platform | "system";
  action: "catalog_sync" | "auto_post" | "connect" | "disconnect" | "error";
  productId?: string;
  status: "success" | "error";
  message?: string;
}) {
  return prisma.activityLog.create({ data: entry });
}

export async function getRecentActivity(shop: string, take = 50) {
  return prisma.activityLog.findMany({
    where: { shop },
    orderBy: { createdAt: "desc" },
    take,
  });
}
