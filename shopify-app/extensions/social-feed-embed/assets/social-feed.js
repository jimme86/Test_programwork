// Fetches recent posts from the app proxy and renders them into the grid.
// The proxy (app/routes/api.proxy.$.tsx) holds the actual Graph API token;
// this script never sees it.
(function () {
  function renderItems(container, items) {
    if (!items || items.length === 0) {
      container.innerHTML = '<p class="offgrid-social-feed__empty">No posts yet.</p>';
      return;
    }
    container.innerHTML = items
      .map((item) => {
        const media = item.media_url || item.thumbnail_url || "";
        const link = item.permalink || "#";
        const caption = (item.caption || "").slice(0, 80);
        return `
          <a class="offgrid-social-feed__item" href="${link}" target="_blank" rel="noopener" title="${escapeHtml(caption)}">
            <img src="${media}" alt="${escapeHtml(caption)}" loading="lazy" />
          </a>`;
      })
      .join("");
  }

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
  }

  document.querySelectorAll(".offgrid-social-feed").forEach((root) => {
    const grid = root.querySelector("[data-social-feed-grid]");
    const proxyUrl = root.dataset.proxyUrl;
    const limit = root.dataset.limit;

    fetch(`${proxyUrl}?limit=${encodeURIComponent(limit)}`)
      .then((res) => (res.ok ? res.json() : { items: [] }))
      .then((data) => renderItems(grid, data.items))
      .catch(() => {
        grid.innerHTML = '<p class="offgrid-social-feed__empty">Could not load posts right now.</p>';
      });
  });
})();
