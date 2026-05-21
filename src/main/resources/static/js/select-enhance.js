(function () {
    'use strict';

    if (typeof Choices === 'undefined') {
        return;
    }

    var registry = new WeakMap();

    function firstEmptyOption(select) {
        for (var i = 0; i < select.options.length; i++) {
            var opt = select.options[i];
            if (!opt.value) {
                return opt;
            }
        }
        return null;
    }

    function buildConfig(select) {
        var emptyOpt = firstEmptyOption(select);
        var optionCount = select.options.length;
        return {
            searchEnabled: optionCount > 8,
            searchPlaceholderValue: 'Tìm kiếm...',
            itemSelectText: '',
            shouldSort: false,
            allowHTML: false,
            placeholder: !!emptyOpt,
            placeholderValue: emptyOpt ? emptyOpt.text : '',
            searchResultLimit: 20,
            position: 'auto'
        };
    }

    function shouldSkip(select) {
        return !select
            || select.tagName !== 'SELECT'
            || select.multiple
            || select.classList.contains('select-native')
            || select.disabled;
    }

    function enhance(select) {
        if (shouldSkip(select)) {
            return null;
        }
        if (registry.has(select)) {
            return registry.get(select);
        }

        select.classList.add('zaki-select-enhanced');
        var isSm = select.classList.contains('form-select-sm');

        var instance = new Choices(select, buildConfig(select));
        registry.set(select, instance);

        if (isSm) {
            var outer = select.closest('.choices');
            if (outer) {
                outer.classList.add('is-sm');
            }
        }

        return instance;
    }

    function refresh(select) {
        if (!select) {
            return null;
        }
        var instance = registry.get(select);
        if (instance) {
            instance.destroy();
            registry.delete(select);
            select.classList.remove('zaki-select-enhanced');
        }
        return enhance(select);
    }

    function initAll(root) {
        var scope = root || document;
        scope.querySelectorAll('select.form-select').forEach(function (el) {
            enhance(el);
        });
    }

    window.ZakiSelect = {
        enhance: enhance,
        refresh: refresh,
        initAll: initAll
    };

    function boot() {
        initAll();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
})();
