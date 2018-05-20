package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import javafx.scene.canvas.Canvas;
import pcd.ass03.ex1.actors.msg.NewBoardMsg;
import pcd.ass03.ex1.actors.msg.StartMsg;
import pcd.ass03.ex1.actors.msg.StopMsg;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.actors.msg.PauseMsg;
import pcd.ass03.ex1.view.game.RenderingService;
import scala.concurrent.ExecutionContext;

import javax.naming.Context;
import java.time.Duration;

public class GuiUpdater extends AbstractLoggingActor {
    private static final int UPDATE_INTERVAL = 50;
    private ActorRef boardUpdater;
    private final int numberOfWorkers;

    private Receive running;
    private Receive paused;

    public GuiUpdater(Board board, Canvas boardView, int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;

        this.running = receiveBuilder().match(StopMsg.class, stopMsg -> {
            boardUpdater.tell(new StopMsg(), getSelf());
            context().stop(getSelf());
        }).match(StartMsg.class, startMsg -> {
            boardUpdater.tell(new StartMsg(board), getSelf());
        }).match(NewBoardMsg.class, newBoardMsg -> {
            RenderingService.renderBoard(boardView, board);
            getContext().getSystem().scheduler().scheduleOnce(Duration.ofMillis(UPDATE_INTERVAL), getSelf(), new StartMsg(newBoardMsg.getNewBoard()), getContext().getSystem().dispatcher(),getSelf());
        }).match(PauseMsg.class, pauseMsg -> {
            getContext().become(paused);
        }).build();

        this.paused = receiveBuilder().match(StopMsg.class, stopMsg -> {
            boardUpdater.tell(new StopMsg(), getSelf());
            context().stop(getSelf());
        }).match(StartMsg.class, startMsg -> {
            getContext().become(running);
            boardUpdater.tell(new StartMsg(board), getSelf());
        }).build();

    }

    public static Props props(Board board, Canvas boardView, int numberOfWorkers) {
        return Props.create(GuiUpdater.class, board, boardView, numberOfWorkers);
    }

    @Override
    public void preStart() throws Exception {
        boardUpdater = getContext().actorOf(BoardUpdater.props(numberOfWorkers), "boardUpdater");
    }

    @Override
    public Receive createReceive() {
        return this.paused;
    }
}
