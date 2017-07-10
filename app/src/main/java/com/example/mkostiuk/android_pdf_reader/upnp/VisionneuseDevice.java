package com.example.mkostiuk.android_pdf_reader.upnp;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

/**
 * Created by mkostiuk on 10/07/2017.
 */

public class VisionneuseDevice {

    static LocalDevice createDevice(UDN udn) throws ValidationException {
        DeviceType type =
                new UDADeviceType("VisionneusePdf", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Visionneuse Pdf",
                        new ManufacturerDetails("IRIT"),
                        new ModelDetails("AndroidController", "Lit des fichiers PDF", "v1")
                );

        LocalService<VisionneuseService> service =
                new AnnotationLocalServiceBinder().read(VisionneuseService.class);

        service.setManager(
                new DefaultServiceManager<VisionneuseService>(service, VisionneuseService.class)
        );

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
                service);
    }
}
