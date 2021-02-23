package de.fhbielefeld.pmdungeon.game.characters;

import com.badlogic.gdx.graphics.Texture;
import de.fhbielefeld.pmdungeon.game.GameWorld;


public class MaleKnight extends Character {

    private static final float MOVEMENT_SPEED = 5;
    private static final float MAX_HEALTH = 5f;
    public static final int TEXTURE_COUNT = 4;

    public MaleKnight(GameWorld gameWorld) {
        super(gameWorld, MAX_HEALTH, MOVEMENT_SPEED);
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
            idle.addTexture(new Texture("textures/characters/playercharacters/knight_m_idle_anim_f" + i + ".png"));
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
            run.addTexture(new Texture("textures/characters/playercharacters/knight_m_run_anim_f" + i + ".png"));
        }
        return run;
    }
}
