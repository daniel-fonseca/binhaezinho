import java.io.*;
import java.util.*;

class SumThread implements Runnable {
    private Map<String, List<Long>> fileFingerprints;
    private String path;

    public SumThread(Map<String, List<Long>> fileFingerprints, String path) {
        this.fileFingerprints = fileFingerprints;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            List<Long> fingerprint = fileSum(path);
            fileFingerprints.put(path, fingerprint);
        } catch (Exception ex) {
        }
    }

    private List<Long> fileSum(String filePath) throws IOException {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
            }
        }
        return chunks;
    }

    private long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }
}

public class FileSimilarity {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();

        for (String path : args) {
            List<Long> fingerprint = new ArrayList<>();
            fileFingerprints.put(path, fingerprint);
        }

        Thread[] threads = new Thread[args.length];

        // Calculate the fingerprint for each file
        for (int i = 0; i < args.length; i++) {
            Thread thread = new Thread(new SumThread(fileFingerprints, args[i]));
            threads[i] = thread;
            thread.start();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                float similarityScore = similarity(fingerprint1, fingerprint2);
                System.out.println(
                        "Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
            }
        }
    }

    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }

        return (float) counter / base.size();
    }
}
