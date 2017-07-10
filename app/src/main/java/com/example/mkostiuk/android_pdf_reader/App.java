package com.example.mkostiuk.android_pdf_reader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.mkostiuk.android_pdf_reader.upnp.Etat;
import com.example.mkostiuk.android_pdf_reader.upnp.ServiceUpnp;
import com.joanzapata.pdfview.PDFView;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import xdroid.toaster.Toaster;

import static java.lang.String.format;

public class App extends AppCompatActivity implements com.joanzapata.pdfview.listener.OnPageChangeListener {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

    public static final String SAMPLE_FILE = "sample.pdf";

    public static final String ABOUT_FILE = "about.pdf";

    private ServiceUpnp service;


    PDFView pdfView;


    String pdfName = SAMPLE_FILE;


    Integer pageNumber = 1;


    void afterViews() {
        display(pdfName, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        //Vérification que l'autorisation d'accès au système de stockage est accrodée
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Cela signifie que la permission à déjà était
                //demandé et l'utilisateur l'a refusé
                //Vous pouvez aussi expliquer à l'utilisateur pourquoi
                //cette permission est nécessaire et la redemander
                Toaster.toast("Vous avez refusé l'accés au Stockage, fermeture");
                finish();
            } else {
                //Sinon demander la permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }
        else {
            //Permission déjà accrodée
            System.out.println("Initialisation");
            init();
        }
    }

    public void init() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        File dir;
        System.err.println(Build.BRAND);
        if (Build.BRAND.toString().equals("htc_europe"))
            dir = new File("/mnt/emmc/PdfReader/");
        else
            dir = new File(Environment.getExternalStorageDirectory().getPath() + "/PdfReader/");

        while (!dir.exists()) {
            dir.mkdir();
            dir.setReadable(true);
            dir.setExecutable(true);
            dir.setWritable(true);
        }

        service = new ServiceUpnp();


        getApplicationContext().bindService(new Intent(this, AndroidUpnpServiceImpl.class),
                service.getService(),
                Context.BIND_AUTO_CREATE);

        pdfView = (PDFView) findViewById(R.id.pdfview);

        display(SAMPLE_FILE, true);


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                service.getRecorderLocalService().getManager().getImplementation()
                        .getPropertyChangeSupport().addPropertyChangeListener(
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                if (evt.getPropertyName().equals("status")) {
                                    Etat etat = (Etat) evt.getNewValue();

                                    switch (etat) {
                                        case GAUCHE:
                                            pdfView.jumpTo(pdfView.getCurrentPage() - 1);
                                            break;
                                        case DROITE:
                                            pdfView.jumpTo(pdfView.getCurrentPage() + 1);
                                            break;
                                    }
                                }
                            }
                        }
                );
            }
        }, 5000);

    }



    private void display(String assetFileName, boolean jumpToFirstPage) {
        if (jumpToFirstPage) pageNumber = 1;
        setTitle(pdfName = assetFileName);

        pdfView.fromAsset(assetFileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .load();
    }

    private boolean displaying(String fileName) {
        return fileName.equals(pdfName);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(format("%s %s / %s", pdfName, page, pageCount));
    }

    @Override
    public void onBackPressed() {
        if (ABOUT_FILE.equals(pdfName)) {
            display(SAMPLE_FILE, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission est garantie on initialise les services et boutons
                    init();
                } else {
                    Toaster.toast("Permission refusée, fermeture");
                    finish();
                }
                return;
            }
        }
    }
}
