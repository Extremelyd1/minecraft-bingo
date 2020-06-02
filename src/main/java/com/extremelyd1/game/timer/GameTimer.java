package com.extremelyd1.game.timer;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer {

    private final Plugin plugin;
    private final long period;
    private long timeLeft;
    private final TimerConsumer timerConsumer;

    private BukkitRunnable runnable;

    public GameTimer(Plugin plugin, long period, long time, TimerConsumer timerConsumer) {
        this.plugin = plugin;
        this.period = period;
        this.timeLeft = time;
        this.timerConsumer = timerConsumer;
    }

    public void start() {
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (timerConsumer.onTimer(timeLeft)) {
                    cancel();
                }

                timeLeft--;
            }
        };

        runnable.runTaskTimer(
                plugin,
                0,
                period * 20
        );
    }

    public void cancel() {
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }
    }

}
