import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class main {
    private static void generateFileData() {
        int K = 500;
        int R = 200000;
        Random random = new Random();
        try {
            FileWriter writer = new FileWriter("example.txt");
            for (int i = 0; i < R; i++) {
                int call = random.nextInt(K) + 1;
                int rec = random.nextInt(K - 1) + 1;
                if (rec >= call) {
                    rec++;
                }
                int time = (int) (Math.abs(random.nextGaussian()) * 120 + 90);  // Average: 120s -- Standard deviation: 90s
                writer.write(call + " " + rec + " " + time + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) {
        generateFileData();

        Map<Integer, CallerStats> callers = new HashMap<>();
        Map<Integer, CallerStats> receivers = new HashMap<>();
        Map<String, Integer> uniqueCallersReceivers = new HashMap<>();
        Map<String, Integer> uniqueReceiversCallers = new HashMap<>();

        try {
            File file = new File("example.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(" ");

                int callerId = Integer.parseInt(data[0]);
                int receiverId = Integer.parseInt(data[1]);
                int callDuration = Integer.parseInt(data[2]);

                // Creates a variable that stores the calling statistics for the specified callerID: returns if it exists or if it does not exist, it creates a new CallerStats object and returns it
                CallerStats callerStats = callers.getOrDefault(callerId, new CallerStats());
                callerStats.calls++;
                callerStats.duration += callDuration;
                callers.put(callerId, callerStats);

                CallerStats receiverStats = receivers.getOrDefault(receiverId, new CallerStats());
                receiverStats.calls++;
                receiverStats.duration += callDuration;
                receivers.put(receiverId, receiverStats);

                String callerReceiverPair = callerId + "_" + receiverId;
                String receiverCallerPair = receiverId + "_" + callerId;

                uniqueCallersReceivers.put(callerReceiverPair, uniqueCallersReceivers.getOrDefault(callerReceiverPair, 0) + 1);
                uniqueReceiversCallers.put(receiverCallerPair, uniqueReceiversCallers.getOrDefault(receiverCallerPair, 0) + 1);
            }

            scanner.close();

            System.out.println("1. List of customers who \"talked\" the most time as a caller (and this time).");
            System.out.println("2. List of customers who \"talked\" the most time as a receiver (and this time).");
            System.out.println("3. List of customers who called the most other customers.");
            System.out.println("4. List of customers who have received calls from the largest number of other customers.");
            System.out.println("5. List of customers who called most often.");
            System.out.println("6. List of customers who have received the largest number of calls.");
            System.out.println("7. List of customers who called the least.");
            System.out.println("8. List of customers who answered the fewest calls.");
            System.out.println("9. Information about specific client.");

            System.out.println();

            System.out.println("Choose option:");
            int choice = new Scanner(System.in).nextInt();

            int N = 0;
            if (choice != 9) {
                System.out.println("How much records do you wanna see?");
                N = new Scanner(System.in).nextInt();
            }
            System.out.println();

            switch(choice) {
                case 1 -> {
                    List<Map.Entry<Integer, CallerStats>> sortedCallers = callers.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(c -> -c.duration)))
                            .limit(N).toList();
                    System.out.println("Top " + N + " callers:");
                    for (Map.Entry<Integer, CallerStats> entry : sortedCallers) {
                        System.out.println("ID: " + entry.getKey() + "\t| Time: " + entry.getValue().duration + " seconds");
                    }
                }

                case 2 -> {
                    List<Map.Entry<Integer, CallerStats>> sortedReceivers = receivers.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(c -> -c.duration)))
                            .limit(N).toList();
                    System.out.println("Top " + N + " receivers:");
                    for (Map.Entry<Integer, CallerStats> entry : sortedReceivers) {
                        System.out.println("ID: " + entry.getKey() + "\t| Time: " + entry.getValue().duration + " seconds");
                    }
                }

                case 3 -> {
                    System.out.println("Top " + N + " clients who called the most unique numbers:");
                    Map<Integer, Integer> numUniqueCallers = new HashMap<>();
                    uniqueCallersReceivers.forEach((k, v) -> {
                        String[] pair = k.split("_");
                        int caller = Integer.parseInt(pair[0]);
                        numUniqueCallers.put(caller, numUniqueCallers.getOrDefault(caller, 0) + 1);
                    });
                    numUniqueCallers.entrySet().stream()
                            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                            .limit(N)
                            .forEach(entry -> {
                                int callerId = entry.getKey();
                                int numReceivers = entry.getValue();
                                System.out.println("ID: " + callerId + "\t| Unique calls: " + numReceivers);
                            });
                }

                case 4 -> {
                    System.out.println("Top " + N + " receivers who received calls from the most unique numbers:");
                    Map<Integer, Integer> numUniqueReceivers = new HashMap<>();
                    uniqueReceiversCallers.forEach((k, v) -> {
                        String[] pair = k.split("_");
                        int receiver = Integer.parseInt(pair[0]);
                        numUniqueReceivers.put(receiver, numUniqueReceivers.getOrDefault(receiver, 0) + 1);
                    });
                    numUniqueReceivers.entrySet().stream()
                            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                            .limit(N)
                            .forEach(entry -> {
                                int receiverId = entry.getKey();
                                int numCallers = entry.getValue();
                                System.out.println("ID: " + receiverId + "\t| Unique calls: " + numCallers);
                            });
                }

                case 5 -> {
                    System.out.println("Top " + N + " clients who made the most calls:");
                    callers.entrySet().stream()
                            .sorted(Map.Entry.<Integer, CallerStats>comparingByValue(Comparator.comparingInt(cs -> cs.calls)).reversed())
                            .limit(N)
                            .forEach(entry -> System.out.println("ID: " + entry.getKey() + "\t| Calls: " + entry.getValue().calls + " calls"));
                }

                case 6 -> {
                    System.out.println("Top " + N + " clients who received the most calls:");
                    receivers.entrySet().stream()
                            .sorted(Map.Entry.<Integer, CallerStats>comparingByValue(Comparator.comparingInt(cs -> cs.calls)).reversed())
                            .limit(N)
                            .forEach(entry -> System.out.println("ID: " + entry.getKey() + "\t| Calls: " + entry.getValue().calls + " calls"));
                }

                case 7 -> {
                    System.out.println("Top " + N + " clients who made the least calls:");
                    callers.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(cs -> cs.calls)))
                            .limit(N)
                            .forEach(entry -> System.out.println("ID: " + entry.getKey() + "\t| Calls: " + entry.getValue().calls + " calls"));
                }

                case 8 -> {
                    System.out.println("Top " + N + " clients who received the least calls:");
                    receivers.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(cs -> cs.calls)))
                            .limit(N)
                            .forEach(entry -> System.out.println("ID: " + entry.getKey() + "\t| Calls: " + entry.getValue().calls + " calls"));
                }

                case 9 -> {
                    System.out.println("Choose client ID to see information: ");
                    int k = new Scanner(System.in).nextInt();
                    System.out.println();
                    CallerStats callerStats = callers.getOrDefault(k, new CallerStats());
                    CallerStats receiverStats = receivers.getOrDefault(k, new CallerStats());
                    int totalDuration = callerStats.duration + receiverStats.duration;
                    int totalCallsMade = callerStats.calls;
                    int totalCallsReceived = receiverStats.calls;
                    System.out.println("Client ID: " + k);
                    System.out.println("Calls made: " + totalCallsMade);
                    System.out.println("Calls received: " + totalCallsReceived);
                    System.out.println("Total time: " + totalDuration + " seconds");
                }

            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }
}

class CallerStats {
    int calls;
    int duration;
}