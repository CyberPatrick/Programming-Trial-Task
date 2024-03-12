package ee.taltech.food.forms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum City {
    Tallinn(4, 3.5, 3),
    Tartu(3.5, 3, 2.5),
    Pärnu(3, 2.5, 2);

    private final double feeForCar;
    private final double feeForScooter;
    private final double feeForBike;

    /**
     * Get regional base fee (RBF) based on vehicle type.
     * @param vehicleType Transport used for delivery.
     * @return
     */
    public double getRBF(VehicleType vehicleType) {
        switch (vehicleType) {
            case Car:
                return feeForCar;
            case Scooter:
                return feeForScooter;
            case Bike:
                return feeForBike;
            default:
                throw new IllegalArgumentException("Unknown vehicle type");
        }
    }
}
