package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.observer_factory;

import java.util.ArrayList;
import java.util.Observable;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.Phrases_Product;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class dataRepository_Observable {
    private ArrayList<stringFactory_Observer> buffer = new ArrayList<>();
    private int repositoryState;

    public void attach(stringFactory_Observer observer){
        buffer.add(observer);
    }
    public void detach(stringFactory_Observer observer){
        if(buffer.indexOf(observer)>=0)
            buffer.remove(observer);
    }
    public void setState(int state){
        this.repositoryState = state;
        notifyObservers();
    }
    public int getState(){
        return repositoryState;
    }
    public void notifyObservers(){
        for(stringFactory_Observer observer: buffer){
            observer.update();
        }
    }
}
