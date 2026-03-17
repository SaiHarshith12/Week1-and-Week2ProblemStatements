import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    Status status;

    ParkingSpot() {
        this.status = Status.EMPTY;
    }
}

enum Status {
    EMPTY, OCCUPIED, DELETED
}

class ParkingSystem {

    private final int SIZE = 500;
    private ParkingSpot[] table = new ParkingSpot[SIZE];

    private int occupiedSpots = 0;
    private int totalProbes = 0;
    private int totalRequests = 0;

    // Track peak hours
    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public ParkingSystem() {
        for (int i = 0; i < SIZE; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // ------------------ HASH FUNCTION ------------------
    private int hash(String plate) {
        return Math.abs(plate.hashCode()) % SIZE;
    }

    // ------------------ PARK VEHICLE ------------------
    public void parkVehicle(String plate) {
        int index = hash(plate);
        int probes = 0;

        for (int i = 0; i < SIZE; i++) {
            int pos = (index + i) % SIZE;
            probes++;

            if (table[pos].status == Status.EMPTY || table[pos].status == Status.DELETED) {
                table[pos].licensePlate = plate;
                table[pos].entryTime = System.currentTimeMillis();
                table[pos].status = Status.OCCUPIED;

                occupiedSpots++;
                totalProbes += probes;
                totalRequests++;

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

                System.out.println("parkVehicle(\"" + plate + "\") → Assigned spot #"
                        + pos + " (" + (probes - 1) + " probes)");
                return;
            }
        }

        System.out.println("Parking Full!");
    }

    // ------------------ EXIT VEHICLE ------------------
    public void exitVehicle(String plate) {
        int index = hash(plate);

        for (int i = 0; i < SIZE; i++) {
            int pos = (index + i) % SIZE;

            if (table[pos].status == Status.EMPTY) break;

            if (table[pos].status == Status.OCCUPIED &&
                    table[pos].licensePlate.equals(plate)) {

                long durationMs = System.currentTimeMillis() - table[pos].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);

                double fee = calculateFee(hours);

                table[pos].status = Status.DELETED;
                occupiedSpots--;

                System.out.printf("exitVehicle(\"%s\") → Spot #%d freed, Duration: %.2fh, Fee: $%.2f\n",
                        plate, pos, hours, fee);
                return;
            }
        }

        System.out.println("Vehicle not found!");
    }

    // ------------------ FEE CALCULATION ------------------
    private double calculateFee(double hours) {
        return Math.ceil(hours) * 5; // $5 per hour
    }

    // ------------------ FIND NEAREST SPOT ------------------
    public void findNearestAvailableSpot() {
        for (int i = 0; i < SIZE; i++) {
            if (table[i].status == Status.EMPTY) {
                System.out.println("Nearest available spot: #" + i);
                return;
            }
        }
        System.out.println("No available spots!");
    }

    // ------------------ STATISTICS ------------------
    public void getStatistics() {
        double occupancy = (occupiedSpots * 100.0) / SIZE;
        double avgProbes = totalRequests == 0 ? 0 : (double) totalProbes / totalRequests;

        int peakHour = -1, maxTraffic = 0;
        for (int hour : hourlyTraffic.keySet()) {
            if (hourlyTraffic.get(hour) > maxTraffic) {
                maxTraffic = hourlyTraffic.get(hour);
                peakHour = hour;
            }
        }

        System.out.printf("Occupancy: %.2f%%, Avg Probes: %.2f, Peak Hour: %d:00-%d:00\n",
                occupancy, avgProbes, peakHour, peakHour + 1);
    }
}

// ------------------ MAIN ------------------
public class ParkingLotManagement {
    public static void main(String[] args) throws InterruptedException {

        ParkingSystem ps = new ParkingSystem();

        ps.parkVehicle("ABC-1234");
        ps.parkVehicle("ABC-1235");
        ps.parkVehicle("XYZ-9999");

        Thread.sleep(2000); // simulate time

        ps.exitVehicle("ABC-1234");

        ps.findNearestAvailableSpot();

        ps.getStatistics();
    }
}
