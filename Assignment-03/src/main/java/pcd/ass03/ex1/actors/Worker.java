package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import pcd.ass03.ex1.actors.msg.StartPartialUpdate;
import pcd.ass03.ex1.actors.msg.StartUpdate;
import pcd.ass03.ex1.actors.msg.Stop;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.domain.CellUtils;

import static pcd.ass03.ex1.actors.msg.FinishedUpdate.FinishedUpdate;

public class Worker extends AbstractLoggingActor {

    private final int numberOfPartitions;

    public Worker(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public static Props props(int numberOfPartitions) {
        return Props.create(Worker.class, numberOfPartitions);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartUpdate.class, msg -> {
            int from = msg.getFromRow();
            int maxTo = msg.getToRow();
            int rowForPartialWorker = (maxTo - from) / numberOfPartitions;

            final ActorRef sender = getSender();

            getSelf().tell(new StartPartialUpdate(from, from + rowForPartialWorker, msg.getOldBoard(), msg.getNewBoard(), maxTo), getSender());

            getContext().become(receiveBuilder().match(StartPartialUpdate.class, partial -> {
                if ((rowForPartialWorker + partial.getToRow()) >= maxTo) {
                    this.updateBoard(partial.getFromRow(), maxTo, partial.getOldBoard(), partial.getNewBoard());
                    sender.tell(FinishedUpdate, getSelf());
                    getContext().unbecome();
                } else {
                    this.updateBoard(partial.getFromRow(), partial.getToRow(), partial.getOldBoard(), partial.getNewBoard());
                    getSelf().tell(new StartPartialUpdate(partial.getToRow(), rowForPartialWorker + partial.getToRow(), msg.getOldBoard(), msg.getNewBoard(), maxTo), getSender());
                }
            }).build(), false);

        }).match(Stop.class, msg -> {
            log().info(getSelf().path().name() + " stopped");
            context().stop(getSelf());
        }).build();
    }

    private void updateBoard(int fromRow, int toRow, Board oldBoard, Board newBoard) {
        // Update the given portion of the board
        log().info(getSelf().path().name() + " from row {} to row {} started", fromRow, toRow);
        for (int x = fromRow; x < toRow; x++) {
            for (int y = 0; y < oldBoard.getWidth(); y++) {
                newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
            }
        }
        log().info(getSelf().path().name() + "Worker from row {} to row {} finished", fromRow, toRow);
    }

}
