package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.dungeons.DomainDungeonHelper;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AbilityScalarTypeOuterClass;
import emu.grasscutter.net.proto.AbilityScalarValueEntryOuterClass;
import emu.grasscutter.net.proto.AbilityStringOuterClass;
import emu.grasscutter.net.proto.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import emu.grasscutter.net.proto.SyncTeamEntityNotifyOuterClass.SyncTeamEntityNotify;
import emu.grasscutter.net.proto.TeamEntityInfoOuterClass.TeamEntityInfo;
import emu.grasscutter.utils.Utils;

public class PacketSyncTeamEntityNotify extends BasePacket {

    public PacketSyncTeamEntityNotify(Player player) {
        super(PacketOpcodes.SyncTeamEntityNotify);

        // Phlogiston is a Natlan-only team value. Pushing it everywhere made the client draw the
        // phlogiston gauge in every region, so it is only synced while the player is in Natlan.
        AbilitySyncStateInfo phlogiston;
        if (emu.grasscutter.game.world.RegionGate.inNatlan(player)) {
            AbilityScalarValueEntryOuterClass.AbilityScalarValueEntry scalarValue = AbilityScalarValueEntryOuterClass.AbilityScalarValueEntry.newBuilder()
                    .setKey(AbilityStringOuterClass.AbilityString.newBuilder().setHash(Utils.abilityHash("SGV_PlayerTeam_Phlogiston"))
                            .setStr("SGV_PlayerTeam_Phlogiston")
                            .build())
                    .setFloatValue(player.getPhlogistonValue())
                    .build();
            phlogiston = AbilitySyncStateInfo.newBuilder().addSgvDynamicValueMap(scalarValue).build();
        } else {
            phlogiston = AbilitySyncStateInfo.newBuilder().build();
        }

        SyncTeamEntityNotify.Builder proto =
                SyncTeamEntityNotify.newBuilder().setSceneId(DomainDungeonHelper.notifySceneId(player));

        if (player.getWorld().isMultiplayer()) {
            for (var p : player.getWorld()) {
                // Skip if same player
                if (player == p) {
                    continue;
                }

                // Set info
                TeamEntityInfo info =
                        TeamEntityInfo.newBuilder()
                                .setTeamEntityId(p.getTeamManager().getEntity().getId())
                                .setAuthorityPeerId(p.getPeerId())
                                .setTeamAbilityInfo(phlogiston)
                                .build();

                proto.addTeamEntityInfoList(info);
            }
        }

        this.setData(proto);
    }
}
