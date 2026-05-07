<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>AI Chat</title>
    <link rel="stylesheet" href="/css/chat.css">
</head>
<body>

<main class="chat-container">

    <header class="chat-header">
        <h1>AI-Chat</h1>

        <label for="personality" class="sr-only">Välj personlighet</label>
        <div class="personality-field">
            <label for="personality">Välj AI-personlighet</label>
            <select id="personality">
                <option value="helper">Helper – tydlig och hjälpsam</option>
                <option value="coder">Coder – fokuserad på kod och exempel</option>
                <option value="pirate">Pirate – svarar med piratstil</option>
            </select>
        </div>
    </header>

    <section id="chat-history" class="chat-history">
    </section>

    <section class="chat-input-area">
        <label for="message" class="sr-only">Meddelande</label>
        <textarea
                id="message"
                rows="4"
                placeholder="Skriv ett meddelande..."></textarea>

        <button id="send-button" onclick="sendMessage()">
            Skicka
        </button>
    </section>

</main>

<script>

    const sessionId = crypto.randomUUID();

    const textarea = document.getElementById("message");

    textarea.addEventListener("keydown", function (event) {

        if (event.key === "Enter" && !event.shiftKey) {

            event.preventDefault();

            sendMessage();
        }
    });

    async function sendMessage() {

        const messageInput = /** @type {HTMLTextAreaElement} */ (document.getElementById("message"));
        const sendButton = /** @type {HTMLButtonElement} */ (document.getElementById("send-button"));
        const personalitySelect = /** @type {HTMLSelectElement} */ (document.getElementById("personality"));

        const message = messageInput.value;

        if (!message.trim()) {
            return;
        }

        sendButton.disabled = true;
        messageInput.disabled = true;

        let loadingMessage = null;

        try {
            appendMessage("Du", message, "user-message");

            loadingMessage = appendLoadingMessage();

            messageInput.value = "";

            const response = await fetch("/api/v1/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    personality: personalitySelect.value,
                    message: message,
                    sessionId: sessionId
                })
            });

            const data = await response.json();

            removeLoadingMessage(loadingMessage);

            if (!response.ok) {
                appendMessage(
                    "System",
                    data.message || "Något gick fel när AI-tjänsten anropades.",
                    "error-message"
                );
                return;
            }

            appendMessage("AI", data.answer, "ai-message");

        } catch (error) {
            removeLoadingMessage(loadingMessage);

            appendMessage(
                "System",
                "Kunde inte kontakta servern.",
                "error-message"
            );

        } finally {
            sendButton.disabled = false;
            messageInput.disabled = false;
            messageInput.focus();
        }
    }

    function appendMessage(sender, text, cssClass) {
        const history = document.getElementById("chat-history");

        const wrapper = document.createElement("div");
        wrapper.className = "message " + cssClass;

        const senderElement = document.createElement("strong");
        senderElement.textContent = sender;

        const textElement = document.createElement("p");
        textElement.textContent = text;

        wrapper.appendChild(senderElement);
        wrapper.appendChild(textElement);

        history.appendChild(wrapper);
        history.scrollTop = history.scrollHeight;
    }

    function appendLoadingMessage() {
        const history = document.getElementById("chat-history");

        const wrapper = document.createElement("div");
        wrapper.className = "message ai-message loading-message";

        const senderElement = document.createElement("strong");
        senderElement.textContent = "AI";

        const textElement = document.createElement("p");
        textElement.textContent = "AI skriver";

        wrapper.appendChild(senderElement);
        wrapper.appendChild(textElement);

        history.appendChild(wrapper);
        history.scrollTop = history.scrollHeight;

        let dots = 0;

        const intervalId = setInterval(function () {
            dots = (dots + 1) % 4;
            textElement.textContent = "AI skriver" + ".".repeat(dots);
        }, 500);

        wrapper.dataset.intervalId = intervalId.toString();

        return wrapper;
    }

    function removeLoadingMessage(element) {
        if (!element) {
            return;
        }

        const intervalId = element.dataset.intervalId;

        if (intervalId) {
            clearInterval(Number(intervalId));
        }

        element.remove();
    }

</script>

</body>
</html>