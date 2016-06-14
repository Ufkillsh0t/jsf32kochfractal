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
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
    private int fileSize = 10 * 1024 * 1024;
    public TimeStamp tsDe;
    private WriteType writeType;
    private String path;

    public KochManager() {
        pool = Executors.newFixedThreadPool(3);
        edges = new ArrayList<Edge>();
        path = "D:\\School\\S3\\JSF3\\Week 13\\Bestanden\\"; //"/home/jsf3/MountedDrive/";
    }

    public void changeLevel(int nxt, WriteType writeType) {
        tsDe = new TimeStamp();
        tsDe.setBegin("Begin Level<" + nxt + ">");
        time = "";
        this.writeType = writeType;
        edges.clear();
        currentLevel = nxt;
        
        KochCallable kt1 = new KochCallable(this, currentLevel, EdgeSide.Left);
        KochCallable kt2 = new KochCallable(this, currentLevel, EdgeSide.Right);
        KochCallable kt3 = new KochCallable(this, currentLevel, EdgeSide.Bottom);

        pool.submit(kt1);
        pool.submit(kt2);
        pool.submit(kt3);
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
            FileOutputStream fos = new FileOutputStream(path + "edges" + currentLevel + ".dat");
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
            FileWriter fw = new FileWriter(path + "edges" + currentLevel + ".txt");
            PrintWriter pr = new PrintWriter(fw);
            pr.println(edge.toString());
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
        ---Schrijft een lijst van edges weg met behulp van object outputstream.
    */

    public synchronized void writeEdge(List<Edge> edges) {
        try {
            FileOutputStream fos = new FileOutputStream(path + "edges" + currentLevel + ".dat");
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
            FileOutputStream fos = new FileOutputStream(path + "edges" + currentLevel + ".dat");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(edges);
            }
            bos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }
    
    /*
        ---Schrijft edges weg naar een tekst bestand.
    */

    public synchronized void writeEdgeText(List<Edge> edges) {
        try {
            FileWriter fw = new FileWriter(path + "edges" + currentLevel + ".txt");
            PrintWriter pr = new PrintWriter(fw);
            for (Edge e : edges) {
                pr.println(e.toString());
            }
            pr.flush();
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void writeEdgeTextBuffered(List<Edge> edges) {
        try {
            FileWriter fw = new FileWriter(path + "edges" + currentLevel + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pr = new PrintWriter(fw);
            for (Edge e : edges) {
                bw.write(e.toString());
                bw.newLine();
            }
            bw.flush();
            pr.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
        Schrijft de edges weg naar een memorymapped file.
    */
    public synchronized void writeMapped(List<Edge> edges){
        try{
            RandomAccessFile memoryMappedFile = new RandomAccessFile(path + "edges" + currentLevel + ".bin", "rw");
            MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, (7*8)*edges.size());
            for(Edge e : edges){
                out.putDouble(e.X1);
                out.putDouble(e.Y1);
                out.putDouble(e.X2);
                out.putDouble(e.Y2);
                out.putDouble(e.hue);
                out.putDouble(e.saturation);
                out.putDouble(e.brightness);                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
        ---Leest het bestand met edges uit
    */
    
    public synchronized List<Edge> readEdges() {
        List<Edge> readedEdges;
        try {
            FileInputStream fis = new FileInputStream(path + "edges" + currentLevel + ".dat");
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
            FileInputStream fis = new FileInputStream(path + "edges" + currentLevel + ".dat");
            BufferedInputStream bis = new BufferedInputStream(fis);
            try (ObjectInputStream in = new ObjectInputStream(bis)) {
                readedEdges = (List<Edge>) in.readObject();
                bis.close();
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

    public synchronized List<Edge> readEdgesText() {
        try {
            System.out.println("\n");
            FileReader fr = new FileReader(path + "edges" + currentLevel + ".txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
            Scanner inputScanner = new Scanner(fr);
            List<String> lines = new ArrayList<>(); //Een lijst met het aantal lijnen uit het tekstbestand.
            String readedLine; //De huidige lijn.
            while (inputScanner.hasNextLine()) { //Leest het bestand uit tot dat er geen lijnen meer zijn.
                readedLine = inputScanner.nextLine();
                //System.out.println(readedLine); //Print de huidige lijn.
                lines.add(readedLine); //Voegt de huidige lijn toe aan de lijst met lijnen.
            }

            List<Edge> readedEdges = new ArrayList<Edge>();
            for (int i = lines.size(); i > 0; i--) { //Leest de lijst met de lijnen van het tekstbestand uit.
                String t = lines.get(i - 1); //Pakt de huidige lijn uit het tekstbestand.
                String[] velden = t.split(",");
                float X1 = Float.parseFloat(velden[0]);
                float X2 = Float.parseFloat(velden[1]);
                float Y1 = Float.parseFloat(velden[2]);
                float Y2 = Float.parseFloat(velden[3]);
                float hue = Float.parseFloat(velden[4]);
                float saturation = Float.parseFloat(velden[5]);
                float brightness = Float.parseFloat(velden[6]);
                readedEdges.add(new Edge(X1, Y1, X2, Y2, hue, saturation, brightness));
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

    public synchronized List<Edge> readEdgesTextBuffered() {
        try {
            FileReader fr = new FileReader(path + "edges" + currentLevel + ".txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
            BufferedReader br = new BufferedReader(fr); //We maken een bufferedReader aan die het bestand uit de filereader pakt.

            List<String> lines = new ArrayList<>(); //Een lijst met het aantal lijnen uit het tekstbestand.
            String readedLine; //De huidige lijn.
            while ((readedLine = br.readLine()) != null) { //Leest het bestand uit tot dat er geen lijnen meer zijn.
                //System.out.println(readedLine); //Print de huidige lijn.
                lines.add(readedLine); //Voegt de huidige lijn toe aan de lijst met lijnen.
            }

            List<Edge> readedEdges = new ArrayList<Edge>();
            for (int i = lines.size(); i > 0; i--) { //Leest de lijst met de lijnen van het tekstbestand uit.
                String t = lines.get(i - 1); //Pakt de huidige lijn uit het tekstbestand.
                String[] velden = t.split(",");
                float X1 = Float.parseFloat(velden[0]);
                float X2 = Float.parseFloat(velden[1]);
                float Y1 = Float.parseFloat(velden[2]);
                float Y2 = Float.parseFloat(velden[3]);
                float hue = Float.parseFloat(velden[4]);
                float saturation = Float.parseFloat(velden[5]);
                float brightness = Float.parseFloat(velden[6]);
                readedEdges.add(new Edge(X1, Y1, X2, Y2, hue, saturation, brightness));
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
            switch(writeType){
                case Binairy:
                    writeEdge(edges);
                    break;
                case Text:
                    writeEdgeText(edges);
                    break;
                case BufferedBinairy:
                    writeEdgeBuffered(edges);
                    break;
                case BufferedText:
                    writeEdgeTextBuffered(edges);
                    break;
                case Mapped:
                    writeMapped(edges);
                    break;
            }
            tsDe.setEnd("Done writing <" + currentLevel + ">");
            System.out.println(tsDe.toString());
            addTimeStamp(tsDe.toString());
        }
    }

    public void stop() {
        pool.shutdown();
    }
}
