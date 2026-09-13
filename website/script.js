/* 100 steps — download site interactions */

const MAX_STEPS = 100;
const displayEl = document.getElementById('display');
const fillEl = document.getElementById('fill');
const checksumBtn = document.getElementById('checksumBtn');
const copyBtn = document.getElementById('copyBtn');
const shaEl = document.getElementById('sha');
const verifyHint = document.getElementById('verifyHint');
const btnLabel = document.getElementById('btnLabel');

/* ---------- download metadata: one source of truth ---------- */
const APK_NAME = 'step-counter-v0.1.apk';
const APK_VERSION = '0.1';

/* ---------- animated counter + milestone ticks ---------- */
const MILESTONES = [10, 20, 50];
let counterTarget = 0;
let counterFrame = 0;

function animateCountTo(target) {
  const start = Number(displayEl.textContent) || 0;
  if (target === start) return;
  counterTarget = target;
  const duration = start === 0 ? 1400 : 500; // tease the bar on load, be snappy after
  const t0 = performance.now();
  const frame = ++counterFrame;

  function step(now) {
    if (frame !== counterFrame) return; // a newer animation owns the counter
    const p = Math.min((now - t0) / duration, 1);
    const eased = 1 - Math.pow(1 - p, 3); // ease-out cubic
    const value = Math.round(start + (target - start) * eased);
    setCounter(value);
    if (p < 1) requestAnimationFrame(step);
  }

  requestAnimationFrame(step);
}

function setCounter(value) {
  displayEl.textContent = value;
  fillEl.style.width = (value / MAX_STEPS) * 100 + '%';

  document.querySelectorAll('.bar .tick').forEach((node) => {
    const milestone = Number(node.querySelector('em').textContent);
    node.classList.toggle('done', value >= milestone);
  });
}

/* fill the bar to LOAD_TARGET on view, then nudge by +1 every 4s */
const LOAD_TARGET = 54;
const NUDGE_INTERVAL = 4000;

function heroPerformance() {
  setCounter(0);
  animateCountTo(LOAD_TARGET);
  setInterval(() => {
    const current = counterTarget;
    const next = current >= MAX_STEPS ? 0 : current + 1;
    animateCountTo(next);
  }, NUDGE_INTERVAL);
}

/* ---------- download size: fetched live ---------- */
async function enrichDownloadMeta() {
  const sizeFormat = new Intl.NumberFormat('en-US', {
    style: 'unit',
    unit: 'megabyte',
    maximumFractionDigits: 1,
  });

  try {
    const res = await fetch(APK_NAME, { method: 'HEAD' });
    const size = Number(res.headers.get('content-length'));
    if (Number.isFinite(size) && size > 0) {
      const pretty = sizeFormat.format(size / (1024 * 1024));
      document.querySelectorAll('[data-size]').forEach((el) => (el.textContent = pretty));
    }
  } catch (_) {
    /* static hosting without HEAD support — static labels stay */
  }

  btnLabel.textContent = APK_VERSION;
  document.querySelectorAll('[data-version]').forEach((el) => (el.textContent = APK_VERSION));
}

/* ---------- checksum reveal + copy ---------- */
checksumBtn.addEventListener('click', () => {
  const expanded = checksumBtn.getAttribute('aria-expanded') === 'true';
  checksumBtn.setAttribute('aria-expanded', String(!expanded));
  shaEl.hidden = !expanded;
  checksumBtn.textContent = expanded ? 'Verify download' : 'Hide checksum';
  if (!expanded) shaEl.focus();
});

copyBtn.addEventListener('click', async () => {
  try {
    await navigator.clipboard.writeText(shaEl.textContent.trim());
  } catch (_) {
    /* clipboard blocked — select instead */
    shaEl.select();
    document.execCommand('copy');
  }
  verifyHint.hidden = false;
  setTimeout(() => (verifyHint.hidden = true), 4000);
});

/* ---------- boot ---------- */
document.addEventListener('DOMContentLoaded', () => {
  enrichDownloadMeta();
  heroPerformance();
});