package event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus<T> {
    
    private Map<Event<T>, List<Consumer<Event<T>>>> consumers = new HashMap<Event<T>, List<Consumer<Event<T>>>>();

    public void subscribe(Event<T> e, Consumer<Event<T>> c) {
        this.consumers.computeIfAbsent(e, k -> new ArrayList<>()).add(c);
    }

    public void unsubscribe(Event<T> e, Consumer<Event<T>> c) {
        this.consumers.computeIfPresent(e, (key, list) -> {list.remove(c); return list;});
    }

    public void publish (Event<T> event) {
        consumers.getOrDefault(event, new ArrayList<Consumer<Event<T>>>()).forEach(c -> c.accept(event));
    }
}
