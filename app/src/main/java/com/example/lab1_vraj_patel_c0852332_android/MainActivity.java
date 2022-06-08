package com.example.lab1_vraj_patel_c0852332_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    List<Marker> arrayMkrs = new ArrayList<>();
    List<LatLng> arrayLtLg = new ArrayList<>();
    TextView labelDist;
    Polygon polygon = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelDist = findViewById(R.id.labeldist);
        labelDist.setVisibility(TextView.INVISIBLE);
        labelDist.setTextSize(30);
        MapLoading();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        if (arrayMkrs.size() == 0) {
            LatLngBounds boundsNorthAmerica = new LatLngBounds(new LatLng(43.273909, -127.120020), new LatLng(43.273909, -68.409081));
            int padding = 3;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsNorthAmerica, padding);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    map.moveCamera(cameraUpdate);
                }
            }, 100);
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                Marker marker = map.addMarker(markerOptions);

                if (arrayLtLg.size() < 4) {
                    arrayLtLg.add(latLng);
                    arrayMkrs.add(marker);

                    generatePolygon();
                }
            }
        });

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                for(Marker marker : arrayMkrs) {
                    if(Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05) {
                        arrayLtLg.remove(marker.getPosition());
                        arrayMkrs.remove(marker);

                        generatePolygon();
                        marker.remove();
                        break;
                    }
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Boolean isFound = false;
                for(Marker marker1 : arrayMkrs) {
                    if (Math.abs(marker1.getPosition().latitude - marker.getPosition().latitude) < 0.05 && Math.abs(marker.getPosition().longitude - marker.getPosition().longitude) < 0.05) {
                        isFound = true;
                    }
                }
                if (isFound == false) {
                    marker.remove();
                }
                return false;
            }
        });

        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                Log.d("LINE", polyline.getPoints().toString());
            }
        });

        googleMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                Log.d("added",polygon.toString());
                double total = 0.0;
                for (int i = 0; i < arrayLtLg.size(); i++) {
                    if (i == arrayLtLg.size() - 1) {
                        total += toCalculateDistance(arrayLtLg.get(i), arrayLtLg.get(0));
                    } else {
                        total += toCalculateDistance(arrayLtLg.get(i), arrayLtLg.get(i+1));
                    }
                }
                @SuppressLint({"NewApi", "LocalSuppress"}) Integer totalInInt = Math.toIntExact(Math.round(total));
                if (labelDist.getVisibility() == TextView.INVISIBLE) {
                    labelDist.setVisibility(TextView.VISIBLE);
                    labelDist.setText("Total Distance is :- " + totalInInt + " km");
                } else {
                    labelDist.setVisibility(TextView.INVISIBLE);
                    labelDist.setText("");
                }
            }
        });
    }
    void MapLoading() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    void generatePolygon() {
        if (arrayMkrs.size() >= 2) {
            if(polygon != null) polygon.remove();
            PolygonOptions polygonOptions = new PolygonOptions().addAll(arrayLtLg)
                    .clickable(true);
            polygonOptions.clickable(true);
            polygon = map.addPolygon(polygonOptions);


            polygon.setStrokeColor(Color.RED);
            polygon.setFillColor(Color.parseColor("#3500FF00"));
        }
    }

    double toCalculateDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lt1 = StartP.latitude;
        double lt2 = EndP.latitude;

        double lg1 = StartP.longitude;
        double lg2 = EndP.longitude;

        double disLt = Math.toRadians(lt2 - lt1);
        double disLg = Math.toRadians(lg2 - lg1);

        double a = Math.sin(disLt / 2) * Math.sin(disLt / 2)
                + Math.cos(Math.toRadians(lt1))
                * Math.cos(Math.toRadians(lt2)) * Math.sin(disLg / 2)
                * Math.sin(disLg / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        double valOutcome = Radius * c;
        double km = valOutcome / 1;

        DecimalFormat newFormat = new DecimalFormat("####");

        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valOutcome % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valOutcome + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

}