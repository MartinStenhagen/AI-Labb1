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
        <h1>AI-Labb1</h1>

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

        try {

            appendMessage("Du", message, "user-message");

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

            appendMessage("AI", data.answer, "ai-message");

        } catch (error) {

            appendMessage("System", "Kunde inte kontakta AI-tjänsten.", "error-message");

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

</script>

</body>
</html>