/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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

    public synchronized void writeEdgeText(Edge edge) {
        try {
            FileWriter fw = new FileWriter("edges.txt");
            PrintWriter pr = new PrintWriter(fw);
            pr.println(edge.toString());
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public synchronized void writeEdgeBuffered(List<Edge> edges) {
        try {
            FileOutputStream fos = new FileOutputStream("edges.dat");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(edges);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }    

    public synchronized void writeEdgeText(List<Edge> edges) {
        try {
            FileWriter fw = new FileWriter("edges.txt");
            PrintWriter pr = new PrintWriter(fw);
            for (Edge e : edges) {
                pr.println(e.toString());
            }
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void writeEdgeTextBuffered(List<Edge> edges) {
        try {
            FileWriter fw = new FileWriter("edges.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pr = new PrintWriter(fw);
            for (Edge e : edges) {
                bw.write(e.toString());
                bw.newLine();
            }
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
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

    public synchronized List<Edge> readEdgesBuffered() {
        List<Edge> readedEdges;
        try {
            FileInputStream fis = new FileInputStream("edges.dat");
            BufferedInputStream bis = new BufferedInputStream(fis);
            try (ObjectInputStream in = new ObjectInputStream(bis)) {
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

    public synchronized List<Edge> readEdgesTextBuffered() {
        try {
            FileReader fr = new FileReader("edges.txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
            Scanner inputScanner = new Scanner(fr);
            List<String> lines = new ArrayList<>(); //Een lijst met het aantal lijnen uit het tekstbestand.
            String readedLine; //De huidige lijn.
            while ((readedLine = inputScanner.nextLine()) != null) { //Leest het bestand uit tot dat er geen lijnen meer zijn.
                System.out.println(readedLine); //Print de huidige lijn.
                lines.add(readedLine); //Voegt de huidige lijn toe aan de lijst met lijnen.
            }

            List<Edge> readedEdges = new ArrayList<Edge>();
            for (int i = lines.size(); i > 0; i--) { //Leest de lijst met de lijnen van het tekstbestand uit.
                String t = lines.get(i - 1); //Pakt de huidige lijn uit het tekstbestand.
                String[] velden = t.split(",");
                int X1 = Integer.parseInt(velden[0]);
                int X2 = Integer.parseInt(velden[1]);
                int Y1 = Integer.parseInt(velden[2]);
                int Y2 = Integer.parseInt(velden[3]);
                String color = velden[4];
                readedEdges.add(new Edge(X1, Y1, X2, Y2, color));
            }
            fr.close();
            inputScanner.close();
            return readedEdges;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized List<Edge> readEdgesText() {
        try {
            FileReader fr = new FileReader("edges.txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
            BufferedReader br = new BufferedReader(fr); //We maken een bufferedReader aan die het bestand uit de filereader pakt.

            List<String> lines = new ArrayList<>(); //Een lijst met het aantal lijnen uit het tekstbestand.
            String readedLine; //De huidige lijn.
            while ((readedLine = br.readLine()) != null) { //Leest het bestand uit tot dat er geen lijnen meer zijn.
                System.out.println(readedLine); //Print de huidige lijn.
                lines.add(readedLine); //Voegt de huidige lijn toe aan de lijst met lijnen.
            }

            List<Edge> readedEdges = new ArrayList<Edge>();
            for (int i = lines.size(); i > 0; i--) { //Leest de lijst met de lijnen van het tekstbestand uit.
                String t = lines.get(i - 1); //Pakt de huidige lijn uit het tekstbestand.
                String[] velden = t.split(",");
                int X1 = Integer.parseInt(velden[0]);
                int X2 = Integer.parseInt(velden[1]);
                int Y1 = Integer.parseInt(velden[2]);
                int Y2 = Integer.parseInt(velden[3]);
                String color = velden[4];
                readedEdges.add(new Edge(X1, Y1, X2, Y2, color));
            }
            fr.close();
            br.close();
            return readedEdges;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized void addTimeStamp(String s) {
        time += s;
    }

    public synchronized void addEdge(Edge e) {
        edges.add(e);
        if (edges.size() >= (int) (3 * Math.pow(4, currentLevel - 1))) {
            System.out.println("Done generating");
            writeEdge(edges);
        }
    }

    public void stop() {
        pool.shutdown();
    }
}
