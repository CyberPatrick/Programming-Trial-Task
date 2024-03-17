package ee.taltech.food.services;

import ee.taltech.food.entities.StationEntity;
import ee.taltech.food.errors.ServiceError;
import ee.taltech.food.utils.City;
import ee.taltech.food.utils.VehicleType;
import ee.taltech.food.repositories.StationRepository;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DeliveryService {
    private final StationRepository repository;

    /**
     * Return delivery fee for given city and vehicle type, also, if given, calculate fee based on weather conditions,
     * which were valid at the specific time.
     * @param city City of delivery.
     * @param vehicleType Vehicle that deliver.
     * @param date Optional. Calculate fee based on weather conditions at specific time.
     * @return Calculated fee based on weather conditions and business rules.
     * @throws ServiceError If given args combination violates business rules (e.g. bike and wind speed > 20m/s.),
     * no weather data.
     */
    public Double getDeliveryFee(City city, VehicleType vehicleType, @Nullable Date date) throws ServiceError {
        Optional<StationEntity> stationOpt;
        if (Objects.isNull(date)) stationOpt =  repository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
        else stationOpt = repository.findFirstByNameContainingIgnoreCaseAndTimestampBeforeOrderByTimestampDesc(city, date);

        if (stationOpt.isEmpty()) throw new ServiceError("No weather data to calculate fee");
        StationEntity station = stationOpt.get();
        return city.getRBF(vehicleType)
                + getATEF(station, vehicleType)
                + getWSEF(station, vehicleType)
                + getWPEF(station, vehicleType);
    }

    /**
     * Calculate extra fee based on air temperature (ATEF).
     * @param station The parameter shows which station to take weather data from.
     * @param vehicleType Transport used for delivery.
     * @return Calculated extra fee, can be zero.
     */
    private double getATEF(StationEntity station, VehicleType vehicleType) {
        if (!vehicleType.equals(VehicleType.Car) && Objects.nonNull(station.getAirTemperature())) {
            if (station.getAirTemperature() < -10) {
                return  1d;
            } else if (-10f <= station.getAirTemperature() && station.getAirTemperature() <= 0f) {
                return 0.5d;
            }
        }
        return 0;
    }
    /**
     * Calculate extra fee based on wind speed (WSEF).
     * @param station The parameter shows which station to take weather data from.
     * @param vehicleType Transport used for delivery.
     * @return Calculated extra fee, can be zero.
     * @throws ServiceError Throws error if according to weather conditions and business rules,
     * selected transport usage is forbidden.
     */
    private double getWSEF(StationEntity station, VehicleType vehicleType) throws ServiceError {
        if (vehicleType.equals(VehicleType.Bike) && Objects.nonNull(station.getWindSpeed())) {
            if (station.getWindSpeed() > 20f) {
                throw new ServiceError("Usage of selected vehicle type is forbidden");
            } else if (10f <= station.getWindSpeed() && station.getWindSpeed() <= 20f) {
                return 0.5d;
            }
        }
        return 0;
    }
    /**
     * Calculate extra fee based on weather phenomenon (WPEF).
     * @param station The parameter shows which station to take weather data from.
     * @param vehicleType Transport used for delivery.
     * @return Calculated extra fee, can be zero.
     * @throws ServiceError Throws error if according to weather conditions and business rules,
     * selected transport usage is forbidden.
     */
    private double getWPEF(StationEntity station, VehicleType vehicleType) throws ServiceError {
        if (!vehicleType.equals(VehicleType.Car) && Objects.nonNull(station.getPhenomenon())) {
            if (station.getPhenomenon().toLowerCase(Locale.ROOT).contains("snow")
                    || station.getPhenomenon().toLowerCase(Locale.ROOT).contains("sleet")) {
                return 1d;
            } else if (station.getPhenomenon().toLowerCase(Locale.ROOT).contains("rain")) {
                return 0.5d;
            } else if (station.getPhenomenon().toLowerCase(Locale.ROOT).equals("glaze")
                    || station.getPhenomenon().toLowerCase(Locale.ROOT).equals("hail")
                    || station.getPhenomenon().toLowerCase(Locale.ROOT).contains("thunder")) {
                throw new ServiceError("Usage of selected vehicle type is forbidden");
            }
        }
        return 0;
    }
}
