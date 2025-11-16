/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "sitesync_prefs";
    private static final String KEY_SHOW_EXIT = "show_exit_dialog";


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;

    private void showNotification(String message, String action){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(action.equals(getString(R.string.try_again))){
                            askForPermission();
                        }
                    }
                })
                .show();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showNotification(getString(R.string.permission_granted), getString(R.string.dismiss));
                } else {
                    showNotification(getString(R.string.permission_denied), getString(R.string.try_again));
                }
            });

    protected void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        else{
            showNotification(getString(R.string.permission_granted), getString(R.string.dismiss));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForPermission();

        // Set Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set Up bottomNavigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Enable Hamburger Icon ☰ to Open Drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();




        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        // Load default fragment (HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            // Set the correct item as selected in bottom navigation
            if(LoginScreen.isEmployer){
                loadFragment((new JobListingsFragment()));
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
            else{
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }


        }


        //Bottom Nav
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();

                if (id == R.id.nav_jobs) {
                    loadFragment(new JobBoardFragment());
                    return true;
                } else if (id == R.id.nav_home) {
                    if(LoginScreen.isEmployer){
                        loadFragment(new JobListingsFragment());
                        return true;
                    }
                    else{
                        loadFragment(new HomeFragment());
                        return true;
                    }
                } else if (id == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }

                return false;
            }
        });



        //Hamburger Nav
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_help) {
                    //Do Nothing For Now
                } else if (id == R.id.nav_settings) {
                    loadFragment(new SettingsFragment());
                }else if(id == R.id.nav_profile){
                    loadFragment(new ProfileFragment());
                }

                drawerLayout.closeDrawers(); // Close drawer after selection
                return true;
            }
        });

        // Handle back press with confirmation dialog
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
                boolean showExit = sp.getBoolean(KEY_SHOW_EXIT, true); // default = true

                if (showExit) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.exit_application)
                            .setMessage(R.string.exitMsgMain)
                            .setIcon(R.drawable.sitesynclogo)
                            .setPositiveButton(R.string.YesButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish(); // Closes the current activity
                                }
                            })
                            .setNegativeButton(R.string.NoButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })

                            .show();
                } else {

                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // Utility method to load fragments
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.top_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Remember to change the load fragments to the new ones

       if (id == R.id.action_help) {
            loadFragment(new HelpFaqFragment());
            return true;
        } else if (id == R.id.action_permisons) {
            loadFragment(new PermissionsFragment());
            return true;
          } else if (id == R.id.action_feedback) {
            loadFragment(new FeedbackFragment());
            return true;
        } else if (id == R.id.action_about) {
            loadFragment(new AboutFragment());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
