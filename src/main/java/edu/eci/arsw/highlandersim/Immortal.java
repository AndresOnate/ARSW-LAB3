package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private Boolean running;

    private Object lock;

    public Boolean alive;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.running = true;
        this.lock = lock;
        this.alive = true;
    }

    public void run() {
        while (alive && immortalsPopulation.size() > 1) {
            synchronized (lock) {
                while (!running) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void fight(Immortal i2) {
        int currentImmortal = System.identityHashCode(this);
        int secondImmortal = System.identityHashCode(i2);
        if(currentImmortal < secondImmortal){
            synchronized (this) {
                synchronized (i2) {
                    swapHealth(i2);
                }
            }
        }else{
            synchronized (i2) {
                synchronized (this) {
                    swapHealth(i2);
                }
            }
        }

    }

    private void swapHealth(Immortal i2){
        if (this.health>0){
            if (i2.getHealth() > 0) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
                this.health += defaultDamageValue;
                if (i2.getHealth() == 0) {
                    i2.killImmortal();
                }
                updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
            } else {
                updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
            }
        }else{
            this.killImmortal();
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void stopImmortal(){
        this.running = false;
    }

    public void resumeImmortal(){
        this.running = true;
    }

    public Object getLock(){
        return this.lock;
    }

    public void killImmortal(){
        this.alive = false;
        immortalsPopulation.remove(this);
    }

}
