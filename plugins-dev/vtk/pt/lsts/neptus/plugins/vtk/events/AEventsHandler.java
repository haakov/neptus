/*
 * Copyright (c) 2004-2014 Universidade do Porto - Faculdade de Engenharia
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
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
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
 * Author: hfq
 * Mar 17, 2014
 */
package pt.lsts.neptus.plugins.vtk.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.plugins.vtk.utils.Utils;
import pt.lsts.neptus.plugins.vtk.visualization.AInteractorStyleTrackballCamera;
import pt.lsts.neptus.plugins.vtk.visualization.Canvas;
import vtk.vtkPNGWriter;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkWindowToImageFilter;

/**
 * @author hfq
 * 
 */
public abstract class AEventsHandler {

    private Canvas canvas;
    private AInteractorStyleTrackballCamera interactorStyle;
    private vtkRenderer renderer;
    private vtkRenderWindowInteractor interactor;

    // A PNG Writer for screenshot captures
    protected vtkPNGWriter snapshotWriter;
    // Internal Window to image Filter. Needed by a snapshotWriter object
    protected vtkWindowToImageFilter wif;

    /**
     * @param canvas
     * @param renderer
     * @param interactor
     * @param interactorStyle
     */
    public AEventsHandler(Canvas canvas, vtkRenderer renderer, vtkRenderWindowInteractor interactor,
            AInteractorStyleTrackballCamera interactorStyle) {
        this.canvas = canvas;
        this.renderer = renderer;
        this.interactor = interactor;
        this.interactorStyle = interactorStyle;

        // Create the image filter and PNG writer objects
        wif = new vtkWindowToImageFilter();
        snapshotWriter = new vtkPNGWriter();
        snapshotWriter.SetInputConnection(wif.GetOutputPort());
    }

    /**
     * 
     * @param interactorStyle
     */
    public AEventsHandler(AInteractorStyleTrackballCamera interactorStyle) {
        this(interactorStyle.getCanvas(), interactorStyle.getCanvas().GetRenderer(),
                interactorStyle.getCanvas().getRenderWindowInteractor(), interactorStyle);
    }

    /**
     * Initial params configurations
     */
    protected abstract void init();

    /**
     * Syncronously take a snapshot of a 3D view Saves on neptus directory
     */
    public void takeSnapShot() {
        Utils.goToAWTThread(new Runnable() {

            @Override
            public void run() {
                try {
                    interactorStyle.FindPokedRenderer(interactor.GetEventPosition()[0],
                            interactor.GetEventPosition()[1]);
                    wif.SetInput(interactor.GetRenderWindow());
                    wif.Modified();
                    snapshotWriter.Modified();

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssmm").format(Calendar.getInstance()
                            .getTimeInMillis());
                    timeStamp = "snapshot_" + timeStamp;
                    NeptusLog.pub().info("Snapshot timeStamp: " + timeStamp);

                    snapshotWriter.SetFileName(timeStamp);

                    if (!canvas.isWindowSet()) {
                        canvas.lock();
                        canvas.Render();
                        canvas.unlock();
                    }

                    canvas.lock();
                    wif.Update();
                    canvas.unlock();

                    snapshotWriter.Write();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * @return the canvas
     */
    protected Canvas getCanvas() {
        return canvas;
    }

    /**
     * @param canvas the canvas to set
     */
    protected void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * @return the interactorStyle
     */
    protected AInteractorStyleTrackballCamera getInteractorStyle() {
        return interactorStyle;
    }

    /**
     * @param interactorStyle the interactorStyle to set
     */
    protected void setInteractorStyle(AInteractorStyleTrackballCamera interactorStyle) {
        this.interactorStyle = interactorStyle;
    }

    /**
     * @return the renderer
     */
    protected vtkRenderer getRenderer() {
        return renderer;
    }

    /**
     * @param renderer the renderer to set
     */
    protected void setRenderer(vtkRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * @return the interactor
     */
    protected vtkRenderWindowInteractor getInteractor() {
        return interactor;
    }

    /**
     * @param interactor the interactor to set
     */
    protected void setInteractor(vtkRenderWindowInteractor interactor) {
        this.interactor = interactor;
    }

}
