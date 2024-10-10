package de.spraener.jsynth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

public class WaveDisplay extends JPanel{
    private float[] data;
    private int width = 1024;
    private int height = 768;

    public WaveDisplay(float[] data) {
        JFrame frame = new JFrame();
        frame.setTitle("WaveForm");
        frame.setSize(width,  height);
        frame.getContentPane().add(this);

        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == WindowEvent.WINDOW_CLOSED) {
                    System.exit(0);
                }
            }
        });
        this.data = data;
        frame.setVisible(true);
    }


    public void scaleWidth() {
        int blockSize = data.length / width;
        float[] avgData = new float[width];
        for( int i=0; i<width; i++ ) {
            float blockMax = 0;
            for( int j=0; j<blockSize; j++ ) {
                float d = data[i*blockSize+j];
                if( Math.abs(d) > blockMax) {
                    blockMax = d;
                }
            }
            avgData[i] = blockMax;
        }
        data = avgData;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 1024, 768);
        g2d.setColor(Color.BLACK);
        int y0 = height/2;
        int xp = 0;
        int yp = 0;
        for( int x = 0; x < Math.min(width, data.length); x++ ) {
            int y =(int)(height/2 * data[x] * 0.9);
            g2d.drawLine(xp,yp,x, y0-y);
            xp = x;
            yp = y0-y;
        }
    }
}
