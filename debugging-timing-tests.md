# Debugging the timing tests

Some groups have had trouble with the timing tests on this lab.
Writing automated (JUnit) tests for timing that that is tricky,
but the actual behavior is going to depend on the specific machine
you're using and what else is running on that machine when you
run your test.

That said, you should see a non-trivial speed-up if you've got things
written correctly, and I would expect that test to generally pass under
most circumstances.

If you think your code is right and are worried that you're failing
tests because you're on an old, sad computer (which is a possibility),
you might try running the tests on a faster box. One group was having problems
with timing tests failing on an old box, but their code looked good. When we
ran their tests on a newer, faster box then the timing tests passed nearly every time.

If it's not passing for you then you do probably want to try to figure out why. Working with several groups that were having trouble with that test, the issue was pretty much always that the bounds on the loops were wrong in one or more of the 4 "worker" classes. In some cases this led to one of the threads doing a whole square instead of just the triangle it was supposed to cover, doubling the time required by that thread. In more dramatic cases people had one of their worker classes covering multiple squares, making them slower still.

Since you `join` all the threads, your total time is driven by the slowest thread, so any thread that is doing more work than necessary will end up determining the overall behavior of the system. And that can cause the timing tests to fail, and debugging these problems can be tricky. Some things that might help:

- Perform a very careful code review on your bounds. Use CSci 1302-style rules to manipulate/simplify the bounds in the write-up so it's easier to see what the bounds should be in the code.
- Try some particular values and see if they make sense. Look at the minimum and maximum values for both loops and see if that generates endpoints on the correct triangle.

Another potentially useful source of information is to have the "System Monitor" application open while you're running the timing test, with the "Resources" tab selected. That should give you a set of colored graphs showing how busy the cores are, how much memory you're using, and network traffic.

When you run your timing tests, you should see one core go to 100% while it first runs the sequential version, and then you should see four cores go to 100% when the parallel version happens. If you don't see four cores get busy, then that's a sign that something's wrong. You would also expect all four cores to "settle down" at about the same time if each thread is covering the same size region (which they should if you have the bounds right). One group had two of their cores dropping after a certain amount of time, say _t_, had passed. A third then dropped after about _2t_, suggesting that its bounds were wrong and that it was doing about twice as much work as the first two. The fourth didn't drop until what looked like _3t_ had passed, indicating that another worker class was doing 3 times the amount of work it should have been.

Manipulating the set of worker classes then helped us figure out which of those classes had the incorrect bounds. With some commenting and copy/paste, for example, we created two worker threads doing bottom-left and none doing top-right. That changed things so that three of the cores finished at the same time, and the one that was taking twice as long disappeared, suggesting that the top-right was the one that was doing twice as much work.

Trying to debug with the system monitor graphs is a bit like reading tea leaves, though, and ultimately you'll need to focus on the loop bounds anyway to actually fix anything, so you may just want to focus on the bounds. Perhaps counter-intuitively, the system monitor is perhaps most useful on a slower machine with just four cores. If you run it on a faster machine and/or a machine with 8 cores there's more noise in the graphs and it can be harder to figure out what's up.

Hope this helps!