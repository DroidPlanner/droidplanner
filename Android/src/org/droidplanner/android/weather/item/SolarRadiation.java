package org.droidplanner.android.weather.item;

public class SolarRadiation implements IWeatherItem {
	
	private int kIndex;
	public static final int MIDDLE_VALUE = 4;
	
	public SolarRadiation (int kIndex){
		this.kIndex = kIndex;
	}
	
	public int getkIndex(){
		return kIndex;
	}

}
