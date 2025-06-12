package fr.kstars.pocEndgameAnimation;

import fr.kstars.pocEndgameAnimation.cmd.EndgameAnimation;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin has been enabled!");

        Objects.requireNonNull(getCommand("endgame_animation")).setExecutor(new EndgameAnimation(this));
    }

    @Override
    public void onDisable() {
       getLogger().info("Plugin has been disabled!");
    }
}
