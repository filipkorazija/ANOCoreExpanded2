package me.astonic.ANOC2.xpboost;

public class ActiveBooster {

    private final double multiplier;
    private int duration;

    public ActiveBooster(double multiplier, int duration) {
        this.multiplier = multiplier;
        this.duration = duration;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getDuration() {
        return duration;
    }

    public void decreaseDuration() {
        this.duration--;
    }
} 