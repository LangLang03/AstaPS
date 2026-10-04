package emu.grasscutter.game.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pins which world areas count as Snezhnaya for the cold meter. */
public final class SnezhnayaFrigidTest {
    @Test
    @DisplayName("Snezhnaya's level-1 areas and their sub-areas are cold")
    public void snezhnayaIsCold() {
        assertTrue(SnezhnayaFrigid.isFrigidArea(3, 70));
        assertTrue(SnezhnayaFrigid.isFrigidArea(3, 76));
        assertTrue(SnezhnayaFrigid.isFrigidArea(3, 7001));
        assertTrue(SnezhnayaFrigid.isFrigidArea(3, 7105));
    }

    @Test
    @DisplayName("Nod-Krai, other regions and other scenes are not")
    public void elsewhereIsNot() {
        assertFalse(SnezhnayaFrigid.isFrigidArea(3, 55)); // Nod-Krai, also Cryo, has no Frigid
        assertFalse(SnezhnayaFrigid.isFrigidArea(3, 5501));
        assertFalse(SnezhnayaFrigid.isFrigidArea(3, 77));
        assertFalse(SnezhnayaFrigid.isFrigidArea(3, 1));
        assertFalse(SnezhnayaFrigid.isFrigidArea(3, 0));
        assertFalse(SnezhnayaFrigid.isFrigidArea(1162, 70)); // an indoor Snezhnaya scene
    }
}
