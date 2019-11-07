package mpd;

import java.util.Random;

public class SerialMain {

    public static void main(String[] args) {
        int numValues = Integer.parseInt(args[0]);
        MinimumPairwiseDistance mpd = new SerialMinimumPairwiseDistance();

        Random random = new Random();
        int[] values = new int[numValues];
        for (int i = 0; i < numValues; ++i) {
            values[i] = random.nextInt();
        }
        
        long result = mpd.minimumPairwiseDistance(values);
        System.out.println("The minimum distance was " + result);
    }

}
