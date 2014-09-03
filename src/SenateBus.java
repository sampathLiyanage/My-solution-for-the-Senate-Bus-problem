import java.util.HashSet;
import java.util.concurrent.Semaphore;

class Semaphores{
    static Semaphore notLateForTheBus = new Semaphore(1);  //for synchronizing late comers
    static Semaphore busArrived = new Semaphore(0);        //for synchronizing bus arrivals and boarding
    static Semaphore allBoarded = new Semaphore(0);        //for synchronizing bus depart
}

class SenateBus{

    public static void main(String args[]) throws InterruptedException {
        int noOfRiders = 10;
        BusStop busstop = new BusStop();
        Bus bus1 = new Bus();
        Bus bus2 = new Bus();
        Rider riders[] = new Rider[noOfRiders*2];
        int i;
        for (i=0; i<noOfRiders; i++){
            riders[i] = new Rider(i, busstop);
            (new Thread(riders[i])).start();
        }
        bus1.board(busstop);

        for (int j=i; j<noOfRiders*2; j++){
            riders[j] = new Rider(j, busstop);
            (new Thread(riders[j])).start();
        }
        bus2.board(busstop);
    }
}

class Rider implements Runnable{

    public int id;
    private BusStop busStop;

    public Rider(int anId, BusStop aBusStop){
        id = anId;
        busStop = aBusStop;
    }



    public void getBoard() throws InterruptedException {
        //going to bus stop
        Semaphores.notLateForTheBus.acquire();
        busStop.riders.add(this);
        System.out.println("rider " + id + " arrived to bus stop");
        Semaphores.notLateForTheBus.release();

        //getting on the bus
        Semaphores.busArrived.acquire();
        if (busStop.riders.size()>0 && busStop.bus.riders.size() < busStop.bus.MAX_CAPACITY){
            busStop.riders.remove(this);
            busStop.bus.riders.add(this);
            System.out.println("rider " + id + " got on the bus");
        }
        if (busStop.riders.size()==0 || busStop.bus.riders.size() >= busStop.bus.MAX_CAPACITY) {
            Semaphores.allBoarded.release();
        } else {
	    Semaphores.busArrived.release();
	}

    }

    public void run(){
        try {
            getBoard();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


class Bus {

    public HashSet<Rider> riders;
    public final int MAX_CAPACITY = 6;
    BusStop busstop;

    public Bus(){
        riders = new HashSet<Rider>();
        busstop = null;
    }

    public void board(BusStop busStop) throws InterruptedException {
        Semaphores.notLateForTheBus.acquire();
        busstop = busStop;
        busStop.bus = this;
        System.out.println("bus arrived to the bus stop");
        Semaphores.busArrived.release();
        if (busstop.riders.size() > 0){
            Semaphores.allBoarded.acquire();
        } else {
	    Semaphores.busArrived.acquire();
	}
        busStop.bus = null;
        System.out.println("bus left the bus stop");
        busstop = null;
        Semaphores.notLateForTheBus.release();
    }
}

class BusStop{
    public HashSet<Rider>  riders;
    public Bus bus;

    public BusStop(){
        riders = new HashSet<Rider>();
        bus = null;
    }
}
