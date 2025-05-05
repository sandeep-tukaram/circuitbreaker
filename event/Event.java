package event;

public class Event<T> {
    T newState;

    public Event(T newState) {
        this.newState = newState;
    }

    public T getNewState() {
        return newState;
    }

}
