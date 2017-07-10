package com.example.mkostiuk.android_pdf_reader.upnp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;

/**
 * Created by mkostiuk on 10/07/2017.
 */

public class ServiceUpnp {

    private AndroidUpnpService upnpService;
    private UDN udnVisionneuse;
    private ServiceConnection serviceConnection;

    public ServiceUpnp() {

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                upnpService = (AndroidUpnpService) service;


                LocalService<VisionneuseService> remoteControllerService = getRecorderLocalService();

                // Register the device when this activity binds to the service for the first time
                if (remoteControllerService == null) {
                    try {
                        System.err.println("CREATION DEVICE!!!");
                        udnVisionneuse = new SaveUDN().getUdn();
                        LocalDevice remoteDevice = VisionneuseDevice.createDevice(udnVisionneuse);

                        upnpService.getRegistry().addDevice(remoteDevice);

                    } catch (Exception ex) {
                        System.err.println("Creating Android remote controller device failed !!!");
                        return;
                    }
                }

                System.out.println("Creation device reussie...");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    public LocalService<VisionneuseService> getRecorderLocalService() {

        if (upnpService == null)
            return null;

        LocalDevice remoteDevice;
        if ((remoteDevice = upnpService.getRegistry().getLocalDevice(udnVisionneuse, true)) == null)
            return null;

        return (LocalService<VisionneuseService>)
                remoteDevice.findService(new UDAServiceType("VisionneuseService",1));

    }

    public ServiceConnection getService() {
        return serviceConnection;
    }

    public void stop() {
        upnpService.get().shutdown();
    }

    public UDN getUdn() {
        return udnVisionneuse;
    }
}
