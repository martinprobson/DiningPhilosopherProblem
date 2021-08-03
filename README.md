# The Dining Philosophers Problem
The [Dining Philosophers Problem](https://en.wikipedia.org/wiki/Dining_philosophers_problem) is a classic problem used
in concurrent algorithm design to illustrate synchronization issues and techniques to resolve
them.

This repo contains a Scala implementation of the problem for learning purposes.

## Overview
We have two classes, one representing the Forks and the other the Philosophers. 

The main program loop is in the `Philosopher.process` method.
This method also contains the important code section that implements the [Resource Hierarchy solution](https://en.wikipedia.org/w/index.php?title=Dining_philosophers_problem&section=4): -

```scala
    val (fork1, fork2) =
    if (leftFork.number < rightFork.number) (leftFork, rightFork)
    else (rightFork, leftFork)
```

## ToDo

- [ ] Rewrite using functional programming techniques.
- [ ] Implement the solution using Actors.
