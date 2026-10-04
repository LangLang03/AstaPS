package emu.grasscutter.game.player;

import java.util.List;

/**
 * Snezhnaya's cold meter (Frigid) in the open world.
 *
 * <p>The meter itself - building up, the warmth from fire, the extra chill from water, the damage
 * once it fills - all runs on the client from {@code SceneObj_Area_Zd_SnezhnayaFrigid_TeamAbility_Basic}.
 * The server only has to put that team ability on while the player is in Snezhnaya, and nothing in
 * the client data says where that is: the official server attaches it from its own tables. So it
 * follows the world area the client reports, which for Snezhnaya is level-1 areas 70 to 76.
 *
 * <p>The colder Lv2/Lv3 zones and the warm areas are separate abilities and are not attached here,
 * since there is no data mapping them to places.
 */
public final class SnezhnayaFrigid {
    static final String BASIC = "SceneObj_Area_Zd_SnezhnayaFrigid_TeamAbility_Basic";

    private static final int BIG_WORLD = 3;
    private static final int FIRST_AREA = 70;
    private static final int LAST_AREA = 76;

    private SnezhnayaFrigid() {}

    /**
     * Whether an area is in Snezhnaya. Big-world level-1 area IDs are below 100 and each level-2
     * area is its parent's ID times 100 plus its own number, so 7001 sits in area 70.
     */
    static boolean isFrigidArea(int sceneId, int areaId) {
        if (sceneId != BIG_WORLD || areaId <= 0) return false;
        int parent = areaId >= 100 ? areaId / 100 : areaId;
        return parent >= FIRST_AREA && parent <= LAST_AREA;
    }

    /** Puts the cold meter on or takes it off after the client reports a new area. */
    public static void onAreaChange(Player player) {
        boolean wanted = isFrigidArea(player.getSceneId(), player.getAreaId());
        if (wanted != TeamAbilityToggle.isOn(player, BASIC)) {
            TeamAbilityToggle.set(player, List.of(BASIC), wanted);
        }
    }

    /**
     * Drops the cold meter when the player leaves the open world, so it does not follow them into a
     * domain or the teapot. The new scene's ability block is sent after this, so nothing else is.
     */
    public static void onSceneChange(Player player, int newSceneId) {
        if (newSceneId != BIG_WORLD && TeamAbilityToggle.isOn(player, BASIC)) {
            TeamAbilityToggle.set(player, List.of(BASIC), false, false);
        }
    }
}
