/* global flatpickr */

(function () {
  function normalizeToIso(raw) {
    if (!raw) return '';
    raw = raw.trim();
    var iso = raw.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (iso) return iso[1] + '-' + iso[2] + '-' + iso[3];
    var vn = raw.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/);
    if (vn) {
      var d = ('0' + vn[1]).slice(-2);
      var m = ('0' + vn[2]).slice(-2);
      return vn[3] + '-' + m + '-' + d;
    }
    return raw;
  }

  function toDateValue(el) {
    var raw = (el.getAttribute('data-value') || el.value || '').trim();
    return normalizeToIso(raw);
  }

  function shouldSkip(el) {
    // altInput của flatpickr cũng có class flatpickr-input + zaki-date nhưng không có _flatpickr.
    // Khởi tạo lại sẽ parse d/m/Y bằng format Y-m-d → lỗi hiển thị 01/01/2026.
    return el.classList.contains('flatpickr-input') && !el._flatpickr;
  }

  function initOne(el) {
    if (!window.flatpickr) return;
    if (el._flatpickr || shouldSkip(el)) return;

    var min = (el.getAttribute('data-min') || '').trim();
    var max = (el.getAttribute('data-max') || '').trim();
    var iso = toDateValue(el);
    if (iso) el.value = iso;

    flatpickr(el, {
      locale: (flatpickr.l10ns && (flatpickr.l10ns.vn || flatpickr.l10ns.vietnamese)) || 'default',
      allowInput: true,
      dateFormat: 'Y-m-d',
      altInput: true,
      altFormat: 'd/m/Y',
      defaultDate: iso || null,
      minDate: min || null,
      maxDate: max || null,
      disableMobile: true,
      onReady: function (_selectedDates, _dateStr, instance) {
        if (instance.altInput) {
          instance.altInput.classList.remove('zaki-date');
        }
      }
    });
  }

  function initAll() {
    document.querySelectorAll('input.zaki-date').forEach(initOne);
  }

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
