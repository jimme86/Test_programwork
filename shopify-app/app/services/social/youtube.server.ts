import { google } from "googleapis";
import type { SyncableProduct, PostResult } from "./types";

// YouTube integration via Google OAuth2 + YouTube Data API v3 (community
// posts / channel activity) and the YouTube "Shopping" product feed, which
// in practice is managed by linking a Merchant Center account rather than a
// direct write API — see docs/SOCIAL_CHANNEL_SETUP.md for the manual link
// step. This module handles the parts that ARE API-driven: OAuth, and
// auto-posting a "New product" community update.

function getOAuthClient() {
  return new google.auth.OAuth2(
    process.env.YOUTUBE_CLIENT_ID,
    process.env.YOUTUBE_CLIENT_SECRET,
    process.env.YOUTUBE_REDIRECT_URI
  );
}

export function getYouTubeOAuthUrl(state: string) {
  const client = getOAuthClient();
  return client.generateAuthUrl({
    access_type: "offline",
    prompt: "consent",
    state,
    scope: [
      "https://www.googleapis.com/auth/youtube",
      "https://www.googleapis.com/auth/youtube.force-ssl",
      "https://www.googleapis.com/auth/yt-analytics.readonly",
    ],
  });
}

export async function exchangeYouTubeCode(code: string) {
  const client = getOAuthClient();
  const { tokens } = await client.getToken(code);
  return tokens; // { access_token, refresh_token, expiry_date, ... }
}

export async function getChannelForToken(accessToken: string, refreshToken?: string | null) {
  const client = getOAuthClient();
  client.setCredentials({ access_token: accessToken, refresh_token: refreshToken ?? undefined });
  const youtube = google.youtube({ version: "v3", auth: client });
  const res = await youtube.channels.list({ part: ["snippet"], mine: true });
  const channel = res.data.items?.[0];
  return channel ? { id: channel.id, title: channel.snippet?.title } : null;
}

/**
 * YouTube's Data API does not expose a general "create a Community Post"
 * endpoint to third-party apps (that surface is allowlisted per-channel).
 * So the universally-supported auto-post action is: append a "new product"
 * blurb + link to the description of the channel's most recent public
 * upload, which shows up to subscribers via the Subscriptions feed/notif.
 * If/when the channel is later granted Community Posts API access, swap
 * the body of this function for that call — call sites don't need to change.
 */
export async function postProductUpdate(
  accessToken: string,
  refreshToken: string | null,
  product: SyncableProduct,
  caption: string
): Promise<PostResult> {
  try {
    const client = getOAuthClient();
    client.setCredentials({ access_token: accessToken, refresh_token: refreshToken ?? undefined });
    const youtube = google.youtube({ version: "v3", auth: client });

    const uploads = await youtube.channels.list({ part: ["contentDetails"], mine: true });
    const uploadsPlaylistId =
      uploads.data.items?.[0]?.contentDetails?.relatedPlaylists?.uploads;
    if (!uploadsPlaylistId) throw new Error("No uploads playlist found for this channel.");

    const latest = await youtube.playlistItems.list({
      part: ["contentDetails"],
      playlistId: uploadsPlaylistId,
      maxResults: 1,
    });
    const videoId = latest.data.items?.[0]?.contentDetails?.videoId;
    if (!videoId) throw new Error("Channel has no uploaded videos to attach the update to.");

    const current = await youtube.videos.list({ part: ["snippet"], id: [videoId] });
    const snippet = current.data.items?.[0]?.snippet;
    if (!snippet) throw new Error("Could not read current video snippet.");

    await youtube.videos.update({
      part: ["snippet"],
      requestBody: {
        id: videoId,
        snippet: {
          ...snippet,
          description: `${caption}\n\n${snippet.description ?? ""}`,
        },
      },
    });

    return { platform: "youtube", ok: true, externalId: videoId };
  } catch (err) {
    return { platform: "youtube", ok: false, error: (err as Error).message };
  }
}
