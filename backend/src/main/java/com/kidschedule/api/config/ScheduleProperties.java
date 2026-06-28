package com.kidschedule.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.schedule")
public class ScheduleProperties {

	private int horizonWeeks = 12;

	public int getHorizonWeeks() {
		return horizonWeeks;
	}

	public void setHorizonWeeks(int horizonWeeks) {
		this.horizonWeeks = horizonWeeks;
	}
}
