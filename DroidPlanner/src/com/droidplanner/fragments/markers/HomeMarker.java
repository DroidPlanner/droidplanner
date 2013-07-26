package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.R.drawable;
import com.droidplanner.drone.variables.Home;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeMarker {
	public static MarkerOptions build(Home home) {
		return new MarkerOptions()
				.position(home.getCoord())
				.draggable(true)
				.title("Home")
				.snippet(
						String.format(Locale.ENGLISH, "%.1fm", home.getHeight()))
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(drawable.ic_menu_home)).title("Home");
	}

	public static void update(Marker marker, Home home) {
		marker.setPosition(home.getCoord());
		marker.setSnippet(String.format(Locale.ENGLISH, "%s %.1fm", home
				.getCmd().getName(), home.getHeight()));
	}

}