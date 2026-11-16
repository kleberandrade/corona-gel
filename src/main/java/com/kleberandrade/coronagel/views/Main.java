package com.kleberandrade.coronagel.views;

import com.kleberandrade.coronagel.components.CarouselSlider;
import com.kleberandrade.coronagel.helpers.LogHelper;
import com.kleberandrade.coronagel.helpers.MouseHelper;
import com.kleberandrade.coronagel.sensors.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.JFrame;

/**
 *
 * @author Kleber de Oliveira Andrade
 */
public class Main extends javax.swing.JFrame {

    private static final int DELTA_TIME = 7000;
    private static final int FADE_OUT_TIME = 500;
    private CarouselSlider carouselSlider;
    private SensorManager sensorManager;

    public Main() {
        super.setExtendedState(JFrame.MAXIMIZED_BOTH);
        initComponents();
        initialize();
    }

    private void initialize() {
        LogHelper.write("Opening Corona Gel 2.0; hardware=" + Boolean.getBoolean("coronagel.hardware"));
        MouseHelper.hideCursor(this);
        carouselSlider = new CarouselSlider(DELTA_TIME, FADE_OUT_TIME);
        setContentPane(carouselSlider);

        SimulatedPorts simulation = new SimulatedPorts();
        SensorManager.Ports ports = createPorts(simulation);
        sensorManager = new SensorManager(ports, carouselSlider);
        Runtime.getRuntime().addShutdownHook(new Thread(sensorManager::close));
        sensorManager.start();
        bindSimulation("pressed SPACE", true, simulation);
        bindSimulation("released SPACE", false, simulation);
    }

    private SensorManager.Ports createPorts(SimulatedPorts simulation) {
        return Boolean.getBoolean("coronagel.hardware") ? createHardwarePorts() : simulation;
    }

    private SensorManager.Ports createHardwarePorts() {
        try {
            return new Pi4jPorts();
        } catch (RuntimeException | Error failure) {
            carouselSlider.stop();
            dispose();
            throw failure;
        }
    }

    private void bindSimulation(String key, boolean detected, SimulatedPorts simulation) {
        carouselSlider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        carouselSlider.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { simulation.detect(detected); }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Corona Gel");
        setAlwaysOnTop(true);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1920, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1080, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(1920, 1080));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        LogHelper.write("Close system");
        carouselSlider.stop();
        if (sensorManager != null) {
            sensorManager.dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
