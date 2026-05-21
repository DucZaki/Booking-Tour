(function () {
    var section = document.getElementById('nearbyToursSection');
    if (!section) return;

    var statusEl = document.getElementById('nearbyToursStatus');
    var gridEl = document.getElementById('nearbyToursGrid');
    var pagerEl = document.getElementById('nearbyToursPagination');
    var citySelect = document.getElementById('nearbyCitySelect');
    var retryBtn = document.getElementById('nearbyRetryBtn');
    var NEARBY_RADIUS_KM = 100;

    var currentParams = {};
    var currentPage = 0;

    function formatPrice(value) {
        if (value == null) return 'Liên hệ';
        return Number(value).toLocaleString('vi-VN') + ' ₫';
    }

    function setStatus(html, isError) {
        if (!statusEl) return;
        statusEl.innerHTML = html;
        statusEl.className = 'nearby-status mb-4' + (isError ? ' text-danger' : ' text-muted');
    }

    function renderTours(data) {
        if (!gridEl) return;
        gridEl.innerHTML = '';
        if (pagerEl) {
            pagerEl.innerHTML = '';
            pagerEl.style.display = 'none';
        }
        var tours = data.tours || [];

        if (data.inRange === false) {
            var farMsg = data.message || 'Không có tour xuất phát gần vị trí của bạn.';
            if (data.nearestDepartureCity && data.nearestDistanceKm != null) {
                farMsg += ' Điểm xuất phát gần nhất: <strong>' + data.nearestDepartureCity
                    + '</strong> (~' + data.nearestDistanceKm + ' km).';
            }
            setStatus(farMsg, false);
            return;
        }

        if (!tours.length) {
            setStatus(data.message || ('Chưa có tour khởi hành từ <strong>'
                + (data.departureCity || 'khu vực của bạn') + '</strong>.'), false);
            return;
        }

        var city = data.departureCity || '';
        var dist = data.distanceKm != null ? ' (~' + data.distanceKm + ' km)' : '';
        setStatus('Gợi ý tour xuất phát từ <strong>' + city + '</strong>' + dist + '.', false);

        tours.forEach(function (t) {
            var col = document.createElement('div');
            col.className = 'col-md-4';
            var img = t.hinhAnh || '/img/placeholder-tour.jpg';
            var diemDen = t.diemDen ? ' → ' + t.diemDen : '';
            var kmBadge = t.distanceKm != null
                ? '<span class="badge bg-light text-dark border mb-2 align-self-start"><i class="bi bi-crosshair me-1"></i>'
                    + t.distanceKm + ' km</span>'
                : '';
            col.innerHTML =
                '<div class="card border-0 shadow-sm h-100 rounded-4 overflow-hidden">' +
                '  <div class="img-zoom">' +
                '    <img src="' + img + '" class="card-img-top" alt="" style="width:100%;height:220px;object-fit:cover;" />' +
                '  </div>' +
                '  <div class="card-body d-flex flex-column">' +
                kmBadge +
                '    <span class="badge bg-primary-subtle text-primary mb-2 align-self-start">' +
                '      <i class="bi bi-geo-alt-fill"></i> ' + (t.diemDon || 'Điểm đón') + diemDen +
                '    </span>' +
                '    <h5 class="card-title fw-bold">' + (t.tieuDe || 'Tour') + '</h5>' +
                '    <p class="fw-bold text-danger fs-5 mb-3">' + formatPrice(t.gia) + '</p>' +
                '    <a href="/tour/' + t.id + '" class="btn btn-primary rounded-pill mt-auto align-self-start px-4">Xem chi tiết</a>' +
                '  </div>' +
                '</div>';
            gridEl.appendChild(col);
        });

        renderPagination(data);
    }

    function renderPagination(data) {
        if (!pagerEl) return;
        var totalPages = Number(data.totalPages || 0);
        var page = Number(data.page || 0);
        if (!totalPages || totalPages <= 1) {
            pagerEl.style.display = 'none';
            return;
        }

        pagerEl.style.display = '';

        var ul = document.createElement('ul');
        ul.className = 'pagination zaki-pagination mb-0';

        function addItem(label, targetPage, disabled, active, aria) {
            var li = document.createElement('li');
            li.className = 'page-item' + (disabled ? ' disabled' : '') + (active ? ' active' : '');
            var a = document.createElement('a');
            a.className = 'page-link';
            a.href = '#nearbyToursSection';
            if (aria) a.setAttribute('aria-label', aria);
            a.textContent = label;
            if (!disabled) {
                a.addEventListener('click', function (e) {
                    e.preventDefault();
                    currentPage = targetPage;
                    loadNearby(Object.assign({}, currentParams, { page: currentPage }));
                });
            }
            li.appendChild(a);
            ul.appendChild(li);
        }

        addItem('‹', page - 1, page <= 0, false, 'Previous');

        // windowed pagination
        var windowSize = 5;
        var start = Math.max(0, page - Math.floor(windowSize / 2));
        var end = Math.min(totalPages - 1, start + windowSize - 1);
        start = Math.max(0, end - windowSize + 1);

        if (start > 0) {
            addItem('1', 0, false, page === 0);
            if (start > 1) {
                var ell = document.createElement('li');
                ell.className = 'page-item disabled';
                ell.innerHTML = '<span class="page-link">…</span>';
                ul.appendChild(ell);
            }
        }

        for (var i = start; i <= end; i++) {
            addItem(String(i + 1), i, false, i === page);
        }

        if (end < totalPages - 1) {
            if (end < totalPages - 2) {
                var ell2 = document.createElement('li');
                ell2.className = 'page-item disabled';
                ell2.innerHTML = '<span class="page-link">…</span>';
                ul.appendChild(ell2);
            }
            addItem(String(totalPages), totalPages - 1, false, page === totalPages - 1);
        }

        addItem('›', page + 1, page >= totalPages - 1, false, 'Next');

        pagerEl.appendChild(ul);
    }

    function loadNearby(params) {
        setStatus('<span class="spinner-border spinner-border-sm me-2"></span>Đang tìm chuyến đi gần bạn...', false);
        if (gridEl) gridEl.innerHTML = '';
        if (pagerEl) {
            pagerEl.innerHTML = '';
            pagerEl.style.display = 'none';
        }

        params.radiusKm = params.radiusKm || NEARBY_RADIUS_KM;
        params.limit = params.limit || 6;
        params.page = params.page || 0;

        currentParams = Object.assign({}, params);
        currentPage = Number(params.page || 0);

        var qs = new URLSearchParams(params);
        fetch('/api/tour/nearby?' + qs.toString())
            .then(function (res) { return res.json(); })
            .then(renderTours)
            .catch(function () {
                setStatus('Không tải được danh sách tour. Thử chọn thành phố bên cạnh hoặc bấm thử lại.', true);
            });
    }

    function requestGeolocation() {
        if (!navigator.geolocation) {
            setStatus('Trình duyệt không hỗ trợ định vị. Hãy chọn thành phố bên cạnh.', true);
            return;
        }
        navigator.geolocation.getCurrentPosition(
            function (pos) {
                loadNearby({
                    lat: pos.coords.latitude,
                    lng: pos.coords.longitude,
                    page: 0
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
                loadNearby({ city: citySelect.value, page: 0 });
            }
        });
    }
    if (retryBtn) {
        retryBtn.addEventListener('click', requestGeolocation);
    }

    requestGeolocation();
})();
