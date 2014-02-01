package org.droidplanner.glass.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.MAVLink.Messages.ApmModes;
import org.droidplanner.MAVLink.MavLinkArm;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.fragments.SettingsFragment;
import org.droidplanner.glass.fragments.GlassMapFragment;
import org.droidplanner.glass.fragments.HudFragment;
import org.droidplanner.glass.utils.GlassUtils;
import org.droidplanner.utils.Constants;
import org.droidplanner.utils.Utils;

import java.util.List;

/**
 * This is the main activity for the glass interface.
 *
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class GlassFlightActivity extends GlassActivity implements DroneInterfaces.OnDroneListener {

    /**
     * This is the activity fragment manager.
     *
     * @since 1.2.0
     */
    private FragmentManager mFragManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        updateConnectionPrefs();

        mFragManager = getSupportFragmentManager();

        Fragment glassFragment = getCurrentFragment();
        if (glassFragment == null) {
            glassFragment = new HudFragment();
            mFragManager.beginTransaction().add(R.id.glass_layout, glassFragment).commit();
        }
    }

    private void updateConnectionPrefs() {
        PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).edit().putString(Constants.PREF_CONNECTION_TYPE,
                Utils.ConnectionType.BLUETOOTH.name()).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_glass_activity, menu);

        //Fill the flight modes menu with all the implemented flight modes
        MenuItem flightModes = menu.findItem(R.id.menu_flight_modes);
        SubMenu flightModesMenu = flightModes.getSubMenu();

        //Get the list of apm modes for this drone
        List<ApmModes> apmModesList = ApmModes.getModeList(drone.type.getType());

        //Add them to the flight modes menu
        for (ApmModes apmMode : apmModesList) {
            flightModesMenu.add(apmMode.getName());
        }

        final boolean isDroneConnected = drone.MavClient.isConnected();

        //Update the toggle connection menu title
        final MenuItem connectMenuItem = menu.findItem(R.id.menu_connect);
        if (connectMenuItem != null) {
            connectMenuItem.setTitle(isDroneConnected
                    ? R.string.menu_disconnect
                    : R.string.menu_connect);
        }

        //Make the drone control menu visible if connected
        menu.setGroupVisible(R.id.menu_group_drone_connected, isDroneConnected);
        menu.setGroupEnabled(R.id.menu_group_drone_connected, isDroneConnected);

        //Update the drone arming state if connected
        if(isDroneConnected){
            final MenuItem armingMenuItem = menu.findItem(R.id.menu_arming_state);
            if(armingMenuItem != null){
                boolean isArmed = drone.state.isArmed();
                armingMenuItem.setTitle(isArmed ? R.string.menu_disarm : R.string.menu_arm);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.NONE: {
                //Handle the flight modes
                final String itemTitle = item.getTitle().toString();
                final ApmModes selectedMode = ApmModes.getMode(itemTitle, drone.type.getType());
                if (ApmModes.isValid(selectedMode)) {
                    drone.state.changeFlightMode(selectedMode);
                    return true;
                }

                return false;
            }

            case R.id.menu_glass_hud: {
                launchHud();
                return true;
            }

            /*case R.id.menu_glass_map: {
                launchMap();
                return true;
            }*/

            case R.id.menu_arming_state:
                toggleArming();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public CharSequence[][] getHelpItems() {
        return new CharSequence[0][];
    }

    private void toggleArming(){
        final boolean isDroneArmed = drone.state.isArmed();
        if (!isDroneArmed){
            drone.tts.speak("Arming the vehicle, please standby");
        }

        MavLinkArm.sendArmMessage(drone, !isDroneArmed);
    }

    private void launchHud() {
        //Replace the current fragment with the hud fragment.
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof HudFragment)) {
            currentFragment = new HudFragment();
            mFragManager.beginTransaction()
                    .replace(R.id.glass_layout, currentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void launchMap() {
        //Replace the current fragment with the map fragment.
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof GlassMapFragment)) {
            currentFragment = new GlassMapFragment();
            mFragManager.beginTransaction()
                    .replace(R.id.glass_layout, currentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Update the app navigation menu item based on the current fragment
        final Fragment currentFragment = getCurrentFragment();

        final MenuItem hudMenu = menu.findItem(R.id.menu_glass_hud);
        if (hudMenu != null) {
            final boolean isHudFragment = currentFragment instanceof HudFragment;
            hudMenu.setEnabled(!isHudFragment);
            hudMenu.setVisible(!isHudFragment);
        }

        /*final MenuItem mapMenu = menu.findItem(R.id.menu_glass_map);
        if (mapMenu != null) {
            final boolean isMapFragment = currentFragment instanceof GlassMapFragment;
            mapMenu.setEnabled(!isMapFragment);
            mapMenu.setVisible(!isMapFragment);
        }*/

        //TODO: If connected, update the title for the drone arming state.
        return true;
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (GlassUtils.isGlassDevice() && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (GlassUtils.isVoiceControlActive(getApplicationContext())) {
                openVoiceMenu();
            }
            else {
                openOptionsMenu();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Fragment getCurrentFragment() {
        return mFragManager.findFragmentById(R.id.glass_layout);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case CONNECTED:
            case DISCONNECTED:
            case ARMING:
                if (GlassUtils.isVoiceControlActive(getApplicationContext())) {
                    invalidateVoiceMenu();
                }
                else {
                    invalidateOptionsMenu();
                }
                break;
        }
    }
}
