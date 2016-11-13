package ru.disdev.model.flows;

import ru.disdev.entity.Post;
import ru.disdev.entity.Prototype;

@Prototype
public class PostFlow extends AbstractPostFlow<Post> {
    public PostFlow(long chatId) {
        super(chatId);
    }

    @Override
    public Post buildResult() {
        return new Post();
    }
}
