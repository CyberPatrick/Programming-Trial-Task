package ee.taltech.food.services;

import ee.taltech.food.entities.StationEntity;
import ee.taltech.food.errors.ServiceError;
import ee.taltech.food.forms.City;
import ee.taltech.food.forms.VehicleType;
import ee.taltech.food.repositories.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class DeliveryService {
    private final StationRepository repository;

    public Double getDeliveryFee(City city, VehicleType vehicleType) throws ServiceError {
        StationEntity station = repository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
        return city.getRBF(vehicleType)
                + getATEF(station, vehicleType)
                + getWSEF(station, vehicleType)
                + getWPEF(station, vehicleType);
    }

    private float getATEF(StationEntity station, VehicleType vehicleType) {
        if (!vehicleType.equals(VehicleType.Car) && Objects.nonNull(station.getAirTemperature())) {
            if (station.getAirTemperature() < -10) {
                return  1;
            } else if (-10 <= station.getAirTemperature() && station.getAirTemperature() <= 0) {
                return 0.5f;
            }
        }
        return 0;
    }

    private float getWSEF(StationEntity station, VehicleType vehicleType) throws ServiceError {
        if (vehicleType.equals(VehicleType.Bike) && Objects.nonNull(station.getWindSpeed())) {
            if (station.getWindSpeed() > 20) {
                throw new ServiceError("Usage of selected vehicle type is forbidden");
            } else if (10 <= station.getAirTemperature() && station.getAirTemperature() <= 20) {
                return 0.5f;
            }
        }
        return 0;
    }

    private float getWPEF(StationEntity station, VehicleType vehicleType) throws ServiceError {
        if (!vehicleType.equals(VehicleType.Car) && Objects.nonNull(station.getPhenomenon())) {
            if (station.getPhenomenon().contains("snow") || station.getPhenomenon().contains("sleet")) {
                return 1;
            } else if (station.getPhenomenon().contains("rain")) {
                return 0.5f;
            } else if (station.getPhenomenon().equals("Glaze")
                    || station.getPhenomenon().equals("Hail")
                    || station.getPhenomenon().contains("Thunder")) {
                throw new ServiceError("Usage of selected vehicle type is forbidden");
            }
        }
        return 0;
    }
}
