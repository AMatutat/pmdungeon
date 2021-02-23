package de.fhbielefeld.pmdungeon.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import de.fhbielefeld.pmdungeon.game.GameWorld;
import de.fhbielefeld.pmdungeon.game.characters.components.GraphicsComponent;
import de.fhbielefeld.pmdungeon.game.characters.components.InputComponent;
import de.fhbielefeld.pmdungeon.game.dungeon.dungeonconverter.Coordinate;
import de.fhbielefeld.pmdungeon.game.dungeon.tiles.Tile;
import de.fhbielefeld.pmdungeon.game.interactable.Interactable;

/**
 * Represents a character of the game
 */
public abstract class Character implements Disposable {

    private static final float CHARACTER_WIDTH = 1;
    private static final float INTERACTABLE_REACH = 1.5f;
    private static final float PUNCH_BACK_INTENSITY = 15f;
    private static final float PUNCH_BACK_DURATION = 100;

    protected GameWorld gameWorld;
    protected InputComponent inputComponent;
    protected GraphicsComponent graphicsComponent;

    protected boolean idle = true;
    private boolean dead = false;
    protected boolean facingLeft = false;
    private boolean movementEnabled = true;
    private boolean punched = false;
    private long punchStart;
    private Vector2 punchBackDirection = new Vector2();
    protected float positionX = 0;
    protected float positionY = 0;
    private float oldX = 0;
    private float oldY = 0;

    private float healthPoints;
    private final float maxHealthPoints;
    private final float movementSpeed;

    public Character(GameWorld gameWorld, InputComponent inputComponent, float maxHealthPoints, float movementSpeed) {
        this.gameWorld = gameWorld;
        this.inputComponent = inputComponent;
        this.healthPoints = this.maxHealthPoints = maxHealthPoints;
        this.movementSpeed = movementSpeed;
        this.graphicsComponent = new GraphicsComponent(this, setupIdleAnimation(), setupRunAnimation());
    }

    //todo loot from chest
    //todo add inventory

    /**
     * Characters are setting up their specific idle animation
     *
     * @return Idle animation
     */
    protected abstract Animation setupIdleAnimation();

    /**
     * Characters are setting up their specific running animation
     *
     * @return Running animation
     */
    protected abstract Animation setupRunAnimation();

    public void update() {
        if (punched) {
            updatePositionWhilePunchBack();
        }
        if (dead) dispose();
        if (inputComponent != null) inputComponent.update(this);

        checkForIdle();
    }

    /**
     * Determines the position of a character after it was hit. Updates it over time.
     */
    private void updatePositionWhilePunchBack() {
        disableMovement();
        if (TimeUtils.timeSinceMillis(punchStart) < PUNCH_BACK_DURATION) {
            float nextX = positionX + punchBackDirection.x * Gdx.graphics.getDeltaTime() * PUNCH_BACK_INTENSITY;
            float nextY = positionY + punchBackDirection.y * Gdx.graphics.getDeltaTime() * PUNCH_BACK_INTENSITY;
            if (gameWorld.getDungeon().isTileAccessible((int) nextX, (int) nextY)) {
                positionX = nextX;
                positionY = nextY;
            }
        } else {
            punched = false;
            enableMovement();
        }
    }

    /**
     * Checks if a character is moving or in idle
     */
    private void checkForIdle() {
        idle = positionX == oldX && positionY == oldY;
        oldX = positionX;
        oldY = positionY;
    }

    /**
     * Delegates render call to characters graphics component
     */
    public void render() {
        graphicsComponent.update(gameWorld.getBatch());
    }

    /**
     * Moving character up if target tile is accessible
     */
    public void moveUp() {
        if (movementEnabled) {
            float nextY = positionY + getMovementSpeed() * Gdx.graphics.getDeltaTime();
            if (gameWorld.getDungeon().isTileAccessible((int) positionX, (int) nextY)) positionY = nextY;
        }
    }

    /**
     * Moving character down if target tile is accessible
     */
    public void moveDown() {
        if (movementEnabled) {
            float nextY = positionY - getMovementSpeed() * Gdx.graphics.getDeltaTime();
            if (gameWorld.getDungeon().isTileAccessible((int) positionX, (int) nextY)) positionY = nextY;
        }
    }

