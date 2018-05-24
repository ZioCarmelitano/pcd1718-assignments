package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import pcd.ass03.ex1.actors.msg.NewBoardMsg;
import pcd.ass03.ex1.actors.msg.StartMsg;
import pcd.ass03.ex1.actors.msg.StopMsg;

public class BenchmarkActor extends AbstractLoggingActor {

    private final int numberOfWorkers;
    private ActorRef boardUpdater;

    public BenchmarkActor(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public static Props props(int numberOfWorkers) {
        return Props.create(BenchmarkActor.class, numberOfWorkers);
    }

    @Override
    public void preStart() throws Exception {
        boardUpdater = getContext().actorOf(BoardUpdater.props(numberOfWorkers), "boardUpdater");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StopMsg.class, stopMsg -> {
            log().info(getSelf().path().name() + " StopMsg");
            boardUpdater.tell(new StopMsg(), getSelf());
            context().stop(getSelf());
        }).match(StartMsg.class, startMsg -> {
            log().info(getSelf().path().name() + " StartMsg");
            this.boardUpdater.tell(startMsg, getSelf());
        }).match(NewBoardMsg.class, newBoardMsg -> {
            log().info(getSelf().path().name() + " newBoardMsg");
            getSelf().tell(new StopMsg(), getSelf());
        }).build();
    }

}
