"""
Social media publisher for Offgrid-lifestyle.com.

Publishes queued posts from content_calendar.json to Facebook, Instagram,
and Pinterest via each platform's official API. Designed to be driven by
a recurring agent run (see README.md's "Social Media Automation" section):
each run calls `process_due_posts()`, which publishes anything scheduled
for today-or-earlier, marks it published in the calendar file, and leaves
everything else untouched. It is safe to run repeatedly (idempotent) and
safe to run before credentials exist (it reports what's missing instead of
raising).

Required environment variables (set at the Environment level so scheduled
runs inherit them — see README):

  FACEBOOK_PAGE_ACCESS_TOKEN   Long-lived Page access token
  FACEBOOK_PAGE_ID             The numeric Page ID
  INSTAGRAM_BUSINESS_ID        The IG Business Account ID linked to the Page
  PINTEREST_ACCESS_TOKEN       Pinterest API v5 access token
  PINTEREST_BOARD_ID           Destination board ID
"""
from __future__ import annotations

import datetime
import json
import os
from pathlib import Path
from typing import Optional

import requests

CALENDAR_PATH = Path(__file__).resolve().parent / "content_calendar.json"

FB_GRAPH_BASE = "https://graph.facebook.com/v21.0"
PINTEREST_BASE = "https://api.pinterest.com/v5"


class MissingCredentials(Exception):
    """Raised when the env vars needed for a platform aren't configured yet."""


def _require_env(*names: str) -> list[str]:
    values = [os.environ.get(n) for n in names]
    missing = [n for n, v in zip(names, values) if not v]
    if missing:
        raise MissingCredentials(
            f"Missing environment variable(s): {', '.join(missing)}. "
            "Add them to this environment's configuration once the account/app exists."
        )
    return values


def publish_facebook_post(message: str, image_url: Optional[str] = None, link: Optional[str] = None) -> dict:
    page_id, token = _require_env("FACEBOOK_PAGE_ID", "FACEBOOK_PAGE_ACCESS_TOKEN")

    if image_url:
        url = f"{FB_GRAPH_BASE}/{page_id}/photos"
        payload = {"url": image_url, "caption": message, "access_token": token}
    else:
        url = f"{FB_GRAPH_BASE}/{page_id}/feed"
        payload = {"message": message, "access_token": token}
        if link:
            payload["link"] = link

    resp = requests.post(url, data=payload, timeout=30)
    resp.raise_for_status()
    return resp.json()


def publish_instagram_post(caption: str, image_url: str) -> dict:
    ig_id, token = _require_env("INSTAGRAM_BUSINESS_ID", "FACEBOOK_PAGE_ACCESS_TOKEN")

    create = requests.post(
        f"{FB_GRAPH_BASE}/{ig_id}/media",
        data={"image_url": image_url, "caption": caption, "access_token": token},
        timeout=30,
    )
    create.raise_for_status()
    creation_id = create.json()["id"]

    publish = requests.post(
        f"{FB_GRAPH_BASE}/{ig_id}/media_publish",
        data={"creation_id": creation_id, "access_token": token},
        timeout=30,
    )
    publish.raise_for_status()
    return publish.json()


def publish_pinterest_pin(caption: str, image_url: str, link: Optional[str] = None) -> dict:
    board_id, token = _require_env("PINTEREST_BOARD_ID", "PINTEREST_ACCESS_TOKEN")

    payload = {
        "board_id": board_id,
        "media_source": {"source_type": "image_url", "url": image_url},
        "description": caption,
    }
    if link:
        payload["link"] = link

    resp = requests.post(
        f"{PINTEREST_BASE}/pins",
        json=payload,
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    resp.raise_for_status()
    return resp.json()


PUBLISHERS = {
    "facebook": lambda post: publish_facebook_post(post["caption"], post.get("image_url"), post.get("link")),
    "instagram": lambda post: publish_instagram_post(post["caption"], post["image_url"]),
    "pinterest": lambda post: publish_pinterest_pin(post["caption"], post["image_url"], post.get("link")),
}


def _load_calendar() -> dict:
    return json.loads(CALENDAR_PATH.read_text())


def _save_calendar(data: dict) -> None:
    CALENDAR_PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")


def process_due_posts(today: Optional[str] = None) -> list[dict]:
    """Publish every 'scheduled' post whose date is today or earlier.

    Returns a list of {id, platform, outcome, detail} summaries — one per
    post considered, whether it published, was skipped (missing creds), or
    failed. Never raises; failures are captured in the result list so a
    scheduled run can report them instead of crashing.
    """
    today = today or datetime.date.today().isoformat()
    calendar = _load_calendar()
    results = []

    for post in calendar["posts"]:
        if post["status"] != "scheduled":
            continue
        if post["date"] > today:
            continue

        publisher = PUBLISHERS.get(post["platform"])
        if publisher is None:
            results.append({"id": post["id"], "platform": post["platform"], "outcome": "error",
                             "detail": f"unknown platform '{post['platform']}'"})
            continue

        try:
            response = publisher(post)
        except MissingCredentials as e:
            results.append({"id": post["id"], "platform": post["platform"], "outcome": "waiting_on_credentials",
                             "detail": str(e)})
            continue
        except requests.HTTPError as e:
            post["status"] = "failed"
            post["last_error"] = str(e)
            results.append({"id": post["id"], "platform": post["platform"], "outcome": "failed",
                             "detail": f"{e} — {e.response.text if e.response is not None else ''}"})
            continue

        post["status"] = "published"
        post["published_at"] = datetime.datetime.utcnow().isoformat() + "Z"
        post["platform_response"] = response
        results.append({"id": post["id"], "platform": post["platform"], "outcome": "published", "detail": response})

    _save_calendar(calendar)
    return results


if __name__ == "__main__":
    for result in process_due_posts():
        print(f"[{result['outcome']}] {result['id']} ({result['platform']}): {result['detail']}")
