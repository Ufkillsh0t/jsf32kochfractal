/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import timeutil.TimeStamp;

/**
 *
 * @author tim
 */
public class KochManager {

    private static final String USERDIR = System.getProperty("user.dir");

    private ExecutorService pool;
    private KochTask kt1, kt2, kt3;
    private List<Edge> edges;
    public int count;
    private String time;
    private int currentLevel;
    public TimeStamp tsDe;

    public KochManager() {
        pool = Executors.newFixedThreadPool(3);
        edges = new ArrayList<Edge>();
    }

    public void changeLevel(int nxt) {
        TimeStamp tsDe = new TimeStamp();
        tsDe.setBegin("Begin Level<" + nxt + ">");
        time = "";
        edges.clear();
        currentLevel = nxt;

        KochTask kt1 = new KochTask(this, nxt, EdgeSide.Left);
        KochTask kt2 = new KochTask(this, nxt, EdgeSide.Right);
        KochTask kt3 = new KochTask(this, nxt, EdgeSide.Bottom);

        pool.submit(kt1);
        pool.submit(kt2);
        pool.submit(kt3);

        tsDe.setEnd("Submitted thread pools");

        addTimeStamp(tsDe.toString());
    }

    public synchronized void addTimeStamp(String s) {
        time += s;
    }

    public synchronized void addEdge(Edge e) {
        edges.add(e);
        if (edges.size() >= (int) (3 * Math.pow(4, currentLevel - 1))) {
            System.out.println("Done generating");
        }
    }

    public void stop() {
        pool.shutdown();
    }
}
