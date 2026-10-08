package emu.grasscutter.game.world;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.PlayerProperty;

/**
 * Region gating for Natlan-only mechanics.
 *
 * <p>Several mechanics are pushed to the client unconditionally, which makes the client draw the
 * phlogiston gauge (and the related Natlan HUD) in every region. The push sites are:
 *
 * <ul>
 *   <li>{@code AbilityManager.initializeEntityAbilities} - avatar abilities
 *   <li>{@code Player.applyStartingProperties} - the phlogiston enable property
 *   <li>{@code PacketPlayerEnterSceneInfoNotify} / {@code PacketSyncTeamEntityNotify} - the
 *       {@code SGV_PlayerTeam_Phlogiston} team value
 * </ul>
 *
 * <p>{@code TeamManager.getAbilityControlBlock} already gated the team-ability path on scene 101;
 * this class is the single place the other sites ask, so they cannot drift apart again.
 */
public final class RegionGate {
    /** Natlan is scene 101 in every client build that ships Natlan. */
    public static final int NATLAN_SCENE_ID = 101;

    private RegionGate() {}

    /** True when the player is currently standing in Natlan. */
    public static boolean inNatlan(Player player) {
        if (player == null) {
            return false;
        }
        var scene = player.getScene();
        if (scene != null) {
            return scene.getId() == NATLAN_SCENE_ID;
        }
        // No live scene yet (login, or between scenes): fall back to the recorded scene id so
        // reconnecting into Natlan still shows the gauge.
        return player.getSceneId() == NATLAN_SCENE_ID;
    }

    /** True when the given scene id is Natlan. */
    public static boolean isNatlanScene(int sceneId) {
        return sceneId == NATLAN_SCENE_ID;
    }

    /**
     * Applies the Natlan-dependent player properties for the scene the player is now in.
     *
     * <p>Called from {@code Scene.addPlayer}, after the scene and scene id are set, so the value
     * always matches the region the player is actually standing in. Outside Natlan the phlogiston
     * gauge is disabled, which is what stops the client from drawing it in Mondstadt and the rest.
     */
    public static void applyRegionProperties(Player player) {
        if (player == null) {
            return;
        }
        player.setProperty(PlayerProperty.PROP_PHLOGISTON_ENABLE, inNatlan(player) ? 1 : 0);
    }
}
