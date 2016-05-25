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
import java.util.concurrent.CyclicBarrier;
import javafx.concurrent.Task;

/**
 *
 * @author tim
 */
public class KochTask extends Task implements  Observer{

    private KochManager km;
    private KochFractal kf;
    private int level;
    private List<Edge> edges;
    private CyclicBarrier cb;
    private EdgeSide side;
    private int progress;
    
    public KochTask(KochManager km, int level, EdgeSide side){
        this.level = level;
        this.side = side;
        this.km = km;
        this.cb = cb;
        this.edges = new ArrayList<Edge>();

        kf = new KochFractal();
        kf.addObserver(this);
        kf.setLevel(level);
    }
    
    public int getLevel(){
        return level;
    }
    
    @Override
    protected Void call() throws Exception {
        switch(side){
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
        
        progress++;
        this.updateProgress(progress, kf.getNrOfEdges() / 3);   
    }
}
