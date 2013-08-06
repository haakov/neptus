/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: José Pinto
 * Dec 20, 2012
 */
package pt.up.fe.dceg.neptus.mp.preview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.global.Settings;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import pt.up.fe.dceg.neptus.i18n.I18n;
import pt.up.fe.dceg.neptus.mp.Maneuver;
import pt.up.fe.dceg.neptus.mp.ManeuverLocation;
import pt.up.fe.dceg.neptus.mp.ManeuverLocation.Z_UNITS;
import pt.up.fe.dceg.neptus.mp.SystemPositionAndAttitude;
import pt.up.fe.dceg.neptus.mp.maneuvers.LocatedManeuver;
import pt.up.fe.dceg.neptus.plugins.mra3d.Marker3d;
import pt.up.fe.dceg.neptus.types.coord.LocationType;
import pt.up.fe.dceg.neptus.types.mission.plan.PlanType;

/**
 * @author zp
 *
 */
public class PlanSimulation3D extends JPanel {

    private static final long serialVersionUID = 1L;
    protected Chart chart = null;
    protected LineStrip path = null;
    protected PlanSimulationOverlay overlay;
    protected PlanType plan;
    
    protected void init() {
        while (!overlay.simulationFinished)
            Thread.yield();
        
        Settings.getInstance().setHardwareAccelerated(true);
        chart = new Chart(Quality.Advanced, "swing");
        path = new LineStrip();
        Coord3d firstCoord = null;
        Coord3d lastCoord = new Coord3d();
        
        for (int i = 0; i < overlay.getStates().size(); i++) {
            SystemPositionAndAttitude state = overlay.getStates().get(i);
            java.awt.Color c = overlay.colors.get(i).darker();
            LocationType loc = state.getPosition();
            double offsets[] = loc.getOffsetFrom(overlay.ref);
            double depth = -loc.getDepth();
            lastCoord = new Coord3d(-offsets[0], offsets[1], depth);
            path.add(new Point(lastCoord, new Color(c.getRed(),c.getGreen(),c.getBlue()), 1f));
            if (firstCoord == null)
                firstCoord = new Coord3d(-offsets[0], offsets[1], depth);
        }
        
        
        if (plan != null) {
        
            for (Maneuver m : plan.getGraph().getAllManeuvers()) {
                if (m instanceof LocatedManeuver) {
                    ManeuverLocation loc = ((LocatedManeuver)m).getStartLocation();
                    double[] offsets = loc.getOffsetFrom(overlay.ref);

                    if (loc.getZUnits() == Z_UNITS.DEPTH) {
                        chart.getScene().add(new Marker3d(m.getId(), new Coord3d(-offsets[0], offsets[1], -loc.getZ()), java.awt.Color.blue));

                        ArrayList<Coord3d> coords = new ArrayList<>();
                        coords.add(new Coord3d(-offsets[0], offsets[1], 0));
                        coords.add(new Coord3d(-offsets[0], offsets[1], -loc.getZ()));                    
                        LineStrip strip = new LineStrip(coords);
                        strip.setWireframeColor(new Color(0, 100, 100, 100));
                        chart.getScene().add(strip);
                    }
                    else if (loc.getZUnits() == Z_UNITS.ALTITUDE) { 
                        chart.getScene().add(new Marker3d(m.getId(), new Coord3d(-offsets[0], offsets[1], -(SimulationEngine.simBathym.getSimulatedDepth(loc)-loc.getZ())), java.awt.Color.blue));

                        ArrayList<Coord3d> coords = new ArrayList<>();
                        coords.add(new Coord3d(-offsets[0], offsets[1], -SimulationEngine.simBathym.getSimulatedDepth(loc)));
                        coords.add(new Coord3d(-offsets[0], offsets[1], -(SimulationEngine.simBathym.getSimulatedDepth(loc)-loc.getZ())));                    
                        LineStrip strip = new LineStrip(coords);
                        strip.setWireframeColor(new Color(0, 100, 100, 100));
                        chart.getScene().add(strip);
                    }
                    else
                        chart.getScene().add(new Marker3d(m.getId(), new Coord3d(-offsets[0], offsets[1], 0), java.awt.Color.blue));
                }
            }
        }
        path.add(new Point(lastCoord, new Color(0, 0, 0, 0), 0f));
        path.add(new Point(firstCoord, new Color(0, 0, 0, 0), 0f));
        path.setWidth(2f);
        chart.getScene().add(path);
        chart.getView().setMaximized(true);        
        ChartLauncher.configureControllers(chart, "chart", true, false);
        
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                add((Component) chart.getCanvas());     
                revalidate();
                repaint();
            }
        });        
    }
    
    public void cleanup() {
        if (chart != null) {
            chart.stopAnimator();
            chart.clear();
            chart.dispose();            
        }
    }
    
    public PlanSimulation3D(PlanSimulationOverlay overlay, PlanType plan) {
        setLayout(new BorderLayout());
        this.overlay = overlay;
        this.plan = plan;
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run() {
                init();                
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public static void showSimulation(Window owner, PlanSimulationOverlay overlay, PlanType plan) {
        final PlanSimulation3D sim3d = new PlanSimulation3D(overlay, plan);
        JDialog dialog = new JDialog(owner, I18n.text("Plan execution preview"));
        dialog.setSize(600, 400);
        dialog.getContentPane().add(sim3d);
        dialog.setResizable(false);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
               sim3d.cleanup();
            }
        });
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
