/* global flatpickr */

(function () {
  function toDateValue(el) {
    // Prefer current value; fall back to data-value (if server sets it).
    return (el.value || el.getAttribute('data-value') || '').trim();
  }

  function initOne(el) {
    if (!window.flatpickr) return;
    if (el._flatpickr) return;

    var min = (el.getAttribute('data-min') || '').trim();
    var max = (el.getAttribute('data-max') || '').trim();
    var defaultDate = toDateValue(el);

    flatpickr(el, {
      locale: (flatpickr.l10ns && (flatpickr.l10ns.vn || flatpickr.l10ns.vietnamese)) || 'default',
      allowInput: true,
      dateFormat: 'Y-m-d',
      altInput: true,
      altFormat: 'd/m/Y',
      defaultDate: defaultDate || null,
      minDate: min || null,
      maxDate: max || null,
      disableMobile: true
    });
  }

  function initAll() {
    document.querySelectorAll('input.zaki-date').forEach(initOne);
  }

  // Support dynamically-added fields (e.g. admin add-row cloning).
  document.addEventListener('focusin', function (e) {
    var t = e.target;
    if (t && t.matches && t.matches('input.zaki-date')) initOne(t);
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAll);
  } else {
    initAll();
  }
})();
