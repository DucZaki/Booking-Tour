/**
 * Toast thông báo hết / thiếu chỗ khi đặt tour (giống UI Vietravel).
 */
(function (global) {
    'use strict';

    var STACK_ID = 'zakiCapacityToastStack';
    var AUTO_HIDE_MS = 6000;

    function formatMessage(remaining) {
        var n = parseInt(remaining, 10);
        if (isNaN(n) || n < 0) {
            n = 0;
        }
        if (n === 0) {
            return 'Rất tiếc Tour hiện tại đã hết chỗ.';
        }
        return 'Rất tiếc Tour hiện tại số chỗ còn nhận chỉ còn: ' + n + ' chỗ';
    }

    function ensureStack() {
        var stack = document.getElementById(STACK_ID);
        if (!stack) {
            stack = document.createElement('div');
            stack.id = STACK_ID;
            stack.className = 'zaki-capacity-toast-stack';
            stack.setAttribute('aria-live', 'polite');
            stack.setAttribute('role', 'status');
            document.body.appendChild(stack);
        }
        return stack;
    }

    function show(remaining, options) {
        options = options || {};
        var stack = ensureStack();
        var toast = document.createElement('div');
        toast.className = 'zaki-capacity-toast';
        toast.innerHTML =
            '<span class="zaki-capacity-toast__icon" aria-hidden="true">' +
            '<i class="bi bi-x-lg"></i></span>' +
            '<p class="zaki-capacity-toast__text"></p>';

        var text = options.message || formatMessage(remaining);
        toast.querySelector('.zaki-capacity-toast__text').textContent = text;
        stack.appendChild(toast);

        var hideMs = options.duration != null ? options.duration : AUTO_HIDE_MS;
        setTimeout(function () {
            toast.classList.add('is-leaving');
            setTimeout(function () {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 220);
        }, hideMs);

        return toast;
    }

    global.ZakiCapacityToast = {
        formatMessage: formatMessage,
        show: show
    };
})(typeof window !== 'undefined' ? window : this);
