/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        KochCallable kt1 = new KochCallable(this, nxt, EdgeSide.Left);
        KochCallable kt2 = new KochCallable(this, nxt, EdgeSide.Right);
        KochCallable kt3 = new KochCallable(this, nxt, EdgeSide.Bottom);

        pool.submit(kt1);
        pool.submit(kt2);
        pool.submit(kt3);

        tsDe.setEnd("Submitted thread pools");

        addTimeStamp(tsDe.toString());
    }

    /*
        nogal inefficient
    */
    public synchronized void writeEdge(Edge edge) {
        try {
            List<Edge> edges = readEdges();
            if (edges == null) {
                edges = new ArrayList<Edge>();
            }
            edges.add(edge);
            FileOutputStream fos = new FileOutputStream("edges.dat");
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(edges);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    public synchronized void writeEdge(List<Edge> edges) {
        try {
            FileOutputStream fos = new FileOutputStream("edges.dat");
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(edges);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    public synchronized List<Edge> readEdges() {
        List<Edge> readedEdges;
        try {
            FileInputStream fis = new FileInputStream("edges.dat");
            try (ObjectInputStream in = new ObjectInputStream(fis)) {
                readedEdges = (List<Edge>) in.readObject();
                return readedEdges;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public synchronized void addTimeStamp(String s) {
        time += s;
    }

    public synchronized void addEdge(Edge e) {
        edges.add(e);
        writeEdge(e);
        if (edges.size() >= (int) (3 * Math.pow(4, currentLevel - 1))) {
            System.out.println("Done generating");
        }
    }

    public void stop() {
        pool.shutdown();
    }
}
