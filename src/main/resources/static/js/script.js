/**
 * ZakiBooking - Main Script
 */
document.addEventListener('DOMContentLoaded', function () {

    // =============================================
    // Splide Carousel - nếu có element #main-slider
    // =============================================
    if (document.getElementById('main-slider')) {
        new Splide('#main-slider', {
            type: 'loop',
            perPage: 1,
            autoplay: true,
            interval: 4000,
            pauseOnHover: true,
        }).mount();
    }

    // =============================================
    // Auto-dismiss alerts sau 5 giây
    // =============================================
    document.querySelectorAll('.alert.alert-dismissible').forEach(function (alert) {
        setTimeout(function () {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // =============================================
    // Premium Nav Dock - Scroll Effect
    // =============================================
    const navDock = document.querySelector('.premium-nav-dock');
    if (navDock) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navDock.classList.add('scrolled');
            } else {
                navDock.classList.remove('scrolled');
            }
        });
    }

    // =============================================
    // Nav search — trang chủ: scroll tới form; trang khác: dropdown navbar
    // =============================================
    const navSearchToggle = document.getElementById('navSearchToggle');
    const navSearchPanel = document.getElementById('navSearchPanel');
    const navSearchScrim = document.getElementById('navSearchScrim');
    const navSearchClose = document.getElementById('navSearchClose');
    const homeSearchSection = document.getElementById('homeSearch');

    function setNavSearchOpen(open) {
        if (!navSearchPanel || !navSearchToggle) return;
        navSearchPanel.classList.toggle('open', open);
        if (navSearchScrim) navSearchScrim.classList.toggle('open', open);
        navSearchToggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        navSearchPanel.setAttribute('aria-hidden', open ? 'false' : 'true');
        if (open) {
            var firstInput = navSearchPanel.querySelector('input[name="diemDen"]');
            if (firstInput) setTimeout(function () { firstInput.focus(); }, 120);
        }
    }

    function openHomeSearch() {
        if (!homeSearchSection) return false;
        homeSearchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
        var input = homeSearchSection.querySelector('input[name="diemDen"]');
        if (input) setTimeout(function () { input.focus(); }, 400);
        return true;
    }

    if (navSearchToggle) {
        navSearchToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            if (homeSearchSection) {
                openHomeSearch();
                setNavSearchOpen(false);
                return;
            }
            setNavSearchOpen(!navSearchPanel.classList.contains('open'));
        });
        if (navSearchScrim) {
            navSearchScrim.addEventListener('click', function () { setNavSearchOpen(false); });
        }
        if (navSearchClose) {
            navSearchClose.addEventListener('click', function () { setNavSearchOpen(false); });
        }
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') setNavSearchOpen(false);
        });
        document.addEventListener('click', function (e) {
            if (!navSearchPanel.classList.contains('open')) return;
            if (navSearchPanel.contains(e.target) || navSearchToggle.contains(e.target)) return;
            setNavSearchOpen(false);
        });
    }

    // =============================================
    // AI Chatbot - with sessionStorage persistence
    // =============================================
    const CHAT_STORAGE_KEY = 'zaki_chat_history';
    const CHAT_OPEN_KEY    = 'zaki_chat_open';

    const chatbotTrigger = document.getElementById('chatbotTrigger');
    const chatbotWidget  = document.getElementById('chatbotWidget');
    const closeChatbot   = document.getElementById('closeChatbot');

    if (chatbotTrigger && chatbotWidget && closeChatbot) {

        console.log('[ZakiChat] v2 - sessionStorage persistence loaded');

        // ── helpers ──────────────────────────────────────────────
        function getHistory() {
            try { return JSON.parse(sessionStorage.getItem(CHAT_STORAGE_KEY)) || []; }
            catch { return []; }
        }
        function saveHistory(history) {
            sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(history));
        }

        const chatBody = chatbotWidget.querySelector('.ai-chat-body');
        const chatInput = chatbotWidget.querySelector('.ai-chat-input-wrapper input');
        const sendBtn   = chatbotWidget.querySelector('.ai-send-btn');

        // Fix all links in an element to open in same tab
        function fixLinks(container) {
            container.querySelectorAll('a[href]').forEach(function(a) {
                a.removeAttribute('target');
                a.removeAttribute('rel');
                // Intercept click to guarantee same-tab navigation
                a.addEventListener('click', function(e) {
                    var href = a.getAttribute('href');
                    if (href && !href.startsWith('http') || href.startsWith(window.location.origin)) {
                        e.preventDefault();
                        window.location.href = href;
                    }
                }, { once: true });
            });
        }

        // Render one bubble (raw html for ai, plain text for user)
        function renderBubble(role, content) {
            const div = document.createElement('div');
            div.className = `chat-bubble ${role}`;
            if (role === 'ai') {
                div.innerHTML = typeof marked !== 'undefined' ? marked.parse(content) : content;
                fixLinks(div);
            } else {
                div.innerText = content;
            }
            return div;
        }

        // Append a message, persist to sessionStorage, scroll down
        function appendMessage(role, text) {
            const div = renderBubble(role, text);
            chatBody.appendChild(div);
            chatBody.scrollTop = chatBody.scrollHeight;

            // Persist
            const history = getHistory();
            history.push({ role: role, content: text });
            saveHistory(history);
        }

        // ── restore history on page load ─────────────────────────
        function restoreHistory() {
            const history = getHistory();
            if (history.length === 0) return;

            // Hide the default welcome bubble & suggestions so we don't duplicate
            const defaultBubble = chatBody.querySelector('.chat-bubble.ai');
            if (defaultBubble) defaultBubble.style.display = 'none';
            const suggestions = chatBody.querySelector('.chat-suggestions');
            if (suggestions) suggestions.style.display = 'none';

            history.forEach(function(msg) {
                const div = renderBubble(msg.role, msg.content);
                chatBody.appendChild(div);
            });
            chatBody.scrollTop = chatBody.scrollHeight;
        }

        // ── MutationObserver: fix any link added dynamically ─────
        const linkObserver = new MutationObserver(function(mutations) {
            mutations.forEach(function(m) {
                m.addedNodes.forEach(function(node) {
                    if (node.nodeType === 1) fixLinks(node);
                });
            });
        });
        linkObserver.observe(chatBody, { childList: true, subtree: true });

        // ── open / close state ───────────────────────────────────
        function openWidget() {
            chatbotWidget.classList.add('active');
            chatbotTrigger.classList.add('hidden');
            sessionStorage.setItem(CHAT_OPEN_KEY, '1');
        }
        function closeWidget() {
            chatbotWidget.classList.remove('active');
            chatbotTrigger.classList.remove('hidden');
            sessionStorage.removeItem(CHAT_OPEN_KEY);
        }

        chatbotTrigger.addEventListener('click', openWidget);
        closeChatbot.addEventListener('click', closeWidget);

        // Re-open widget if it was open before navigation
        if (sessionStorage.getItem(CHAT_OPEN_KEY) === '1') {
            chatbotWidget.classList.add('active');
            chatbotTrigger.classList.add('hidden');
        }

        // ── clear history button ──────────────────────────────────
        const clearBtn = document.createElement('button');
        clearBtn.id = 'clearChatHistory';
        clearBtn.title = 'Xóa lịch sử chat';
        clearBtn.innerHTML = '<i class="bi bi-trash3"></i>';
        clearBtn.style.cssText = 'background:none;border:none;cursor:pointer;font-size:1rem;opacity:0.7;padding:0;';
        clearBtn.addEventListener('click', function() {
            sessionStorage.removeItem(CHAT_STORAGE_KEY);
            // Remove all bubbles except the initial greeting
            Array.from(chatBody.querySelectorAll('.chat-bubble, .chat-suggestions')).forEach(el => el.remove());
            // Re-add greeting
            const greeting = document.createElement('div');
            greeting.className = 'chat-bubble ai';
            greeting.innerText = 'Xin chào! Tôi là trợ lý ảo ZakiBooking. Tôi có thể giúp gì cho chuyến du lịch tiếp theo của bạn?';
            chatBody.appendChild(greeting);
            const chips = document.createElement('div');
            chips.className = 'chat-suggestions';
            chips.innerHTML = '<div class="suggestion-chip">Tour giá rẻ dưới 5 triệu</div><div class="suggestion-chip">Điểm đến hot nhất</div><div class="suggestion-chip">Tư vấn tour biển</div><div class="suggestion-chip">Tra cứu đơn hàng</div>';
            chatBody.appendChild(chips);
            attachChipListeners();
        });
        // Insert clear button into header next to close button
        const chatHeader = chatbotWidget.querySelector('.ai-chat-header');
        if (chatHeader) {
            const btnGroup = document.createElement('div');
            btnGroup.style.cssText = 'display:flex;align-items:center;gap:8px;';
            const closeBtn = chatHeader.querySelector('#closeChatbot');
            chatHeader.appendChild(btnGroup);
            btnGroup.appendChild(clearBtn);
            btnGroup.appendChild(closeBtn);
        }

        // ── suggestion chips ──────────────────────────────────────
        function attachChipListeners() {
            chatBody.querySelectorAll('.suggestion-chip').forEach(function(chip) {
                chip.addEventListener('click', function() {
                    chatInput.value = chip.textContent;
                    handleSend();
                });
            });
        }
        attachChipListeners();

        function getCsrfHeaders() {
            const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            const headers = { 'Content-Type': 'application/json' };
            if (token && header) {
                headers[header] = token;
            }
            return headers;
        }

        let chatSending = false;

        // ── send message ──────────────────────────────────────────
        async function handleSend() {
            const message = chatInput.value.trim();
            if (!message || chatSending) return;

            const suggestions = chatBody.querySelector('.chat-suggestions');
            if (suggestions) suggestions.style.display = 'none';

            appendMessage('user', message);
            chatInput.value = '';
            chatSending = true;
            sendBtn.disabled = true;

            const loadingDiv = document.createElement('div');
            loadingDiv.className = 'chat-bubble ai loading';
            loadingDiv.innerHTML = '<span class="chat-typing"><span></span><span></span><span></span></span>';
            chatBody.appendChild(loadingDiv);
            chatBody.scrollTop = chatBody.scrollHeight;

            try {
                const response = await fetch('/api/chat', {
                    method: 'POST',
                    headers: getCsrfHeaders(),
                    body: JSON.stringify({ message: message })
                });
                const data = await response.json().catch(function () { return {}; });
                chatBody.removeChild(loadingDiv);
                if (!response.ok) {
                    appendMessage('ai', data.reply || 'Không thể gửi tin nhắn. Vui lòng thử lại.');
                    return;
                }
                appendMessage('ai', data.reply || 'Xin lỗi, không nhận được phản hồi.');
            } catch (error) {
                if (chatBody.contains(loadingDiv)) chatBody.removeChild(loadingDiv);
                appendMessage('ai', 'Mất kết nối máy chủ. Kiểm tra mạng hoặc thử lại sau.');
            } finally {
                chatSending = false;
                sendBtn.disabled = false;
                chatInput.focus();
            }
        }

        sendBtn.addEventListener('click', handleSend);
        chatInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend();
            }
        });

        // Restore history last (after DOM is ready)
        restoreHistory();
    }

    // =============================================
    // Active link highlight dựa trên URL
    // =============================================
    var currentPath = window.location.pathname;
    document.querySelectorAll('.premium-nav-dock .nav-link').forEach(function (link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
});
