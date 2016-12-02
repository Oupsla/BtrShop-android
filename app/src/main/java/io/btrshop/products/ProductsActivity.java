package io.btrshop.products;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.eddystone.Eddystone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.btrshop.BtrShopApplication;
import io.btrshop.R;
import io.btrshop.detailsproduct.DetailsProductActivity;
import io.btrshop.detailsproduct.domain.model.Product;
import io.btrshop.scanner.ScannerActivity;
import io.btrshop.util.EspressoIdlingResource;



public class ProductsActivity extends AppCompatActivity implements ProductsContract.View {

    // Constantes
    private final static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private final static int REQUEST_PERMISSION_PHONE_STATE = 1;
    protected final static String TAG = "ProductsFragment";

    private final double COEFF1 = 0.42093;
    private final double COEFF2 = 6.9476;
    private final double COEFF3 = 0.54992;

    // UI
    @Inject ProductsPresenter mProductsPresenter;
    @BindView(R.id.drawer_layout)DrawerLayout mDrawerLayout;
    @BindView(R.id.fab_scan_article) FloatingActionButton fab;
    //@BindView(R.id.products_test)TextView productTestET;
    static MaterialDialog dialog;

    // Components
    private BeaconManager beaconManager;
    private Region region;
    private int targetSdkVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_act);

        // Targer Version
        try {
            final PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Injection
        ButterKnife.bind(this);
        DaggerProductsComponent.builder()
                .apiComponent(((BtrShopApplication) getApplicationContext()).getApiComponent())
                .productsModule(new ProductsModule(this))
                .build().inject(this);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Navigation drawer.
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        // Floaction action button
        fab.setImageResource(R.mipmap.ic_barcode);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductsPresenter.scanProduct();
            }
        });

        // Permissions and bluetooth
        verifyBluetooth();
        checkAndRequestPermissions();

        beaconManager = new BeaconManager(this);
        beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override
            public void onEddystonesFound(List<Eddystone> list) {

            }
        });
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Log.d(TAG, "Nombres d'estimote captés: " + list.size());
                    for(Beacon beac : list){
                        Log.d(TAG, "Estimote mac : " + beac.getMacAddress());
                        Log.d(TAG, "Estimote power : " + beac.getMeasuredPower());
                        Log.d(TAG, "Estimote distance : " + calculateDistance(beac.getMeasuredPower(), beac.getRssi()));

                    }
                }
            }
        });

        region = new Region("ranged region", null, null, null);
        Log.i(TAG, "CIYYYYYYYYYYYYYYYYYYYYYYYYYC : "  + region.getProximityUUID());


    }

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        double distance;
        if (ratio < 1.0) {
            distance =  Math.pow(ratio,10);
        }
        else {
            distance =  (COEFF1)*Math.pow(ratio,COEFF2) + COEFF3;
        }
        return distance;
    }


    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }



    private void checkAndRequestPermissions() {
        /* Checking for permissions */
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!selfPermissionGranted(Manifest.permission.CAMERA)){
            permissionsNeeded.add("Camera");
            permissionsList.add(Manifest.permission.CAMERA);
        }
        if (!selfPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)){
            permissionsNeeded.add("Access Location");
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        /* Asking for permissions */
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }



    public boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = getApplicationContext().checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(getApplicationContext(), permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("result");
                Log.d("SCAN", result);
                dialog = new MaterialDialog.Builder(this)
                        .title("Récupération produit")
                        .content("Veuillez patientez ...")
                        .progress(true, 0)
                        .show();
                mProductsPresenter.getProduct(result);
            }
        }
    }

    @Override
    public void showScan() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void showProduct(final Product product){
        dialog.dismiss();
        Intent detailProductIntent = new Intent(this, DetailsProductActivity.class);
        detailProductIntent.putExtra("product", product);
        startActivity(detailProductIntent);
    }

    @Override
    public void showError() {

        dialog.dismiss();
        new MaterialDialog.Builder(this)
                .title("No Product !")
                .content("There is no product with this ean ! ")
                .positiveText("Ok")
                .show();
    }

    private void verifyBluetooth() {
        try {
            boolean bluetoothError = false;

            if (android.os.Build.VERSION.SDK_INT < 18) {
                throw new RuntimeException("Bluetooth LE not supported by this device");
            }
            if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                throw new RuntimeException("Bluetooth LE not supported by this device");
            } else {
                if (!((BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()) {
                    bluetoothError = true;
                }
            }
            if(bluetoothError){
                dialog = new MaterialDialog.Builder(ProductsActivity.this)
                        .title("Bluetooth not enabled")
                        .content("Please enable bluetooth in settings and restart this application.")
                        .positiveText("Ok")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.finishAffinity(ProductsActivity.this);
                                System.exit(0);
                            }
                        })
                        .show();
            }
        }
        catch (RuntimeException e) {
            dialog = new MaterialDialog.Builder(ProductsActivity.this)
                    .title("Bluetooth LE not available")
                    .content("Sorry, this device does not support Bluetooth LE.")
                    .positiveText("Ok")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ActivityCompat.finishAffinity(ProductsActivity.this);
                            System.exit(0);
                        }
                    })
                    .show();
        }

    }
}
