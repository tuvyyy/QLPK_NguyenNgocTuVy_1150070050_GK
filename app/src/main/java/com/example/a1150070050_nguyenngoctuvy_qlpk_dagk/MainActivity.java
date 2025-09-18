package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities.LoginActivity;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.AppointmentsFragment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.DoctorsFragment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.HomeFragment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.PatientsFragment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.ProfileFragment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments.ServicesFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                                getSupportFragmentManager().popBackStack();
                            } else {
                                finish();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment()).commit();
        } else if (id == R.id.nav_patients) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new PatientsFragment()).commit();
        } else if (id == R.id.nav_doctors) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new DoctorsFragment()).commit();
        } else if (id == R.id.nav_appointments) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new AppointmentsFragment()).commit();
        } else if (id == R.id.nav_services) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ServicesFragment()).commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ProfileFragment()).commit();
        } else if (id == R.id.nav_logout) {
            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
