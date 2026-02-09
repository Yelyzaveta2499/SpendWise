const GuideCharacter = (() => {
    const character = document.getElementById("guide-character");
    const sprite = character.querySelector(".character-sprite");
    const bubble = character.querySelector(".speech-bubble");
    const speechText = document.getElementById("speech-text");

    function showSpeech(text, timeout = 3000) {
        speechText.textContent = text;
        bubble.classList.remove("hidden");

        if (timeout) {
            setTimeout(() => bubble.classList.add("hidden"), timeout);
        }
    }

    function clearAnimations() {
        sprite.className = "character-sprite";
    }

    return {
        idle() {
            clearAnimations();
            sprite.style.animation = "idle-bounce 2s ease-in-out infinite";
        },

        wave() {
            clearAnimations();
            sprite.style.animation = "wave 0.8s ease-in-out 2";
            showSpeech("Hi there! 👋");
        },

        jump() {
            clearAnimations();
            sprite.style.animation = "jump 0.5s ease";
        },

        shake() {
            clearAnimations();
            sprite.style.animation = "shake 0.4s ease";
            showSpeech("Oops! Try again 🙂");
        },

        celebrate() {
            clearAnimations();
            sprite.style.animation = "celebrate 1s ease-in-out";
            showSpeech("Yay! Great job! 🎉");
        },

        speak(text) {
            showSpeech(text);
        }

    };

})();

//window.addEventListener("load", () => {
   // GuideCharacter.wave();
//});
