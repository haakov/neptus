package pt.lsts.neptus.hyperspectral;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang.ArrayUtils;
import org.jzy3d.maths.Array;

import com.google.common.eventbus.Subscribe;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.HyperSpecData;
import pt.lsts.neptus.console.ConsoleLayer;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.events.ConsoleEventMainSystemChange;
import pt.lsts.neptus.console.plugins.MainVehicleChangeListener;
import pt.lsts.neptus.plugins.ConfigurationListener;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.plugins.NeptusProperty.LEVEL;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.update.Periodic;
import pt.lsts.neptus.plugins.update.PeriodicUpdatesService;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.StateRenderer2D;

import java.awt.image.DataBufferByte;

/*
 * Copyright (c) 2004-2015 Universidade do Porto - Faculdade de Engenharia
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
 * Author: tsmarques
 * 13 May 2015
 */

/**
 * @author tsmarques
 *
 */
@SuppressWarnings("serial")
@PluginDescription(name = "Hyperspectral Layer", author = "tsmarques", version = "0.1")
@LayerPriority(priority = 40)
public class HyperspectralViewer extends ConsoleLayer implements ConfigurationListener {
    private static final String TEST_DATA_DIR = "./plugins-dev/hyperspectral/pt/lsts/neptus/hyperspectral/test-data/";
    
    public static final int MIN_FREQ = 0;
    public static final int MAX_FREQ = 640; /* also frame's width */
    /* frames will be FRAME_WIDTH x MAX_FREQ px */
    public static final int FRAME_HEIGHT = 250;
    
    
    private static final float FRAME_OPACITY = 0.9f;
    /* draw frames with opacity */
    private final AlphaComposite composite = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, FRAME_OPACITY);
    private final AffineTransform transform = new AffineTransform();
    
    private ConsoleLayout console;
    private String mainSys = "";
      
    /* testing */
    private BufferedImage dataDisplay; /* image currently being displayed */
    private Queue<byte[]> frames;
    private boolean framesLoaded = false;
    
    @NeptusProperty(editable = true, name = "Hyperspectral wavelength", userLevel = LEVEL.REGULAR)
    private int wavelengthProperty = 0;
    
    private int selectedWavelength = 320;
    private boolean initDisplay = false;
    

    public HyperspectralViewer() {
        this.console = getConsole();
        initDisplayedImage();
        /* testing */
    }
    
    private void initDisplayedImage() {
        dataDisplay = new BufferedImage(MAX_FREQ, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = dataDisplay.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, MAX_FREQ, FRAME_HEIGHT);
        g.dispose();
    }
    
    /* request data with new wavelength */
    private void requestWavelength() {
        frames = loadFrames(selectedWavelength + "/");
    }
    
    @Override
    public void propertiesChanged() {
        if(!(wavelengthProperty >= MIN_FREQ && wavelengthProperty <= MAX_FREQ))
            return;
        
        if(wavelengthProperty != selectedWavelength) {
            framesLoaded = false;            
            selectedWavelength = wavelengthProperty;
            
            initDisplayedImage();
            requestWavelength();
        }
    }
    
    private void updateDisplay(byte[] frameBytes) {
        try {
            BufferedImage newFrame = ImageIO.read(new ByteArrayInputStream(frameBytes));
            
            /* remove oldest frame */
            dataDisplay = dataDisplay.getSubimage(1, 0, MAX_FREQ - 1, FRAME_HEIGHT);
            dataDisplay = joinBufferedImage(dataDisplay, newFrame);
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    
    private BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(MAX_FREQ, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();

        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth(), 0);
        g2.dispose();
        
        return newImage;
    }
    
    /* for testing */
    /* load the frames columns */
    private Queue<byte[]> loadFrames(String path) {
        File dir = new File(TEST_DATA_DIR + path);
        File[] tmpFrames = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bmp");
            }            
        });
        
        if(!dir.exists())
            return new LinkedList<byte[]>();
        
        File frames[] = new File[tmpFrames.length];
       
        
        for(int i = 0; i < frames.length ; i++) {
            int filepos = Integer.parseInt(tmpFrames[i].getName().split(".bmp")[0]);
            frames[filepos] = tmpFrames[i];
        }
        
        Queue<byte[]> framesList = new LinkedList<>();
        
        for(int i = 0; i < frames.length; i++) {
            try {
                framesList.add(Files.readAllBytes(frames[i].toPath()));
            }
            catch (IOException e) { e.printStackTrace(); }
        }

        framesLoaded = true;
        return framesList;
    }
    
    /* Given a path for the test data,
       remove everything but the column
       correspondent to the selected wavelength 
       and save them in a folder named after the wavelength
    */
    private void cropFrames(int wave, String path) {
        File dir = new File(path);
        File[] imgFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bmp");
            }            
        });
        
        Arrays.sort(imgFiles, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
        try {
            for(int i = 0; i < imgFiles.length; i++) {
                BufferedImage frame = (BufferedImage) ImageIO.read(imgFiles[i]);
                
                BufferedImage cropped = frame.getSubimage(wave - 1, 0, 1, 250);
                ImageIO.write(cropped, "bmp", new File(TEST_DATA_DIR + wave + "/" + i + ".bmp"));   
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        if(!framesLoaded && !initDisplay) {
            int newX = -((MAX_FREQ / 2)) + (FRAME_HEIGHT / 2);
            int newY = (renderer.getHeight() - FRAME_HEIGHT) / 2;
            
            transform.translate(newX, newY);
            transform.rotate(Math.toRadians(-90), dataDisplay.getWidth() / 2, dataDisplay.getHeight() / 2);
                        
            initDisplay = true;
        }
        else {
            g.setColor(Color.red);
            g.drawString(selectedWavelength + " nm", (FRAME_HEIGHT / 2) - 30, (FRAME_HEIGHT / 2) - 60);
            g.setTransform(transform);
            g.setComposite(composite);
            g.drawImage(dataDisplay, 0, 0, renderer);
        }
    }
    
    
    @Subscribe
    public void on(HyperSpecData msg){
        if(msg.getSourceName() != mainSys)
            return;
        updateDisplay(msg.getData());
    }
    
    @Subscribe
    public void on(EstimatedState state) {
        if(!framesLoaded || !state.getSourceName().equals(mainSys))
            return;

        if(frames.isEmpty())
            return;
        
        byte[] frameBytes= frames.poll();
        updateDisplay(frameBytes);
        frames.offer(frameBytes); /* keep a circular queue */
    }
    

    @Subscribe
    public void on(ConsoleEventMainSystemChange ev) {
        mainSys = getConsole().getMainSystem();
    }
    
    @Override
    public void initLayer() {

    }
    
    @Override
    public void cleanLayer() {
        
    }

    @Override
    public boolean userControlsOpacity() { return false; }

}