/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import timeutil.TimeStamp;

/**
 *
 * @author tim
 */
public class KochCallable implements Callable, Observer {

    private KochManager km;
    private KochFractal kf;
    private int level;
    private List<Edge> edges;
    private EdgeSide side;

    public KochCallable(KochManager km, int level, EdgeSide side) {
        this.level = level;
        this.side = side;
        this.km = km;
        this.edges = new ArrayList<Edge>();

        kf = new KochFractal();
        kf.addObserver(this);
        kf.setLevel(level);
    }

    @Override
    public Object call() throws Exception {
        System.out.println("Called");
        switch (side) {
            case Bottom:
                kf.generateBottomEdge();
                break;
            case Left:
                kf.generateLeftEdge();
                break;
            case Right:
                kf.generateRightEdge();
                break;
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        Edge edge = (Edge) arg;
        edges.add(edge);
        km.addEdge(edge);
    }
}
