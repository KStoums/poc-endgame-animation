package fr.kstars.poc_endgame_animation.cmd;

import fr.kstars.poc_endgame_animation.model.Podium;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EndgameAnimation implements CommandExecutor {
    private final JavaPlugin plugin;

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

                    teleportPlayerFrontOfPodium(player);
                    spawnTopPlayersOnPodium(player);
                    return;
                }

                Title startingAnimationTitle = Title.title(
                        Component.text("§cEndGame Animation"),
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
        Location endGameSpecLocation = new Location(Bukkit.getWorld("lobby_world"), -1.725, 101, 0.503);
        endGameSpecLocation.setYaw(-90);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "PODIUM SPECTATE");
        npc.spawn(endGameSpecLocation);
        //TODO SET PLAYER CAMERA TO NPC AND UNLOCK WHEN TELEPORTED IN LOBBY
    }

    private void teleportPlayerInLobby(Player player) {
        Title gameTerminatedTitle = Title.title(
                Component.text("§cGame Terminated"),
                Component.text("Teleport to lobby..."),
                Title.Times.times(Duration.ofSeconds(3), Duration.ofSeconds(3), Duration.ofSeconds(3))
        );

        player.showTitle(gameTerminatedTitle);
        player.showPlayer(plugin, player);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.2f);
        player.teleport(player.getWorld().getSpawnLocation());
        CitizensAPI.getNPCRegistry().deregisterAll();
    }

    private void spawnTopPlayersOnPodium(Player player) {
        Location firstPlaceLocation = new Location(Bukkit.getWorld("lobby_world"), 1.463, 102.500, 0.452, 90f, 0);
        Location secondPlaceLocation = new Location(Bukkit.getWorld("lobby_world"), 1.387, 102, 1.445, 90f, 0);
        Location lastPlaceLocation = new Location(Bukkit.getWorld("lobby_world"), 1.438, 101.500, -0.508, 90f, 0);

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

                Objects.requireNonNull(Bukkit.getWorld("lobby_world")).strikeLightningEffect(lastPlaceLocation);

                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, podiumList.get(index).getWinnerName());
                Objects.requireNonNull(podiumList.get(index).getPlaceLocation().getWorld()).strikeLightningEffect(podiumList.get(index).getPlaceLocation());
                npc.spawn(podiumList.get(index).getPlaceLocation());
                index--;
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }
}