    /**
     * Moving character left if target tile is accessible
     */
    public void moveLeft() {
        if (movementEnabled) {
            facingLeft = true;
            float nextX = positionX - getMovementSpeed() * Gdx.graphics.getDeltaTime();
            if (gameWorld.getDungeon().isTileAccessible((int) nextX, (int) positionY)) positionX = nextX;
        }
    }

    /**
     * Moving character right if target tile is accessible
     */
    public void moveRight() {
        if (movementEnabled) {
            facingLeft = false;
            float nextX = positionX + getMovementSpeed() * Gdx.graphics.getDeltaTime();
            if (gameWorld.getDungeon().isTileAccessible((int) nextX, (int) positionY)) positionX = nextX;
        }
    }

    /**
     * Interacts with nearest interactable, if there is one
     */
    public void interact() {
        Interactable interactable = gameWorld.nearestInteractable(this);
        if (interactable != null && distanceBetween(interactable.getPositionX(), interactable.getPositionY()) < INTERACTABLE_REACH) {
            interactable.interact(this);
        }
    }


    /**
     * Determines the nearest character
     *
     * @return Nearest character from the current one
     */
    public Character nearestCharacter() {
        float minDistance = Float.MAX_VALUE;
        Character returnCharacter = null;
        for (Character character : gameWorld.getCharacterList()) {
            if (character != this) {
                float distance = distanceBetween(character);
                if (minDistance > distance) {
                    minDistance = distance;
                    returnCharacter = character;
                }
            }
        }
        return returnCharacter;
    }

    /**
     * Calculates the distance between the current and another character
     *
     * @param character The other character
     * @return Distance between the two characters
     */
    public float distanceBetween(Character character) {
        return distanceBetween(character.getPositionX(), character.getPositionY());
    }

    /**
     * Calculates the position between the currents character and another position
     *
     * @param x X coordinate of the position
     * @param y Y coordinate of the position
     * @return Distance between the character and the coordinates
     */
    public float distanceBetween(float x, float y) {
        return (float) Math.sqrt(Math.pow(this.positionX - x, 2) + Math.pow(this.positionY - y, 2));
    }

    /**
     * Attacks a given character with a given damage. Can't be the same character.
     *
     * @param character Character that's attacked
     * @param damage    Amount of damage
     */
    public void attack(Character character, float damage) {
        if (this != character) {
            character.decreaseHealth(damage);
        }
    }

    /**
     * Call if a punch should be triggered. Calculates the direction of the punch back. Attacked player gets punched back in a straight line
     *
     * @param from The attacker
     */
    public void punchBack(Character from) {
        punched = true;
        Vector2 vector = new Vector2(positionX - from.getPositionX(), positionY - from.getPositionY());
        vector.nor();
        punchBackDirection = vector;
        punchStart = TimeUtils.millis();
    }

    /**
     * Decreases healthpoints of this character by the given amount and checks if the character is dead.
     *
     * @param damage Amount of healthpoints to be removed
     */
    private void decreaseHealth(float damage) {
        this.healthPoints -= damage;
        if (this.healthPoints <= 0) {
            dead = true;
        }
    }

    /**
     * Increase healthpoints of this character by the given amount.
     *
     * @param heal Amount of healthpoints to be restored
     */
    public void heal(float heal) {
        if (heal > 0) {
            if (this.healthPoints + heal >= getMaxHealthPoints()) {
                this.healthPoints = getMaxHealthPoints();
            } else {
                this.healthPoints += heal;
            }
        }
    }


    public void setPosition(Coordinate position) {
        this.positionX = position.getX();
        this.positionY = position.getY();
    }

    public Tile currentTile() {
        return gameWorld.getDungeon().getTileAt((int) positionX, (int) positionY);
    }

    public void enableMovement() {
        this.movementEnabled = true;
    }

    public void disableMovement() {
        this.movementEnabled = false;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public float getHealthPoints() {
        return healthPoints;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isIdle() {
        return idle;
    }

    public float getCharacterWidth() {
        return CHARACTER_WIDTH;
    }

    public float getMaxHealthPoints() {
        return maxHealthPoints;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void dispose() {
        graphicsComponent.dispose();
    }
}
