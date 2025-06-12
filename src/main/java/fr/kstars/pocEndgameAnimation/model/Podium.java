package fr.kstars.pocEndgameAnimation.model;

import org.bukkit.Location;

public class Podium {
    private final String winnerName;
    private final Location placeLocation;

    public Podium(String winnerName, Location placeLocation) {
        this.winnerName = winnerName;
        this.placeLocation = placeLocation;
    }

    public String getWinnerName() {
        return this.winnerName;
    }

    public Location getPlaceLocation() {
        return this.placeLocation;
    }
}
