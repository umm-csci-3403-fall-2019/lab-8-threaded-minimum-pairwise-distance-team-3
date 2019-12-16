package mpd;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedMinimumPairwiseDistance implements MinimumPairwiseDistance {

    @Override
    public long minimumPairwiseDistance(int[] values) {

        if (values.length == 0)
            return Integer.MAX_VALUE;

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Future<Long>[] futures = new Future[4];

        int half = (values.length / 2);

        // lower left
        futures[0] = executor.submit(() -> {
            long result = Integer.MAX_VALUE;
            for (int i = 0; i < half; ++i) {
                for (int j = 0; j < i; ++j) {
                    long diff = Math.abs(values[i] - values[j]);
                    if (diff < result) {
                        result = diff;
                    }
                }
            }
            return result;
        });

        // top right
        futures[1] = executor.submit(() -> {
            long result = Integer.MAX_VALUE;
            for (int i = half; i < values.length; ++i) {
                for (int j = half; j < i; ++j) {
                    long diff = Math.abs(values[i] - values[j]);
                    if (diff < result) {
                        result = diff;
                    }
                }
            }
            return result;
        });

        // bottom right
        futures[2] = executor.submit(() -> {
            long result = Integer.MAX_VALUE;
            for (int i = half; i < values.length; ++i) {
                for (int j = 0; (j + half) < i; ++j) {
                    long diff = Math.abs(values[i] - values[j]);
                    if (diff < result) {
                        result = diff;
                    }
                }
            }
            return result;
        });

        // center
        futures[3] = executor.submit(() -> {
            long result = Integer.MAX_VALUE;
            for (int j = 0; (j + half) < values.length; ++j) {
                for (int i = half; i < (j + half); ++i) {
                    long diff = Math.abs(values[i] - values[j]);
                    if (diff < result) {
                        result = diff;
                    }
                }
            }
            return result;
        });


        // Go through all the futures and gets their values, then gets the min of those.
        long result = Arrays.stream(futures).mapToLong(x -> {
            try {
                return x.get(); // gets the value of the future (waits if necessary)
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        }).min().getAsLong();

        executor.shutdown(); 

        return result;
    }

}
