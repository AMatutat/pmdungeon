package de.fhbielefeld.pmdungeon.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import de.fhbielefeld.pmdungeon.game.GameWorld;
import de.fhbielefeld.pmdungeon.game.interactable.Chest;
import de.fhbielefeld.pmdungeon.game.interactable.Interactable;
import de.fhbielefeld.pmdungeon.game.items.Item;

import java.util.HashMap;
import java.util.Map;

import static de.fhbielefeld.pmdungeon.screens.GameScreen.VIRTUAL_HEIGHT;

/**
 * Holds the HUD.
 */
public class HeadUpDisplay implements Disposable {

    private static final float HEART_SIZE = 0.4f;
    private static final float ITEM_BACKGROUND_SIZE = 0.5f;
    private static final float INVENTORY_ITEM_SIZE = 0.5f;
    private static final int CHEST_GRID = 3;

    private final GameWorld gameWorld;
    private final SpriteBatch hudBatch;
    private final OrthographicCamera hudCamera;
    private final Texture heartFull = new Texture("textures/ui/ui_heart_full.png");
    private final Texture heartHalf = new Texture("textures/ui/ui_heart_half.png");
    private final Texture heartEmpty = new Texture("textures/ui/ui_heart_empty.png");
    private final Texture background = createBackground();
    private final Map<Rectangle, Integer> chestInventoryInputs = new HashMap<>();

    public HeadUpDisplay(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        hudBatch = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.position.set(0, 0, 0);
        hudCamera.update();
    }

    /**
     * Main loop of the hud.
     */
    public void render() {
        hudCamera.update();
        hudBatch.setProjectionMatrix(hudCamera.combined);

        hudBatch.begin();
        if (gameWorld.getHero() != null) {
            drawHealthPoints();
        }
        Chest openChest = findOpenChest();
        if (openChest != null) {
            drawOpenChest();
            if (Gdx.input.isTouched()) {
                checkOpenChestInput(openChest);
            }
        }
        hudBatch.end();
    }

    /**
     * Draws health points at the upper left corner of the screen.
     */
    private void drawHealthPoints() {
        int i = 0;
        for (; i < Math.floor(gameWorld.getHero().getHealthPoints()); i++) {
            hudBatch.draw(heartFull, i * HEART_SIZE, VIRTUAL_HEIGHT - HEART_SIZE, HEART_SIZE, HEART_SIZE);
        }
        if (gameWorld.getHero().getHealthPoints() % 1 != 0) {
            hudBatch.draw(heartHalf, i++ * HEART_SIZE, VIRTUAL_HEIGHT - HEART_SIZE, HEART_SIZE, HEART_SIZE);
        }
        for (; i < gameWorld.getHero().getMaxHealthPoints(); i++) {
            hudBatch.draw(heartEmpty, i * HEART_SIZE, VIRTUAL_HEIGHT - HEART_SIZE, HEART_SIZE, HEART_SIZE);
        }
    }

    /**
     * todo how to remove this?
     * Draws the inventory at the bottom of the screen.
     */
   /* private void drawInventory() {
        Texture highlight = createInventoryHighlight();
        InventoryComponent inventory = gameWorld.getHero().getInventory();

        float originX = ((VIRTUAL_HEIGHT * Gdx.graphics.getWidth() / Gdx.graphics.getHeight()) / 2) - (inventory.getSize() * ITEM_BACKGROUND_SIZE) / 2;

        Item[] items = inventory.getItems();
        for (int i = 0; i < inventory.getSize(); i++) {
            hudBatch.draw(background, originX + i * ITEM_BACKGROUND_SIZE, 0, ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE);
            if (items[i] != null) {
                if (inventory.getSelectedItem() != null && items[i] == inventory.getSelectedItem()) {
                    hudBatch.draw(highlight, originX + i * ITEM_BACKGROUND_SIZE, 0, ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE / 12);
                }
                float itemOffset = ITEM_BACKGROUND_SIZE / 2 - calculateItemWidth(items[i]) / 2;
                hudBatch.draw(items[i].getTexture(), (originX + i * INVENTORY_ITEM_SIZE) + itemOffset, 0, calculateItemWidth(items[i]), INVENTORY_ITEM_SIZE);
            }
        }
    }*/

