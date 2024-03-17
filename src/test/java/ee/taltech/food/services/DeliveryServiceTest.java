package ee.taltech.food.services;

import ee.taltech.food.entities.StationEntity;
import ee.taltech.food.errors.ServiceError;
import ee.taltech.food.utils.City;
import ee.taltech.food.utils.VehicleType;
import ee.taltech.food.mapper.StationMapper;
import ee.taltech.food.repositories.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    @InjectMocks
    private DeliveryService deliveryService;
    @Mock
    private StationRepository stationRepository;
    @Spy
    private StationMapper stationMapper = Mappers.getMapper(StationMapper.class);

    private static Random random = new Random();

    @Test
    void getDeliveryFee_NoWeatherData_ThrowsServiceError() {
        City city = City.Tallinn;
        VehicleType vehicleType = VehicleType.Car;
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.empty());
        try {
            deliveryService.getDeliveryFee(city, vehicleType, null);
            fail(); // if there wasn`t error, test didn`t pass
        } catch (ServiceError ex) {
            assertEquals("No weather data to calculate fee", ex.getMessage());
        }

        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static List<StationEntity> getStationsWithBadWeatherPhenomenon(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        for (String phenomenon : List.of("Glaze", "Hail", "Thunder")) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), 0f, 0f, phenomenon, new Date()));
        }
        return stationEntities;
    }

    private static Stream<Arguments> cityAndBadWeatherPhenomenonAndVehicleTypeArgsWithoutCar() {
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (VehicleType vehicleType : VehicleType.values()) {
                for (StationEntity station : getStationsWithBadWeatherPhenomenon(city)) {
                    if (!vehicleType.equals(VehicleType.Car)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getPhenomenon()));
                    }
                }
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, phenomenon={3}")
    @MethodSource("cityAndBadWeatherPhenomenonAndVehicleTypeArgsWithoutCar")
    void getDeliveryFee_ForbiddenWeatherPhenomenonForVehicleType_ThrowsServiceError(City city, VehicleType vehicleType,
                                                                                    StationEntity station, String phenomenon) {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        try {
            deliveryService.getDeliveryFee(city, vehicleType, null);
            fail(); // if there wasn`t error, test didn`t pass
        } catch (ServiceError ex) {
            assertEquals("Usage of selected vehicle type is forbidden", ex.getMessage());
        }

        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static List<StationEntity> getStationsWithBadWindSpeed(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        for (Float windSpeed : List.of(20.5f, 21f, 23f, 20.0001f, 30f)) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), 0f, windSpeed, null, new Date()));
        }
        return stationEntities;
    }

    private static Stream<Arguments> cityAndBadWindSpeedAndVehicleTypeBikeArgsWithoutCar() {
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (StationEntity station : getStationsWithBadWindSpeed(city)) {
                combinations.add(Arguments.of(city, VehicleType.Bike, station, station.getWindSpeed()));
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, wind speed={3}")
    @MethodSource("cityAndBadWindSpeedAndVehicleTypeBikeArgsWithoutCar")
    void getDeliveryFee_ForbiddenWindSpeedForVehicleType_ThrowsServiceError(City city, VehicleType vehicleType,
                                                                            StationEntity station, Float windSpeed) {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        try {
            deliveryService.getDeliveryFee(city, vehicleType, null);
            fail(); // if there wasn`t error, test didn`t pass
        } catch (ServiceError ex) {
            assertEquals("Usage of selected vehicle type is forbidden", ex.getMessage());
        }

        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static float getRandomFloatInRange(float from, float to) {
        return from + random.nextFloat() * (to - from);
    }

    private static List<StationEntity> getStationsWithGoodWeatherConditions(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), getRandomFloatInRange(0.1f, Float.MAX_VALUE),
                    getRandomFloatInRange(Float.MIN_VALUE, 9.9999999f), null, new Date()));
        }
        return stationEntities;
    }

    private static Stream<Arguments> cityAndVehicleTypeAndStationWithGoodWeatherConditions() {
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (VehicleType vehicleType : VehicleType.values()) {
                for (StationEntity station : getStationsWithGoodWeatherConditions(city)) {
                    combinations.add(Arguments.of(city, vehicleType, station, station.getAirTemperature(),
                            station.getWindSpeed(), city.getRBF(vehicleType)));
                }
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, air temperature={3}, wind speed={4}")
    @MethodSource("cityAndVehicleTypeAndStationWithGoodWeatherConditions")
    void getDeliveryFee_NoExtraFeesForWeatherConditions_OnlyBaseFee(City city, VehicleType vehicleType,
                                                                    StationEntity station,  float airTemperature,
                                                                    float windSpeed, double baseFee) throws ServiceError {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        var result = deliveryService.getDeliveryFee(city, vehicleType, null);

        assertEquals(baseFee, result);
        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static List<StationEntity> getStationsWithBadAirTemperatureBigAtef(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), -100f,
                0f, null, new Date()));
        for (int i = 0; i < 3; i++) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), getRandomFloatInRange(-100f, -10f),
                    0f, null, new Date()));
        }
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), -10.0001f,
                0f, null, new Date()));
        return stationEntities;
    }

    private static List<StationEntity> getStationsWithBadAirTemperatureMediumAtef(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), -10f,
                0f, null, new Date()));
        for (int i = 0; i < 3; i++) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), getRandomFloatInRange(-10f, 0f),
                    0f, null, new Date()));
        }
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), 0f,
                0f, null, new Date()));
        return stationEntities;
    }

    private static Stream<Arguments> cityAndVehicleTypeExcludedCarAndStationWithBadAirTemperature() {
        double bigATEF = 1d;
        double mediumATEF = 0.5d;
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (VehicleType vehicleType : VehicleType.values()) {
                if (!vehicleType.equals(VehicleType.Car)) {
                    for (StationEntity station : getStationsWithBadAirTemperatureBigAtef(city)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getAirTemperature(),
                                city.getRBF(vehicleType) + bigATEF));
                    }
                    for (StationEntity station : getStationsWithBadAirTemperatureMediumAtef(city)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getAirTemperature(),
                                city.getRBF(vehicleType) + mediumATEF));
                    }
                }
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, air temperature={3}")
    @MethodSource("cityAndVehicleTypeExcludedCarAndStationWithBadAirTemperature")
    void getDeliveryFee_BadAirTemperature_ExtraFeeAdded(City city, VehicleType vehicleType,
                                                        StationEntity station,  float airTemperature, double fee) throws ServiceError {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        var result = deliveryService.getDeliveryFee(city, vehicleType, null);

        assertEquals(fee, result);
        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static List<StationEntity> getStationsWithBadWindSpeedBigWSEF(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), 10f,
                10f, null, new Date()));
        for (int i = 0; i < 3; i++) {
            stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                    random.nextInt(), 10f,
                    getRandomFloatInRange(10f, 20f), null, new Date()));
        }
        stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), 10f,
                20f, null, new Date()));
        return stationEntities;
    }

    private static Stream<Arguments> cityAndVehicleTypeBikeAndStationWithBadWindSpeed() {
        double WSEF = 0.5d;
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (VehicleType vehicleType : VehicleType.values()) {
                if (vehicleType.equals(VehicleType.Bike)) {
                    for (StationEntity station : getStationsWithBadWindSpeedBigWSEF(city)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getWindSpeed(),
                                city.getRBF(vehicleType) + WSEF));
                    }
                }
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, wind speed={3}")
    @MethodSource("cityAndVehicleTypeBikeAndStationWithBadWindSpeed")
    void getDeliveryFee_BadWindSpeed_ExtraFeeAdded(City city, VehicleType vehicleType,
                                                        StationEntity station,  float windSpeed, double fee) throws ServiceError {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        var result = deliveryService.getDeliveryFee(city, vehicleType, null);

        assertEquals(fee, result);
        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }


    private static String randomizeCase(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray())
            sb.append(random.nextBoolean()
                    ? Character.toLowerCase(c)
                    : Character.toUpperCase(c));
        return sb.toString();
    }
    private static List<StationEntity> getStationsWithBadWeatherPhenomenonBigWPEF(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        for (String phenomenon : List.of("Drifting snow", "Blowing snow", "Heavy snowfall", "Moderate snowfall",
                "Light snowfall", "Heavy snow shower", "Moderate snow shower", "Light snow shower", "Light sleet",
                "Moderate sleet")) {
            for (int i = 0; i < 3; i++) {
                stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                        random.nextInt(), 10f,
                        0f, randomizeCase(phenomenon), new Date()));
            }
        }
        return stationEntities;
    }

    private static List<StationEntity> getStationsWithBadWeatherPhenomenonMediumWPEF(City city) {
        List<StationEntity> stationEntities = new ArrayList<>();
        for (String phenomenon : List.of("Light rain", "Moderate rain", "Light rain")) {
            for (int i = 0; i < 3; i++) {
                stationEntities.add(new StationEntity(random.nextInt(), city.toString(),
                        random.nextInt(), 10f,
                        0f, randomizeCase(phenomenon), new Date()));
            }
        }
        return stationEntities;
    }

    private static Stream<Arguments> cityAndVehicleTypeExcludedCarAndStationWithBadWeatherPhenomenon() {
        double bigWPEF = 1d;
        double mediumWPEF = 0.5d;
        List<Arguments> combinations = new ArrayList<>();
        for (City city : City.values()) {
            for (VehicleType vehicleType : VehicleType.values()) {
                if (!vehicleType.equals(VehicleType.Car)) {
                    for (StationEntity station : getStationsWithBadWeatherPhenomenonBigWPEF(city)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getPhenomenon(),
                                city.getRBF(vehicleType) + bigWPEF));
                    }
                    for (StationEntity station : getStationsWithBadWeatherPhenomenonMediumWPEF(city)) {
                        combinations.add(Arguments.of(city, vehicleType, station, station.getPhenomenon(),
                                city.getRBF(vehicleType) + mediumWPEF));
                    }
                }
            }
        }
        return combinations.stream();
    }
    @ParameterizedTest(name = "{index} city={0}, vehicle={1}, weather phenomenon={3}")
    @MethodSource("cityAndVehicleTypeExcludedCarAndStationWithBadWeatherPhenomenon")
    void getDeliveryFee_BadWeatherPhenomenon_ExtraFeeAdded(City city, VehicleType vehicleType,
                                                        StationEntity station, String phenomenon, double fee) throws ServiceError {
        given(stationRepository.findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city))
                .willReturn(Optional.of(station));

        var result = deliveryService.getDeliveryFee(city, vehicleType, null);

        assertEquals(fee, result);
        then(stationRepository).should().findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(city);
    }

    private static StationEntity getOldStationWithExtremeWeatherConditions(City city) {
        return new StationEntity(random.nextInt(), city.toString(),
                random.nextInt(), -100f,
                15f, "snow", new Date(10000L));
    }

    @Test
    void getDeliveryFee_PassDateArg_CalculateFeeBasedOnSpecificTime() throws ServiceError {
        City city = City.Tallinn;
        VehicleType vehicleType = VehicleType.Bike;
        StationEntity badWeather = getOldStationWithExtremeWeatherConditions(city);
        Date previousDate = new Date(1000000L);
        given(stationRepository.findFirstByNameContainingIgnoreCaseAndTimestampBeforeOrderByTimestampDesc(city, previousDate))
                .willReturn(Optional.of(badWeather));

        var result = deliveryService.getDeliveryFee(city, vehicleType, previousDate);

        assertEquals(3d + 1d + 0.5d + 1d, result);
        then(stationRepository).should().findFirstByNameContainingIgnoreCaseAndTimestampBeforeOrderByTimestampDesc(city, previousDate);
    }
}