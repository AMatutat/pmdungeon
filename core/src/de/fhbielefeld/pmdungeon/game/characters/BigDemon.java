package de.fhbielefeld.pmdungeon.game.characters;

import com.badlogic.gdx.graphics.Texture;
import de.fhbielefeld.pmdungeon.game.GameWorld;
import de.fhbielefeld.pmdungeon.game.characters.components.InputComponent;

/**
 * Boss character
 */
public class BigDemon extends Character {

    private static final float CHARACTER_WIDTH = 2f;
    private static final float MOVEMENT_SPEED = 4f;
    private static final float MAX_HEALTH_POINTS = 15f;
    private static final int INVENTORY_SIZE = 1;
    private static final int TEXTURE_COUNT = 4;

    public BigDemon(GameWorld gameWorld, InputComponent inputComponent) {
        super(gameWorld, inputComponent, MAX_HEALTH_POINTS, MOVEMENT_SPEED, INVENTORY_SIZE);
    }

    /**
     * Setting up idle animation
     *
     * @return Idle animation of the character
     */
    @Override
    protected Animation setupIdleAnimation() {
        Animation idle = new Animation(0.2f);
        for (int i = 0; i < TEXTURE_COUNT; i++) {
            idle.addTexture(new Texture("textures/characters/demons/big_demon/big_demon_idle_anim_f" + i + ".png"));
        }
        return idle;
    }

    /**
     * Setting up running animation
     *
     * @return Running animation of the character
     */
    @Override
    protected Animation setupRunAnimation() {
        Animation run = new Animation(0.1f);
        for (int i = 0; i < TEXTURE_COUNT; i++) {
            run.addTexture(new Texture("textures/characters/demons/big_demon/big_demon_run_anim_f" + i + ".png"));
        }
        return run;
    }

    /**
     * Overwriting character width to render this one bigger than the others
     *
     * @return Character width specific to this boss
     */
    @Override
    public float getCharacterWidth() {
        return CHARACTER_WIDTH;
    }
}