    /**
     * Creates a black, slightly translucent background texture for the inventory.
     *
     * @return Black and slightly translucent texture
     */
    private Texture createBackground() {
        Pixmap backgroundPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        backgroundPixmap.setColor(0, 0, 0, 0.3f);
        backgroundPixmap.fill();
        Texture texture = new Texture(backgroundPixmap);
        backgroundPixmap.dispose();
        return texture;
    }

    /**
     * Creates a yellow highlight for the inventory.
     *
     * @return Yellow highlight texture
     */
    private Texture createInventoryHighlight() {
        Pixmap highlightPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        highlightPixmap.setColor(1, 1, 0, 0.5f);
        highlightPixmap.fill();
        Texture highlight = new Texture(highlightPixmap);
        highlightPixmap.dispose();
        return highlight;
    }

    /**
     * Calculates the size in which the item will be drawn to the screen.
     *
     * @param item Item that will be drawn
     * @return Width of the item texture
     */
    private float calculateItemWidth(Item item) {
        return (float) item.getTexture().getWidth() / (float) item.getTexture().getHeight() * INVENTORY_ITEM_SIZE;
    }

    /**
     * Draws the HUD overlay for an opened chest.
     */
    private void drawOpenChest() {
        Chest chest = findOpenChest();
        if (chest != null) {
            chestInventoryInputs.clear();
            Vector3 screen = hudCamera.unproject(new Vector3(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0));
            float originX = screen.x / 2 - (CHEST_GRID * ITEM_BACKGROUND_SIZE) / 2;
            float originY = VIRTUAL_HEIGHT / 2 - (CHEST_GRID * ITEM_BACKGROUND_SIZE) / 2;

            Item[] chestContent = chest.getContent();
            int counter = 0;
            for (int i = CHEST_GRID - 1; i >= 0; i--) {
                for (int j = 0; j < CHEST_GRID; j++) {
                    hudBatch.draw(background, originX + j * ITEM_BACKGROUND_SIZE, originY + i * ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE);
                    if (chestContent[counter] != null) {
                        Item item = chestContent[counter];
                        float itemOffset = ITEM_BACKGROUND_SIZE / 2 - calculateItemWidth(item) / 2;
                        hudBatch.draw(item.getTexture(), (originX + j * INVENTORY_ITEM_SIZE) + itemOffset, originY + i * INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE);
                        chestInventoryInputs.put(new Rectangle(originX + j * ITEM_BACKGROUND_SIZE, originY + i * ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE, ITEM_BACKGROUND_SIZE), counter);
                    }
                    counter++;
                }
            }
        }
    }

    /**
     * Searches for any chest with the state set to open
     *
     * @return An open chest, if there is one
     */
    private Chest findOpenChest() {
        for (Interactable interactable : gameWorld.getInteractables()) {
            if (interactable.getClass() == Chest.class && ((Chest) interactable).getState() == Chest.State.OPEN) {
                return (Chest) interactable;
            }
        }
        return null;
    }

    /**
     * Check if there was a click on the overlay of the opened chest.
     *
     * @param openChest An opened chest
     */
    private void checkOpenChestInput(Chest openChest) {
        for (Map.Entry<Rectangle, Integer> entry : chestInventoryInputs.entrySet()) {
            Vector3 inputPos = hudCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (entry.getKey().contains(inputPos.x, inputPos.y)) {
               //todo take item from chest
            }
        }
    }

    /**
     * Resizing the camera according to the size of the window.
     *
     * @param width  Window width
     * @param height Window height
     */
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, VIRTUAL_HEIGHT * width / (float) height, VIRTUAL_HEIGHT);
        hudBatch.setProjectionMatrix(hudCamera.combined);
    }

    @Override
    public void dispose() {
        hudBatch.dispose();
        heartEmpty.dispose();
        heartHalf.dispose();
        heartFull.dispose();
        background.dispose();
    }
}
