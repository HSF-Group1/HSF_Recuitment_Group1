/* Dark-mode toggle — follows ui-skill.md §9.
   Uses Bootstrap's data-bs-theme attribute and persists the choice under the
   localStorage key "theme". The anti-flash snippet is inlined at the top of
   <head> in each layout; this file handles toggling + icon state, plus the
   §7 scroll-entry reveal for cards. */

function toggleTheme() {
  var html = document.documentElement;
  var next = html.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-bs-theme', next);
  try { localStorage.setItem('theme', next); } catch (e) { /* ignore */ }
  updateThemeIcons();
}

function updateThemeIcons() {
  var isDark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
  var icons = document.querySelectorAll('[data-theme-icon]');
  for (var i = 0; i < icons.length; i++) {
    icons[i].className = (isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon');
    icons[i].setAttribute('data-theme-icon', '');
  }
}

/* Scroll-entry reveal (ui-skill.md §7): fade + translateY(16px) via a single
   non-blocking IntersectionObserver — GPU-accelerated properties only. */
function initReveal() {
  if (!('IntersectionObserver' in window)) { return; }
  var targets = document.querySelectorAll('.app-card, .card, .app-table-wrapper');
  var observer = new IntersectionObserver(function (entries) {
    for (var i = 0; i < entries.length; i++) {
      if (entries[i].isIntersecting) {
        entries[i].target.classList.add('is-visible');
        observer.unobserve(entries[i].target);
      }
    }
  }, { threshold: 0.1 });
  for (var j = 0; j < targets.length; j++) {
    targets[j].classList.add('reveal-on-scroll');
    targets[j].style.transitionDelay = ((j % 6) * 80) + 'ms';
    observer.observe(targets[j]);
  }
}

document.addEventListener('DOMContentLoaded', function () {
  updateThemeIcons();
  initReveal();
});
