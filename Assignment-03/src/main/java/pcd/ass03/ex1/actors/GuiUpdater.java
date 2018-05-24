package pcd.ass03.ex1.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import javafx.scene.canvas.Canvas;
import pcd.ass03.ex1.actors.msg.*;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.view.game.RenderingService;

import java.time.Duration;

public class GuiUpdater extends AbstractLoggingActor {

    private static final int UPDATE_INTERVAL = 50;

    private ActorRef boardUpdater;
    private final int numberOfWorkers;
    private Board currentBoard;

    private Receive running;
    private Receive paused;

    public GuiUpdater(Canvas boardView, int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;

        this.running = receiveBuilder().match(StopMsg.class, stopMsg -> {
            log().info("GuiUpdater StopMsg");
            boardUpdater.tell(new StopMsg(), getSelf());
            context().stop(getSelf());
        }).match(StartMsg.class, startMsg -> {
            log().info("GuiUpdater StartMsg");
            currentBoard = startMsg.getBoard();
            boardUpdater.tell(new StartMsg(currentBoard), getSelf());
        }).match(NewBoardMsg.class, newBoardMsg -> {
            log().info("GuiUpdater NewBoardMsg");
            currentBoard = newBoardMsg.getNewBoard();
            RenderingService.renderBoard(boardView, newBoardMsg.getNewBoard());
            getContext().getSystem().scheduler().scheduleOnce(
                    Duration.ofMillis(UPDATE_INTERVAL),
                    getSelf(), new StartMsg(currentBoard),
                    getContext().getSystem().dispatcher(),
                    getSelf());
        }).match(PauseMsg.class, pauseMsg -> {
            log().info("GuiUpdater PauseMsg");
            getContext().become(paused);
        }).build();

        this.paused = receiveBuilder().match(StopMsg.class, stopMsg -> {
            log().info("GuiUpdater StopMsg");
            boardUpdater.tell(new StopMsg(), getSelf());
            context().stop(getSelf());
        }).match(ResumeMsg.class, resumeMsg -> {
            log().info("GuiUpdater ResumeMsg");
            getContext().become(running);
            boardUpdater.tell(new StartMsg(currentBoard), getSelf());
        }).build();

    }

    public static Props props(Canvas boardView, int numberOfWorkers) {
        return Props.create(GuiUpdater.class, boardView, numberOfWorkers);
    }

    @Override
    public void preStart() throws Exception {
        boardUpdater = getContext().actorOf(BoardUpdater.props(numberOfWorkers), "boardUpdater");
    }

    @Override
    public Receive createReceive() {
        return this.running;
    }

}
