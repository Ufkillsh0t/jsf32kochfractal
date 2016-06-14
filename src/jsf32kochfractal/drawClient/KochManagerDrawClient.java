/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractal.drawClient;

import calculate.Edge;
import calculate.EdgeSide;
import calculate.KochCallable;
import calculate.KochManager;
import calculate.KochTask;
import calculate.WriteType;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
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
public class KochManagerDrawClient {

    private static final String USERDIR = System.getProperty("user.dir");

    private List<Edge> edges;
    public int count;
    private String time;
    private int currentLevel;
    private WriteType writeType;
    public TimeStamp tsDe;
    private JSF32KochFractalDrawClient application;
    private String path;

    public KochManagerDrawClient(JSF32KochFractalDrawClient application) {
        edges = new ArrayList<Edge>();
        this.application = application;
        path = "D:\\School\\S3\\JSF3\\Week 13\\Bestanden\\"; //"/home/jsf3/MountedDrive/";
    }

    public void drawEdges() {
        application.clearKochPanel();

        TimeStamp draw = new TimeStamp();
        draw.setBegin("Pre-Draw");
        for (Edge e : edges) {
            application.drawEdge(e);
        }
        draw.setEnd("KochFractal has been drawn");

        tsDe.setEnd("Done drawing");
        addTimeStamp(tsDe.toString());

        application.setTextCalc(tsDe.toString());
        application.setTextDraw(draw.toString());
        application.setTextNrEdges(Integer.toString(edges.size()));
    }

    public void changeLevel(int nxt, WriteType writeType) {
        tsDe = new TimeStamp();
        tsDe.setBegin("Begin Level<" + nxt + ">");
        time = "";
        this.writeType = writeType;
        edges.clear();
        currentLevel = nxt;

        switch (writeType) {
            case Binairy:
                edges = readEdges(nxt);
                break;
            case Text:
                edges = readEdgesText(nxt);
                break;
            case BufferedBinairy:
                edges = readEdgesBuffered(nxt);
                break;
            case BufferedText:
                edges = readEdgesTextBuffered(nxt);
                break;
            case Mapped:
                edges = readEdgesMappedFast(nxt);
                break;
        }

        application.requestDrawEdges();
    }

    public synchronized List<Edge> readEdgesMapped(int lvl) {
        List<Edge> readedEdges = new ArrayList<>();
        try {
            System.out.println("Reading edges mapped");
            FileInputStream fis = new FileInputStream(path + "edges" + lvl + ".bin");
            DataInputStream dis = new DataInputStream(fis);
            while (dis.available() > 0) {
                double X1 = dis.readDouble();
                double Y1 = dis.readDouble();
                double X2 = dis.readDouble();
                double Y2 = dis.readDouble();
                double hue = dis.readDouble();
                double saturation = dis.readDouble();
                double brightness = dis.readDouble();

                Edge e = new Edge(X1, Y1, X2, Y2, hue, saturation, brightness);
                readedEdges.add(e);
            }
            return readedEdges;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManagerDrawClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KochManagerDrawClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return readedEdges;
    }

    public synchronized List<Edge> readEdgesMappedFast(int lvl) {
        List<Edge> readedEdges = new ArrayList<>();
        try {
            RandomAccessFile file = new RandomAccessFile(new File("mappedEdges.txt"), "r");
            FileChannel fc = file.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            //long length = file.length();
            while (mbb.hasRemaining()) {
                double X1 = mbb.getDouble();
                double Y1 = mbb.getDouble();
                double X2 = mbb.getDouble();
                double Y2 = mbb.getDouble();
                double hue = mbb.getDouble();
                double saturation = mbb.getDouble();
                double brightness = mbb.getDouble();

                readedEdges.add(new Edge(X1, Y1, X2, Y2, hue, saturation, brightness));
            }
            return readedEdges;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManagerDrawClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KochManagerDrawClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return readedEdges;
    }

    public synchronized List<Edge> readEdges(int lvl) {
        List<Edge> readedEdges;
        try {
            FileInputStream fis = new FileInputStream(path + "edges" + lvl + ".dat");
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

    public synchronized List<Edge> readEdgesBuffered(int lvl) {
        List<Edge> readedEdges;
        try {
            FileInputStream fis = new FileInputStream(path + "edges" + lvl + ".dat");
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

    public synchronized List<Edge> readEdgesText(int lvl) {
        try {
            System.out.println("\n");
            FileReader fr = new FileReader(path + "edges" + lvl + ".txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
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

    public synchronized List<Edge> readEdgesTextBuffered(int lvl) {
        try {
            FileReader fr = new FileReader(path + "edges" + lvl + ".txt"); //We maken gebruik van de filereader om het tekstbestand uit te lezen.
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
}
