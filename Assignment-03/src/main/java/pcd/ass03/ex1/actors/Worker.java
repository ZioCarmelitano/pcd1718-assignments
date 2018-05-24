package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import pcd.ass03.ex1.actors.msg.FinishedUpdateMsg;
import pcd.ass03.ex1.actors.msg.StartPartialUpdateMsg;
import pcd.ass03.ex1.actors.msg.StartUpdateMsg;
import pcd.ass03.ex1.actors.msg.StopMsg;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.domain.CellUtils;

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
        return receiveBuilder().match(StartUpdateMsg.class, msg -> {
            int from = msg.getFromRow();
            int maxTo = msg.getToRow();
            int rowForPartialWorker = (maxTo - from) / numberOfPartitions;

            getSelf().tell(new StartPartialUpdateMsg(from, from + rowForPartialWorker, msg.getOldBoard(), msg.getNewBoard(), maxTo), getSender());

            getContext().become(receiveBuilder().match(StartPartialUpdateMsg.class, partialMsg -> {
                if ((rowForPartialWorker + partialMsg.getToRow()) >= maxTo) {
                    this.updateBoard(partialMsg.getFromRow(), maxTo, partialMsg.getOldBoard(), partialMsg.getNewBoard());
                    getSender().tell(new FinishedUpdateMsg(), getSelf());
                    getContext().unbecome();
                } else {
                    this.updateBoard(partialMsg.getFromRow(), partialMsg.getToRow(), partialMsg.getOldBoard(), partialMsg.getNewBoard());
                    getSelf().tell(new StartPartialUpdateMsg(partialMsg.getToRow(), rowForPartialWorker + partialMsg.getToRow(), msg.getOldBoard(), msg.getNewBoard(), maxTo), getSender());
                }
            }).build(), false);

        }).match(StopMsg.class, msg -> {
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
