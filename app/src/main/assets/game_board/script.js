"use strict";

/**
 * Public function called from the Java side to create all HTML elements for the game board.
 * This should only be called exactly once at the very beginning,
 *
 * @param {Integer} size Base size of the game board (e.g. 4, 9 or 16).
 * @param {Integer] sectionSize How many fields make up one section (2, 3 or 4).
 */
function createGameBoard(size, sectionSize) {
    // Avoid race condition on page load
    if (document.readyState !== "complete") {
        window.addTimeout(() => createGameBoard(size, sectionSize), 1000);
        return;
    }

    // Delete old fields, if any
    let gameBoardElement = document.querySelector("#game-board");
    gameBoardElement.innerHTML = "";

    // Create game field elements
    for (let xPos = 0; xPos < size; xPos++) {
        // Determine right border
        let borderRightClass = "border-right-normal";

        if (((xPos + 1) % sectionSize == 0) && ((xPos + 1) < size)) {
            borderRightClass = "border-right-section";
        }

        // Scan rows
        for (let yPos = 0; yPos < size; yPos++) {
            // Determine bottom border
            let borderBottomClass = "border-bottom-normal";

            if (((yPos + 1) % sectionSize == 0) && ((yPos + 1) < size)) {
                borderBottomClass = "border-bottom-section";
            }

            // Append game field
            let gameFieldElement = document.createElement("div");
            gameBoardElement.appendChild(gameFieldElement);

            gameFieldElement.style.gridColumn = xPos + 1;
            gameFieldElement.style.gridRow = yPos + 1;

            gameFieldElement.classList.add("game-field");
            gameFieldElement.classList.add(`size-${size}`);
            gameFieldElement.classList.add(`xpos-${xPos}`);
            gameFieldElement.classList.add(`ypos-${yPos}`);
            gameFieldElement.classList.add(borderRightClass);
            gameFieldElement.classList.add(borderBottomClass);

            gameFieldElement.dataset.xPos = xPos;
            gameFieldElement.dataset.yPos = yPos;

            // Append character and pencil elements
            let characterElement = document.createElement("div");
            characterElement.classList.add("character");
            gameFieldElement.appendChild(characterElement);

            let pencilElement = document.createElement("div");
            pencilElement.classList.add("pencil");
            gameFieldElement.appendChild(pencilElement);

            // Attach event listener to select field on click
            gameFieldElement.addEventListener("click", event => {
                let clickedField = event.target;

                while (clickedField && !clickedField.classList.contains("game-field")) {
                    clickedField = clickedField.parentNode;
                }

                let fieldWasSelected = clickedField.classList.contains("selected");
                let xPos = -1, yPos = -1;

                document.querySelectorAll(".game-field.selected").forEach(e => e.classList.remove("selected"));

                if (!fieldWasSelected) {
                    clickedField.classList.add("selected");

                    if (clickedField.dataset.xPos && clickedField.dataset.yPos) {
                        xPos = parseInt(clickedField.dataset.xPos);
                        yPos = parseInt(clickedField.dataset.yPos);
                    }
                }

                Android.onFieldSelected(xPos, yPos);
            });
        }
    }
}

/**
 * Public function called by the Java side to display the contents of the character fields.
 * We will always get all fields of the whole game board. Therefore, before any values are shown,
 * the previous values should be wiped.
 *
 * @param {List<CharacterFieldEntity>} All game board fields
 */
function updateCharacterFields(characterFields) {
    // Avoid race condition on page load
    if (document.readyState !== "complete") {
        window.addTimeout(() => updateCharacterFields(characterFields), 1000);
        return;
    }

    characterFields.forEach(characterField => {
        let xPos = characterField.xPos, yPos = characterField.yPos;

        let gameFieldElement = document.querySelector(`.xpos-${xPos}.ypos-${yPos}`);
        let characterElement = gameFieldElement.querySelector(".character");
        let pencilElement = gameFieldElement.querySelector(".pencil");

        characterElement.textContent = characterField.character;
        pencilElement.textContent = "";
        characterField.pencil.forEach(p => pencilElement.textContent += ` ${p}`);

        if (characterField.words.length > 0) {
            gameFieldElement.classList.add("word");
        } else {
            gameFieldElement.classList.remove("word");
        }

        if (characterField.locked) {
            gameFieldElement.classList.add("locked");
        } else {
            gameFieldElement.classList.remove("locked");
        }
    });
}

/**
 * Public function called by the Java side to highlight related fields, where no duplicate values
 * are accepted according to the game logic. The given list can either be empty to remove all
 * highlights or contain (x,y) coordinate pairs to highlight. In any case should all previously
 * active highlights be replaced.
 *
 * @param {List<GameLogic.Coordinate>} Coordinates to highlight
 */
function highlightFields(coordinates) {
    // Avoid race condition on page load
    if (document.readyState !== "complete") {
        window.addTimeout(() => highlightFields(coordinates), 1000);
        return;
    }

    document.querySelectorAll(".game-field.highlighted").forEach(e => e.classList.remove("highlighted"));

    coordinates.forEach(coordinate => {
        let xPos = coordinate.xPos, yPos = coordinate.yPos;
        let gameFieldElement = document.querySelector(`.xpos-${xPos}.ypos-${yPos}`);
        gameFieldElement.classList.add("highlighted");
    });
}