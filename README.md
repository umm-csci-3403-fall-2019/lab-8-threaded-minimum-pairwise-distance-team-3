# Minimum pairwise distance

This is the starter code for the Minimum Pairwise Distance lab. The goal of this lab is to use threading 
to parallize an expensive (in this case O(N^2)) computation and see that this indeed yields performance gains.

In this lab there are two classes with a `main` method, `SerialMain` and `ThreadedMain`. Our classes are in a package so you'll have to include that when you make the call. It will look like this: 
 
```bash
time java mpd.SerialMain 1000 
```

Just replace `SerialMain` with `ThreadedMain` to run that one. The parameter can (and probably should) be modified to different values to compare the results. You'll get timing output something like

    real    0m9.325s
    user    0m27.580s
    sys     0m0.228s

The "real" entry is essentially the wall clock time the process took to complete. "user" is the amount of CPU time was spent running the code, and "sys" is time spent in system processes such as allocating memory or reading and writing files. On a single core system "real" is roughly the sum of "user" and "sys" (plus some noise). On a multi-core system, "user" can be bigger than real (as in the example above) because multiple threads running in parallel all add to the "user" value. So if you have two threads that each run for 10 seconds, the wall-clock time ("real") might be 12s, but the "user" time might be 23s.

## The problem

Given an array ```values``` of ```N``` integers, the task is to find the smallest absolute distance between 
any two elements, i.e., minimize `abs(values[i] - values[j])` over `0 ≤ i < j < N`. 

## The project structure

The ```test``` directory has three sets of tests:

* ```test/mpd/SerialMinimumPairwiseDistanceTest.java``` contains tests for the serial (non-threaded, non-parallel) solution. Since we provide a complete, working serial solution, all these tests should pass out of the box.
* ```test/mpd/ThreadedMinimumPairwiseDistanceTest.java``` contains tests for the threaded version. A key part of your job is to implement the threaded version so that these pass **Note:** There's no good way for the tests to know that you actually used threads correctly, or at all. The tests can check that you found the right answer, but not really how you got there. You could in fact just drop in our serial solution in and it would make these tests pass.
* ```test/mpd/ThreadTimingTest.java``` contains a timing test that attempts to address the weakness mentioned above. This test runs both the serial and the threaded versions on the same large-ish set of data, and compares the time required in each case. The assertion, then, is that the threaded version should be at least 3 times faster than the serial version. _This will only be true on a box with at least four cores._ On a single core box (if you can find one) the threaded version is likely to actually be a little slower, and on a dual core box the best speedup you can hope for would be something less than a factor of 2. Most of the machines in the lab now have at least 4 cores, however, so this should generally pass if you have your threading right. If this test is not passing, use a system monitor to determine how many cores your box has, and check the times printed out by the test. If your box only has 2 cores and your speedup is a factor of 1.5 or better, you're probably OK. If your box has 8 cores and your speedup is only 2.5 then you've probably got something wrong and should ask someone else to look it over with you.

The ```src``` directory:

* ```src/mpd/MinimumPairwiseDistance.java``` is a simple interface that specifies a single method ```long minimumPairwiseDistance(int[] values)``` that takes an array of ```int``` values and returns an ```long``` that should be the minimum distance (difference) between any two values in the array.
* ```src/mpd/SerialMinimumPairwiseDistance.java``` is a complete serial (non-threaded, non-parallel) solution to the problem. This works and you shouldn't need to change anything here. The loop structure, though, should be a useful hint for how you might want to structure the loops in your threaded solution.
* ```src/mpd/ThreadedMinimumPairwiseDistance.java``` is a stub for the threaded version. This is where your work will go; see below for hints on the structure of the solution.

## The simple serial solution

In its simple form this is an O(N^2) task because we must compare each of the N^2 pairs of values, or half of that if we take advantage of symmetry and only only check pairs ```(i, j)``` where ```i<j```. There are ways to speed this up in the specific case of just integers (e.g., sort the list in O(N log(N)) and then compute pairwise differences in O(N) time), but if we generalize the contents of the arrays and the notion of distance we can create versions where there's no way to improve on the basic O(N^2) approach, so we're going to stick with that 
for now.

