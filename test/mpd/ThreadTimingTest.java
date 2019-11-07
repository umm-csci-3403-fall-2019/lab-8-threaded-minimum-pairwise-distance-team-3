package mpd;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class ThreadTimingTest {

    public static final int NUM_VALUES = 100000;

    @Test
    public void testSortingTiming() {
        Random random = new Random();
        MinimumPairwiseDistance serialMPD = new SerialMinimumPairwiseDistance();
        int[] serialValues = new int[NUM_VALUES];
        MinimumPairwiseDistance threadedMPD = new ThreadedMinimumPairwiseDistance();
        int[] threadedValues = new int[NUM_VALUES];
        for (int i = 0; i < NUM_VALUES; ++i) {
            serialValues[i] = random.nextInt();
            threadedValues[i] = serialValues[i];
        }

        long startTime = System.currentTimeMillis();
        serialMPD.minimumPairwiseDistance(serialValues);
        long endTime = System.currentTimeMillis();
        long serialTime = endTime - startTime;

        startTime = System.currentTimeMillis();
        threadedMPD.minimumPairwiseDistance(threadedValues);
        endTime = System.currentTimeMillis();
        long threadedTime = endTime - startTime;

        System.out.println("The serial time was " + serialTime + "ms");
        System.out.println("The threaded time was " + threadedTime + "ms");

        assertArrayEquals(serialValues, threadedValues);
        // If you only have two cores, you're unlikely to see a 3-fold
        // improvement in performance and
        // the best you can hope for is something between less than a 2-fold
        // improvement. If you have
        // 4+ cores, however, a 3-fold improvement isn't at all implausible.
        // Most of the machines in
        // the lab are now 4+ cores, but there are still a few old ones that are
        // just dual core
        // boxes.
        //
        // If this test fails, then look at the serial time and the threaded
        // time and confirm that
        // the threaded time is in fact better than the serial time; if it's not
        // then you problem have
        // an issue with your threading. If the threaded time is better, but not
        // 3-fold better, then
        // pull up the system monitor on that box and see how many cores you
        // have. If it's only 2 then
        // either change the improvement factor from 3 to something like 1.5, or
        // try a different box
        // with more cores.
        assertTrue(
                "If this fails, see if the serial time is at least somewhat larger than the threaded time",
                serialTime / 3 > threadedTime);
    }

}
