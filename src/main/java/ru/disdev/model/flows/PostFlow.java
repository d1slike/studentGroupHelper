package ru.disdev.model.flows;

import ru.disdev.entity.post.Post;
import ru.disdev.model.Prototype;

import java.util.concurrent.ScheduledFuture;

@Prototype
public class PostFlow extends AbstractPostFlow<Post> {


    public PostFlow(long chatId, ScheduledFuture<?> cancelTask) {
        super(chatId, cancelTask);
    }

    @Override
    public Post buildResult() {
        return new Post();
    }
}
