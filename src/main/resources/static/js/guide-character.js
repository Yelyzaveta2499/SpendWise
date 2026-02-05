const GuideCharacter = (() => {
    const character = document.getElementById("guide-character");
    const sprite = character.querySelector(".character-sprite");
    const bubble = character.querySelector(".speech-bubble");
    const speechText = document.getElementById("speech-text");

    const positions = {
        bottomRight: { bottom: "20px", right: "20px", top: "auto", left: "auto" },
        bottomLeft:  { bottom: "20px", left: "20px", top: "auto", right: "auto" },
        topRight:    { top: "20px", right: "20px", bottom: "auto", left: "auto" },
        topLeft:     { top: "20px", left: "20px", bottom: "auto", right: "auto" }
    };

    function clearAnimation() {
        sprite.style.animation = "none";
        sprite.offsetHeight; // force reflow
    }

    function showSpeech(text, timeout = 3000) {
        speechText.textContent = text;
        bubble.classList.remove("hidden");

        if (timeout) {
            setTimeout(() => bubble.classList.add("hidden"), timeout);
        }
    }

    return {
        idle() {
            clearAnimation();
            sprite.style.animation = "idle-bounce 2s ease-in-out infinite";
        },

        wave() {
            clearAnimation();
            sprite.style.animation = "idle-bounce 1.2s ease-in-out infinite";
            showSpeech("Hi there! 👋");
        },

        jump() {
            clearAnimation();
            sprite.style.animation = "jump 0.5s ease";
        },

        shake() {
            clearAnimation();
            sprite.style.animation = "shake 0.4s ease";
            showSpeech("Oops! Try again 🙂");
        },

        celebrate() {
            clearAnimation();
            sprite.style.animation = "celebrate 1s ease-in-out";
            showSpeech("Yay! Great job! 🎉");
        },

        speak(text) {
            showSpeech(text);
        },

        moveTo(position) {
            const pos = positions[position];
            if (!pos) {
                console.warn("Unknown position:", position);
                return;
            }
            Object.assign(character.style, pos);
        },

        jumpTo(position) {
            this.jump();
            setTimeout(() => this.moveTo(position), 200);
        },

        randomMove() {
            const keys = Object.keys(positions);
            const random = keys[Math.floor(Math.random() * keys.length)];
            this.jumpTo(random);
        }
    };
})();
