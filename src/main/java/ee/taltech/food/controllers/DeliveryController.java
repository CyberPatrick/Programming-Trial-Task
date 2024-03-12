package ee.taltech.food.controllers;

import ee.taltech.food.customEditors.CaseInsensitiveEnumEditor;
import ee.taltech.food.errors.ServiceError;
import ee.taltech.food.forms.City;
import ee.taltech.food.forms.VehicleType;
import ee.taltech.food.services.DeliveryService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @GetMapping("fee")
    public ResponseEntity<Double> getDeliveryFee(@RequestParam @NonNull City city,
                                                 @RequestParam @NonNull VehicleType vehicleType,
                                                 @RequestParam(required = false) @NonNull
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date)
            throws ServiceError {
        return new ResponseEntity<>(deliveryService.getDeliveryFee(city, vehicleType, date), HttpStatus.OK);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(City.class, new CaseInsensitiveEnumEditor(City.class));
        binder.registerCustomEditor(VehicleType.class, new CaseInsensitiveEnumEditor(VehicleType.class));
    }
}
