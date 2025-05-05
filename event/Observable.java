package event;

public interface Observable {

    void register(Observer observer);
    void notifyObservers();
}