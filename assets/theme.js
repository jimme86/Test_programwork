(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initCartDrawer();
    initMobileMenu();
    initQuantitySelectors();
    initGalleryThumbs();
  });

  /* ---------------- Cart drawer ---------------- */
  function initCartDrawer() {
    var drawer = document.getElementById('CartDrawer');
    if (!drawer) return;

    var openTriggers = document.querySelectorAll('[data-cart-drawer-open]');
    var closeTriggers = drawer.querySelectorAll('[data-cart-drawer-close]');

    openTriggers.forEach(function (btn) {
      btn.addEventListener('click', function () {
        drawer.classList.add('is-open');
        drawer.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
      });
    });

    closeTriggers.forEach(function (btn) {
      btn.addEventListener('click', closeDrawer);
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') closeDrawer();
    });

    function closeDrawer() {
      drawer.classList.remove('is-open');
      drawer.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';
    }

    // Intercept product-form submits to add via AJAX and open the drawer.
    document.addEventListener('submit', function (e) {
      var form = e.target.closest('form[action*="/cart/add"]');
      if (!form) return;
      e.preventDefault();

      var formData = new FormData(form);
      fetch('/cart/add.js', { method: 'POST', body: formData, headers: { Accept: 'application/json' } })
        .then(function (res) { return res.json(); })
        .then(function () { return fetch('/cart.js'); })
        .then(function (res) { return res.json(); })
        .then(function (cart) {
          document.querySelectorAll('[data-cart-count]').forEach(function (el) {
            el.textContent = cart.item_count;
          });
          drawer.classList.add('is-open');
          drawer.setAttribute('aria-hidden', 'false');
          document.body.style.overflow = 'hidden';
          // Refresh the drawer markup by reloading the cart sections if available.
          window.location.reload();
        })
        .catch(function () {
          form.submit();
        });
    });
  }

  /* ---------------- Mobile menu ---------------- */
  function initMobileMenu() {
    var toggle = document.querySelector('[data-mobile-menu-toggle]');
    var menu = document.querySelector('[data-mobile-menu]');
    if (!toggle || !menu) return;

    toggle.addEventListener('click', function () {
      var isOpen = menu.classList.toggle('is-open');
      toggle.setAttribute('aria-expanded', isOpen);
    });
  }

  /* ---------------- Quantity selectors ---------------- */
  function initQuantitySelectors() {
    document.querySelectorAll('.quantity-selector').forEach(function (selector) {
      var input = selector.querySelector('input[type="number"]');
      var minus = selector.querySelector('[data-quantity-minus]');
      var plus = selector.querySelector('[data-quantity-plus]');
      if (!input) return;

      if (minus) {
        minus.addEventListener('click', function () {
          var value = Math.max(parseInt(input.min || '0', 10), (parseInt(input.value, 10) || 1) - 1);
          input.value = value;
          input.dispatchEvent(new Event('change', { bubbles: true }));
        });
      }
      if (plus) {
        plus.addEventListener('click', function () {
          var value = (parseInt(input.value, 10) || 0) + 1;
          input.value = value;
          input.dispatchEvent(new Event('change', { bubbles: true }));
        });
      }
    });
  }

  /* ---------------- Product gallery thumbnails ---------------- */
  function initGalleryThumbs() {
    var mainImage = document.getElementById('ProductMainImage');
    var thumbs = document.querySelectorAll('[data-thumb]');
    if (!mainImage || !thumbs.length) return;

    thumbs.forEach(function (thumb) {
      thumb.addEventListener('click', function () {
        thumbs.forEach(function (t) { t.classList.remove('is-active'); });
        thumb.classList.add('is-active');
        var full = thumb.getAttribute('data-full');
        if (full) mainImage.src = full;
      });
    });
  }
})();
