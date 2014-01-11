package net.dwdg.jumpports.util;

import org.bukkit.potion.PotionEffectType;

public class PortEffect {

	private PotionEffectType potionEffect;
	private int duration;
	private int amplifier;

	public PotionEffectType getPotionEffect() {
		return potionEffect;
	}

	public void setPotionEffect(PotionEffectType potionEffect) {
		this.potionEffect = potionEffect;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getAmplifier() {
		return amplifier;
	}

	public void setAmplifier(int amplifier) {
		this.amplifier = amplifier;
	}

}
