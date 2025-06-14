package fr.kstars.pocEndgameAnimation.cmd;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import fr.kstars.pocEndgameAnimation.model.Podium;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EndgameAnimation implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private final ArrayList<NPC> npcs = new ArrayList<>();
    private GameMode lastPlayerGameMode = GameMode.SURVIVAL;

    private final Location worldSpawnLocation = new Location(Bukkit.getWorld("world"), 26.480, 0, 7.504, 0, 0);

    public EndgameAnimation(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String msg, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length != 0 && !msg.isEmpty()) {
            player.sendMessage("§cUsage: /endgame_animation");
            return false;
        }

        new BukkitRunnable() {
            int startingAnimationTitleCooldown = 3;

            @Override
            public void run() {
                if (startingAnimationTitleCooldown <= 0) {
                    cancel();
                    player.resetTitle();

                    lastPlayerGameMode = player.getGameMode();
                    player.setGameMode(GameMode.SPECTATOR);
                    teleportPlayerFrontOfPodium(player);
                    spawnTopPlayersOnPodium(player);
                    return;
                }

                Title startingAnimationTitle = Title.title(
                        Component.text("§4EndGame Animation"),
                        Component.text("Starting in " + startingAnimationTitleCooldown + " second(s)"),
                        Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(1), Duration.ofSeconds(1))
                );

                player.showTitle(startingAnimationTitle);
                startingAnimationTitleCooldown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }

    private void teleportPlayerFrontOfPodium(Player player) {
        Location endGameSpecLocation = new Location(Bukkit.getWorld("world"), 23.476, -8, 9.496, 180, 0);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "PODIUM SPECTATE NPC");
        npc.spawn(endGameSpecLocation);
        npcs.add(npc);

        new BukkitRunnable() {
            @Override
            public void run() {
                setCameraToEntity(player, npc.getEntity());
            }
        }.runTaskLater(plugin, 1L);
    }

    private void teleportPlayerInLobby(Player player) {
        Title gameTerminatedTitle = Title.title(
                Component.text("§4Game Terminated"),
                Component.text("Teleport to lobby..."),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );

        player.showTitle(gameTerminatedTitle);

        new BukkitRunnable() {

            @Override
            public void run() {
                removeCameraToEntity(player);
                player.teleport(worldSpawnLocation);
                player.setGameMode(lastPlayerGameMode);

                for (NPC npc : npcs) {
                    CitizensAPI.getNPCRegistry().deregister(npc);
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    private void spawnTopPlayersOnPodium(Player player) {
        Location firstPlaceLocation = new Location(Bukkit.getWorld("world"), 23.450, -6.500, 8.465, 0, 0);
        Location secondPlaceLocation = new Location(Bukkit.getWorld("world"), 24.473, -7, 8.452, 0, 0);
        Location lastPlaceLocation = new Location(Bukkit.getWorld("world"), 22.460, -7.500, 8.462, 0, 0);

        List<Podium> podiumList = new ArrayList<>(List.of(
                new Podium("Kstars_", firstPlaceLocation),
                new Podium("Niromash_", secondPlaceLocation),
                new Podium("Violetow9", lastPlaceLocation)
        ));

        new BukkitRunnable() {
            int index = podiumList.toArray().length-1;

            @Override
            public void run() {
                if (index < 0) {
                    cancel();
                    teleportPlayerInLobby(player);
                    return;
                }

                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, podiumList.get(index).getWinnerName());
                Objects.requireNonNull(podiumList.get(index).getPlaceLocation().getWorld()).strikeLightningEffect(podiumList.get(index).getPlaceLocation());
                npc.spawn(podiumList.get(index).getPlaceLocation());
                npcs.add(npc);

                Sound podiumPlaceSound = switch (index) {
                    case 1 -> Sound.ENTITY_WITHER_SPAWN; //Top 2
                    case 0 -> Sound.UI_TOAST_CHALLENGE_COMPLETE; //Top 1
                    default -> Sound.ENTITY_PLAYER_LEVELUP; //Top 3 and another if ur podium is > 3 slots
                };

                player.playSound(player.getLocation(), podiumPlaceSound, 1, 1);
                index--;
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    public void setCameraToEntity(Player player, Entity entity) {
        PacketContainer cameraPacket = protocolManager.createPacket(PacketType.Play.Server.CAMERA);

        cameraPacket.getIntegers().write(0, entity.getEntityId());

        try {
            protocolManager.sendServerPacket(player, cameraPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeCameraToEntity(Player player) {
        PacketContainer cameraPacket = protocolManager.createPacket(PacketType.Play.Server.CAMERA);

        cameraPacket.getIntegers().write(0, player.getEntityId());

        try {
            protocolManager.sendServerPacket(player, cameraPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

