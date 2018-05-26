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

        this.running = receiveBuilder().match(Stop.class, stopMsg -> {
            log().info("GuiUpdater Stop");
            boardUpdater.tell(new Stop(), getSelf());
            context().stop(getSelf());
        }).match(Start.class, startMsg -> {
            log().info("GuiUpdater Start");
            currentBoard = startMsg.getBoard();
            boardUpdater.tell(new Start(currentBoard), getSelf());
        }).match(NewBoard.class, newBoard -> {
            log().info("GuiUpdater NewBoard");
            currentBoard = newBoard.getNewBoard();
            RenderingService.renderBoard(boardView, newBoard.getNewBoard());
            getContext().getSystem().scheduler().scheduleOnce(
                    Duration.ofMillis(UPDATE_INTERVAL),
                    getSelf(), new Start(currentBoard),
                    getContext().getSystem().dispatcher(),
                    getSelf());
        }).match(Pause.class, pause -> {
            log().info("GuiUpdater Pause");
            getContext().become(paused);
        }).build();

        this.paused = receiveBuilder().match(Stop.class, stopMsg -> {
            log().info("GuiUpdater Stop");
            boardUpdater.tell(new Stop(), getSelf());
            context().stop(getSelf());
        }).match(Resume.class, resume -> {
            log().info("GuiUpdater Resume");
            getContext().become(running);
            boardUpdater.tell(new Start(currentBoard), getSelf());
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
