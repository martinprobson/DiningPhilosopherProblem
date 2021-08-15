# The Dining Philosophers Problem
The [Dining Philosophers](https://en.wikipedia.org/wiki/Dining_philosophers_problem) is a classic problem used
in concurrent algorithm design to illustrate synchronization issues and techniques to resolve
them.

This repo contains two Scala implementations of the solution for learning purposes.
1. Using classic Threads with the [Resource Hierarchy solution](https://en.wikipedia.org/w/index.php?title=Dining_philosophers_problem&section=4) 
2. Using [Actors](https://doc.akka.io/docs/akka/current/general/actors.html).

## Classic Threads with Resource Hierarchy solution

### Overview
We have two classes, one representing the Forks and the other the Philosophers. 

The main program loop is in the `net.martinprobson.diningphilosopher.resourcehierarchy.Philosopher.process` method.
This method also contains the important code section that implements the [Resource Hierarchy solution](https://en.wikipedia.org/w/index.php?title=Dining_philosophers_problem&section=4): -

```scala
    val (fork1, fork2) =
    if (leftFork.number < rightFork.number) (leftFork, rightFork)
    else (rightFork, leftFork)
```

### Problems
* Threads are essentially blocked until a Fork resource is available.
* There are potential race conditions all over the code as we are mutating state from multiple Threads.

## The Actor Model

### Overview
This is based on the solution outlined [here](http://www.dalnefre.com/wp/2010/08/dining-philosophers-in-humus/) written using [Akka Actors](https://doc.akka.io/docs/akka/current/typed/actors.html).
There are two main actor classes [Philosopher](src/main/scala/net/martinprobson/diningphilosopher/actor/Philosopher.scala) and 
[Fork](src/main/scala/net/martinprobson/diningphilosopher/actor/Fork.scala) and they act as a finite state machine with each
state represented as a behavior and each behavior handled by a method.

#### Philosopher Actor

| Current State | Input             | Next State   | Description                                                                                                                                                                                    |
|---------------|-------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| think         | eat               | hungry       | Start requesting forks before eating                                                                                                                                                           |
| hungry        | taken(left fork)  | rightWaiting | Obtained a left fork, start waiting for right fork.                                                                                                                                            |
| hungry        | taken(right fork) | leftWaiting  | Obtained a right fork, start waiting for left fork.                                                                                                                                            |
| hungry        | busy              | denied       | Fork(s) are in use, cannot eat right now.                                                                                                                                                      |
| denied        | taken             | think        | Put the taken fork back, and go back to thinking as we cannot obtain both forks right now. Send an eat message to self so we can try eating again soon.                                        |
| denied        | busy              | think        | Go back to thinking as we cannot obtain forks, send an eat message to self so we can try eating again soon.                                                                                    |
| leftWaiting   | taken(left fork)  | eat          | We have a right fork and are waiting for a left fork, we have obtained the left fork, so start eating.                                                                                         |
| leftWaiting   | busy              | think        | We have a right fork and are waiting for a left fork, the left fork is busy, so put the right fork back and go back to thinking. Send an eat message to self so we can try eating again soon.  |
| rightWaiting  | taken(right fork) | eat          | We have a left fork and are waiting for a right fork, we have obtained the right fork, so start eating.                                                                                        |
| rightWaiting  | busy              | think        | We have a left fork and are waiting for a right fork, the right fork is busy, so put the left fork back and go back to thinking. Send an eat message to self so we can try eating again soon.  |
| eat           | think             | think        | Start eating. Timer actor is used to send a think message when eating time is up.                                                                                                              |


#### Fork Actor


| Current State | Input | Next State | Description                                                                                   |
|---------------|-------|------------|-----------------------------------------------------------------------------------------------|
| busy          | take  | busy       | We have received a take message, but are already taken, signal the requester that we are busy |
| busy          | put   | idle       | We have received a put message, therefore we can go to an idle state.                         |
| idle          | take  | busy       | We have received a take message, signal the requester we are taken and go to busy state       |
| idle          | put   | idle       | We have received an put message and are already idle (should not happen?)                     |

In addition to the two main actors above, there is also a [Timer](src/main/scala/net/martinprobson/diningphilosopher/actor/Timer.scala) 
actor to control timeouts for eating and thinking and a [Controller](src/main/scala/net/martinprobson/diningphilosopher/actor/Controller.scala) 
actor that starts and stops the actor system.
