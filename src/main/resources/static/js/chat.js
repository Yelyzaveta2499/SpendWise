/**
 * SpendWise AI Chat – chat.js
 * Handles open/close of the floating chat panel and communication with /api/chat
 */
(function () {
    'use strict';

    // ── DOM refs (resolved after DOMContentLoaded) ──────────────────────────
    let openBtn, overlay, panel, messagesEl, inputEl, sendBtn, clearBtn, closeBtn, welcomeEl;

    // ── In-memory conversation history for multi-turn context ────────────────
    let history = [];
    let busy = false;

    // ── Init ─────────────────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        openBtn     = document.getElementById('chatOpenBtn');
        overlay     = document.getElementById('chatOverlay');
        panel       = document.getElementById('chatPanel');
        messagesEl  = document.getElementById('chatMessages');
        inputEl     = document.getElementById('chatInput');
        sendBtn     = document.getElementById('chatSendBtn');
        clearBtn    = document.getElementById('chatClearBtn');
        closeBtn    = document.getElementById('chatCloseBtn');
        welcomeEl   = document.getElementById('chatWelcome');

        if (!openBtn) return; // guard – not on this page

        openBtn.addEventListener('click', openChat);
        overlay.addEventListener('click', closeChat);
        closeBtn.addEventListener('click', closeChat);
        clearBtn.addEventListener('click', clearChat);
        sendBtn.addEventListener('click', sendMessage);

        inputEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // Auto-resize textarea
        inputEl.addEventListener('input', () => {
            inputEl.style.height = 'auto';
            inputEl.style.height = Math.min(inputEl.scrollHeight, 100) + 'px';
        });

        // Suggestion chips
        document.querySelectorAll('.chat-suggestion-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                inputEl.value = chip.textContent.trim();
                sendMessage();
            });
        });
    });

    // ── Panel open / close ────────────────────────────────────────────────────
    function openChat() {
        overlay.classList.add('open');
        panel.classList.add('open');
        inputEl.focus();
    }

    function closeChat() {
        overlay.classList.remove('open');
        panel.classList.remove('open');
    }

    function clearChat() {
        history = [];
        messagesEl.innerHTML = '';
        if (welcomeEl) {
            const clone = welcomeEl.cloneNode(true);
            clone.style.display = '';
            messagesEl.appendChild(clone);
            // Re-bind chips on cloned welcome block
            clone.querySelectorAll('.chat-suggestion-chip').forEach(chip => {
                chip.addEventListener('click', () => {
                    inputEl.value = chip.textContent.trim();
                    sendMessage();
                });
            });
        }
    }

    // ── Send message ──────────────────────────────────────────────────────────
    async function sendMessage() {
        const text = inputEl.value.trim();
        if (!text || busy) return;

        // Hide welcome on first message
        const welcome = messagesEl.querySelector('.chat-welcome');
        if (welcome) welcome.remove();

        // Append user bubble
        appendBubble('user', text);

        // Reset input
        inputEl.value = '';
        inputEl.style.height = 'auto';

        // Show typing indicator
        const typingEl = appendTyping();
        setBusy(true);

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text, history })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Something went wrong');
            }

            const reply = data.reply;

            // Update history for multi-turn
            history.push({ role: 'user', content: text });
            history.push({ role: 'assistant', content: reply });

            // Keep history capped at 20 turns (40 entries) to avoid huge payloads
            if (history.length > 40) history = history.slice(-40);

            typingEl.remove();
            appendBubble('bot', reply);

        } catch (err) {
            typingEl.remove();
            appendError(err.message || 'Could not reach SpendWise AI. Please try again.');
        } finally {
            setBusy(false);
        }
    }

    // ── DOM helpers ───────────────────────────────────────────────────────────
    function appendBubble(role, text) {
        const isBot = role === 'bot';
        const wrapper = document.createElement('div');
        wrapper.className = `chat-message ${role}`;

        const avatar = document.createElement('div');
        avatar.className = 'chat-msg-avatar';
        avatar.textContent = isBot ? '🐷' : 'U';

        const bubble = document.createElement('div');
        bubble.className = 'chat-msg-bubble';

        // Render markdown-lite: bold, newlines, bullets
        bubble.innerHTML = formatText(text);

        wrapper.appendChild(avatar);
        wrapper.appendChild(bubble);
        messagesEl.appendChild(wrapper);
        scrollBottom();
        return wrapper;
    }

    function appendTyping() {
        const wrapper = document.createElement('div');
        wrapper.className = 'chat-typing';

        const avatar = document.createElement('div');
        avatar.className = 'chat-msg-avatar';
        avatar.style.background = 'linear-gradient(135deg,#0ea5e9,#6366f1)';
        avatar.textContent = '🐷';

        const bubble = document.createElement('div');
        bubble.className = 'chat-typing-bubble';
        bubble.innerHTML = '<span class="chat-typing-dot"></span><span class="chat-typing-dot"></span><span class="chat-typing-dot"></span>';

        wrapper.appendChild(avatar);
        wrapper.appendChild(bubble);
        messagesEl.appendChild(wrapper);
        scrollBottom();
        return wrapper;
    }

    function appendError(msg) {
        const el = document.createElement('div');
        el.className = 'chat-msg-error';
        el.textContent = '⚠️ ' + msg;
        messagesEl.appendChild(el);
        scrollBottom();
    }

    function setBusy(state) {
        busy = state;
        sendBtn.disabled = state;
        inputEl.disabled = state;
    }

    function scrollBottom() {
        messagesEl.scrollTo({ top: messagesEl.scrollHeight, behavior: 'smooth' });
    }

    /**
     * Very light markdown-like formatter:
     * - **bold**
     * - newlines → <br>
     * - lines starting with - or * → bullet list
     */
    function formatText(raw) {
        if (!raw) return '';

        // Escape HTML entities first
        let escaped = raw
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');

        const lines = escaped.split('\n');
        let html = '';
        let inList = false;

        for (const line of lines) {
            const trimmed = line.trimStart();
            const isBullet = /^[-*•]\s+/.test(trimmed);

            if (isBullet) {
                if (!inList) { html += '<ul style="margin:6px 0 6px 18px;padding:0;">'; inList = true; }
                const content = trimmed.replace(/^[-*•]\s+/, '');
                html += `<li style="margin-bottom:3px;">${applyInline(content)}</li>`;
            } else {
                if (inList) { html += '</ul>'; inList = false; }
                if (trimmed === '') {
                    html += '<br>';
                } else {
                    html += `<span>${applyInline(trimmed)}</span><br>`;
                }
            }
        }

        if (inList) html += '</ul>';

        // Clean up trailing <br>
        html = html.replace(/(<br>\s*)+$/, '');

        return html;
    }

    function applyInline(text) {
        // **bold**
        return text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    }

})();

