package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import pcd.ass03.ex1.actors.msg.*;
import pcd.ass03.ex1.domain.Board;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardUpdater extends AbstractLoggingActor {
    private final int numberOfWorkers;
    private List<ActorRef> workers;
    private int finishUpdate;

    public BoardUpdater(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public static Props props(int numberOfWorkers) {
        return Props.create(BoardUpdater.class, numberOfWorkers);
    }

    @Override
    public void preStart() throws Exception {
        workers = IntStream.range(0,numberOfWorkers).mapToObj(i -> getContext().actorOf(Props.create(Worker.class), "Worker" + i)).collect(Collectors.toList());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMsg.class, msg -> {
            Board oldBoard = msg.getBoard();
            // Create the new board
            final Board newBoard = Board.board(oldBoard.getWidth(), oldBoard.getHeight());
            // Prepare workers
            log().debug("Start update");
            prepareWorkers(oldBoard, newBoard);

            getContext().become(receiveBuilder().match(FinishedUpdateMsg.class, finishMsg -> {
                this.finishUpdate++;
                if (this.finishUpdate == this.numberOfWorkers) {
                    this.finishUpdate = 0;
                    getSender().tell(new NewBoardMsg(newBoard), getSelf());
                    getContext().unbecome();
                }
            }).build(), false);
        }).match(StopMsg.class, msg -> {
            System.out.println("Stopped");
            for (ActorRef w : workers) {
                w.tell(msg, getSelf());
            }
            context().stop(getSelf());
        }).build();
    }

    private void prepareWorkers(final Board oldBoard, final Board newBoard) {
        final int height = oldBoard.getHeight();
        final int offset = height / workers.size();
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.size(); i++) {
            workers.get(i).tell(new StartUpdateMsg(fromRow, toRow, oldBoard, newBoard), getSelf());
            fromRow += offset;
            toRow += offset;
        }
        workers.get(0).tell(new StartUpdateMsg(fromRow, fromRow + (height - fromRow), oldBoard, newBoard), getSelf());
    }
}