The file ```src/mpd/SerialMinimumPairwiseDistance.java``` in the starter code includes a serial (non-threaded) O(N^2) solution:

```java
    public long minimumPairwiseDistance(int[] values) {
        long result = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            for (int j = 0; j < i; ++j) {
                // Gives us all the pairs (i, j) where 0 ≤ j < i < values.length
                long diff = Math.abs(values[i] - values[j]);
                if (diff < result) {
                    result = diff;
                }
            }
        }
        return result;
    }
```

Note that we initialize ```result``` with ```Integer.MAX_VALUE```, which is essentially Java's best effort at saying "positive infinity". Positive infinity is the identity of minimum (i.e., ```min(x, infinity) = x``` for all x), making this a useful starting value and a reasonable default value to return in the case that the 
array is empty.

## The threaded solution

The goal here is to implement a threaded solution that can run in parallel on hardware with multiple cores. The stub for this solution is in ```src/mpd/ThreadedMinimumPairwiseDistance.java``` and there are various 
failing JUnit tests waiting for you to implement this class.

The question with this and most parallel problems is how to divide the problem up into chunks that can be processed at least semi-independently. In this case there are N^2/2 pairs (i, j) that need to be processed, as illustrated in the gray triangular section below: 
![Diagram illustrating all pairs (i, j) such that 0 ≤ j < i < N](https://docs.google.com/drawings/d/1I8xiDTwlbkKTPaRdcMK7PWtZbfLFHTkDI7QZr_OUPlI/pub?w=960&h=720)
In this case I'm going to divide the set of pairs ```(i, j)``` such that ```0 ≤ j < i < N``` into four equal triangular regions as illustrated below: 
![Diagram illustrating region divided into four triangular sections](https://docs.google.com/drawings/d/12hyDoIqfpP2DTl5Uk97gcbIf94sABKHG4LTagZKd0nk/pub?w=960&h=720)
Formally the four sections can be described by the following ranges, each of which can be turned into a pair of nested loops similar to the pair of loops in the serial solution above:

* Lower left: ```0 ≤ j < i < N/2```
* Bottom right: ```N/2 ≤ j + N/2 < i < N```
* Top right: ```N/2 ≤ j < i < N```
* Center: ```N/2 ≤ i ≤ j + N/2 < N```

Our strategy, then, is:

* Create a thread for each of these sections.
* Have each thread find the minimum for that section.
* Have each thread update a global minimum when it finishes.
* Wait for each thread to finish.
* Report the resulting global minimum.

There are a bunch of ways one could do this, but I created an (inner) class for each of the four sections (using creative names like ```LowerLeft```). Each of these implemented ```Runnable``` and their ```run()``` method was very similar to the nested loops in the serial solution above, but with the bounds on the loops changed to cover just the pairs assigned to that section. When the nested loops finish, I call a ```updateGlobalResult(localResult)``` method in ```ThreadedMinimumPairwiseDistance``` that compares
```localResult``` to ```globalResult``` updating ```globalResult``` to be ```localResult``` if ```localResult``` is smaller than the previous ```globalResult```.

While creating four inner classes is arguably kind of gross, having them as _inner_ classes does simplify a few things because we can directly access fields (like the ```values``` array) and methods (like ```updateGlobalResult()```) in the containing class. This greatly simplifies the problems of communicating between the ```minimumPairwiseDistance()``` method that starts things off (and which has the array of values and needs to know the global minimum) and the various threads. You can make all this work without the inner classes, but you'll need to find other ways to pass the information around. _If you've never really used inner classes and aren't really sure what we're talking about here, definitely ask._

Make sure to wait for all the threads to finish (I'd use `join()`) before returning the global minimum. Remember that `.start()` will return _immediately_ even if the actual run method has a ton of complex work to do, so after you start them all, you have to wait for them all to finish before you can continue.

You also need to remember to synchronize the `updateGlobalResult()` method so that two threads can't collide and mess each other up with they try to call that method at the same time.
