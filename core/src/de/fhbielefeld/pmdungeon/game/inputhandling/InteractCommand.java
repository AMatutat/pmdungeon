package de.fhbielefeld.pmdungeon.game.inputhandling;

import de.fhbielefeld.pmdungeon.game.characters.Character;

public class InteractCommand implements Command {

    @Override
    public void execute(Character character) {
        character.interact();
    }
}
