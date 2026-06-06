(function () {
    var section = document.getElementById('nearbyToursSection');
    if (!section) return;

    var statusEl = document.getElementById('nearbyToursStatus');
    var gridEl = document.getElementById('nearbyToursGrid');
    var pagerEl = document.getElementById('nearbyToursPagination');
    var citySelect = document.getElementById('nearbyCitySelect');
    var retryBtn = document.getElementById('nearbyRetryBtn');

    var NEARBY_RADIUS_KM = 100;
    var PAGE_LIMIT = 6;
    var TRANSITION_MS = 200;

    var currentParams = {};
    var allTours = [];
    var currentPage = 0;
    var loading = false;
    var isTransitioning = false;
    var meta = {};

    function formatPrice(value) {
        if (value == null) return 'Liên hệ';
        return Number(value).toLocaleString('vi-VN') + ' ₫';
    }

    function setStatus(html, isError) {
        if (!statusEl) return;
        statusEl.innerHTML = html;
        statusEl.className = 'nearby-status text-center mb-4' + (isError ? ' text-danger' : ' text-muted');
    }

    function setGridTransition(active) {
        if (!gridEl) return;
        gridEl.classList.toggle('nearby-grid-transitioning', active);
        gridEl.style.opacity = active ? '0' : '1';
        gridEl.style.transform = active ? 'translateY(10px)' : 'translateY(0)';
    }

    function tourCardHtml(t) {
        var img = t.hinhAnh || '/img/placeholder-tour.jpg';
        var diemDen = t.diemDen ? ' → ' + t.diemDen : '';
        var kmBadge = t.distanceKm != null
            ? '<span class="badge bg-light text-dark border mb-2 align-self-start">' +
                '<i class="bi bi-crosshair me-1"></i>' + t.distanceKm + ' km</span>'
            : '';
        var hotBadge = t.noiBat
            ? '<span class="badge bg-danger position-absolute top-0 start-0 m-2">' +
                '<i class="bi bi-fire me-1"></i>HOT</span>'
            : '';
        return (
            '<div class="col-md-4">' +
            '  <div class="card border-0 shadow-sm h-100 rounded-4 overflow-hidden position-relative">' +
            '    <div class="img-zoom">' +
            hotBadge +
            '      <img src="' + img + '" class="card-img-top" alt="" style="width:100%;height:220px;object-fit:cover;" />' +
            '    </div>' +
            '    <div class="card-body d-flex flex-column text-start">' +
            kmBadge +
            '      <span class="badge bg-primary-subtle text-primary mb-2 align-self-start">' +
            '        <i class="bi bi-geo-alt-fill"></i> ' + (t.diemDon || 'Điểm đón') + diemDen +
            '      </span>' +
            '      <h5 class="card-title fw-bold">' + (t.tieuDe || 'Tour') + '</h5>' +
            '      <p class="fw-bold text-danger fs-5 mb-3">' + formatPrice(t.gia) + '</p>' +
            '      <a href="/tour/' + t.id + '" class="btn btn-primary rounded-pill mt-auto align-self-start px-4">Xem chi tiết</a>' +
            '    </div>' +
            '  </div>' +
            '</div>'
        );
    }

    function renderPage(page) {
        if (!gridEl) return;
        var totalPages = Math.ceil(allTours.length / PAGE_LIMIT);
        if (totalPages === 0) {
            gridEl.innerHTML = '';
            if (pagerEl) {
                pagerEl.innerHTML = '';
                pagerEl.style.display = 'none';
            }
            return;
        }

        currentPage = Math.max(0, Math.min(page, totalPages - 1));
        var from = currentPage * PAGE_LIMIT;
        var slice = allTours.slice(from, from + PAGE_LIMIT);

        gridEl.innerHTML = slice.map(tourCardHtml).join('');
        gridEl.style.minHeight = slice.length > 0 ? '600px' : 'auto';
        renderPagination(totalPages);
    }

    function goPage(target) {
        if (target === currentPage || isTransitioning || loading) return;
        isTransitioning = true;
        setGridTransition(true);
        setTimeout(function () {
            renderPage(target);
            setGridTransition(false);
            isTransitioning = false;
        }, TRANSITION_MS);
    }

    function renderPagination(totalPages) {
        if (!pagerEl) return;
        pagerEl.innerHTML = '';
        if (!totalPages || totalPages <= 1) {
            pagerEl.style.display = 'none';
            return;
        }
        pagerEl.style.display = '';

        var ul = document.createElement('ul');
        ul.className = 'pagination zaki-pagination mb-0';

        function addBtn(label, targetPage, disabled, active, aria) {
            var li = document.createElement('li');
            li.className = 'page-item' +
                (disabled ? ' disabled' : '') +
                (active ? ' active' : '');
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'page-link';
            btn.textContent = label;
            if (aria) btn.setAttribute('aria-label', aria);
            btn.disabled = disabled || loading || isTransitioning;
            if (!disabled) {
                btn.addEventListener('click', function () {
                    goPage(targetPage);
                });
            }
            li.appendChild(btn);
            ul.appendChild(li);
        }

        addBtn('‹', currentPage - 1, currentPage <= 0, false, 'Trước');
        for (var i = 0; i < totalPages; i++) {
            addBtn(String(i + 1), i, false, i === currentPage);
        }
        addBtn('›', currentPage + 1, currentPage >= totalPages - 1, false, 'Tiếp');
        pagerEl.appendChild(ul);
    }

    function applyPayload(data) {
        loading = false;
        meta = data || {};
        allTours = [];
        currentPage = 0;

        if (data.inRange === false) {
            var farMsg = data.message || 'Không có tour xuất phát gần vị trí của bạn.';
            if (data.nearestDepartureCity && data.nearestDistanceKm != null) {
                farMsg += ' Điểm xuất phát gần nhất: <strong>' + data.nearestDepartureCity
                    + '</strong> (~' + data.nearestDistanceKm + ' km).';
            }
            setStatus(farMsg, false);
            if (gridEl) gridEl.innerHTML = '';
            if (pagerEl) {
                pagerEl.innerHTML = '';
                pagerEl.style.display = 'none';
            }
            return;
        }

        allTours = data.tours || [];
        if (!allTours.length) {
            setStatus(data.message || ('Chưa có tour khởi hành từ <strong>'
                + (data.departureCity || 'khu vực của bạn') + '</strong>.'), false);
            if (gridEl) gridEl.innerHTML = '';
            if (pagerEl) {
                pagerEl.innerHTML = '';
                pagerEl.style.display = 'none';
            }
            return;
        }

        var city = data.departureCity || '';
        var dist = data.distanceKm != null ? ' (~' + data.distanceKm + ' km)' : '';
        setStatus('Gợi ý tour xuất phát từ <strong>' + city + '</strong>' + dist + '.', false);
        renderPage(0);
        setGridTransition(false);
    }

    function loadNearby(params) {
        loading = true;
        isTransitioning = false;
        setStatus('<span class="spinner-border spinner-border-sm me-2"></span>Đang tìm chuyến đi gần bạn...', false);
        setGridTransition(true);
        if (gridEl) gridEl.innerHTML = '';
        if (pagerEl) {
            pagerEl.innerHTML = '';
            pagerEl.style.display = 'none';
        }

        var merged = Object.assign({}, params);
        merged.radiusKm = merged.radiusKm || NEARBY_RADIUS_KM;
        merged.limit = 100;
        merged.page = 0;
        currentParams = merged;
        allTours = [];
        currentPage = 0;

        var qs = new URLSearchParams(merged);
        fetch('/api/tour/nearby?' + qs.toString())
            .then(function (res) { return res.json(); })
            .then(applyPayload)
            .catch(function () {
                loading = false;
                setGridTransition(false);
                setStatus('Không tải được danh sách tour. Thử chọn thành phố bên cạnh hoặc bấm thử lại.', true);
            });
    }

    function requestGeolocation() {
        if (!navigator.geolocation) {
            setStatus('Trình duyệt không hỗ trợ định vị. Hãy chọn thành phố bên cạnh.', true);
            return;
        }
        setStatus('<span class="spinner-border spinner-border-sm me-2"></span>Đang lấy vị trí...', false);
        navigator.geolocation.getCurrentPosition(
            function (pos) {
                loadNearby({
                    lat: pos.coords.latitude,
                    lng: pos.coords.longitude
                });
            },
            function () {
                setStatus('Bạn đã từ chối chia sẻ vị trí. Chọn thành phố xuất phát bên cạnh.', true);
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 60000 }
        );
    }

    if (citySelect) {
        citySelect.addEventListener('change', function () {
            if (citySelect.value) {
                loadNearby({ city: citySelect.value });
            }
        });
    }
    if (retryBtn) {
        retryBtn.addEventListener('click', requestGeolocation);
    }

    if (gridEl) {
        gridEl.classList.add('nearby-tours-grid');
        gridEl.style.transition = 'opacity 0.2s ease-in-out, transform 0.2s ease-in-out';
    }

    requestGeolocation();
})();
