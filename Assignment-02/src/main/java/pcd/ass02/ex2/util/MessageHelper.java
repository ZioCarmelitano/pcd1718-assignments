package pcd.ass02.ex2.util;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MessageHelper {

    public static <T> Handler<Message<T>> wrap(Handler<? super T> handler) {
        checkNotNull(handler, "wrap is null");
        return message -> handler.handle(message.body());
    }

    private  MessageHelper() {
    }

}
