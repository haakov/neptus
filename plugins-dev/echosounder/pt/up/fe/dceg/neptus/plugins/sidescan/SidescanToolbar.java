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
 * Author: jqcorreia
 * Mar 15, 2013
 */
package pt.up.fe.dceg.neptus.plugins.sidescan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import pt.up.fe.dceg.neptus.gui.PropertiesEditor;
import pt.up.fe.dceg.neptus.plugins.sidescan.SidescanPanel.InteractionMode;

/**
 * @author jqcorreia
 *
 */
public class SidescanToolbar extends JToolBar implements ActionListener {
    private static final long serialVersionUID = 1L;

    SidescanPanel panel;
    
    ButtonGroup bgroup = new ButtonGroup();
    JToggleButton btnMeasure = new JToggleButton("Measure");
    JToggleButton btnInfo = new JToggleButton("Info");
    JToggleButton btnZoom = new JToggleButton("Zoom");
    JToggleButton btnMark = new JToggleButton("Mark");
    
    JButton btnConfig = new JButton(new AbstractAction("Config") {
        private static final long serialVersionUID = -878895322319699542L;

        @Override
        public void actionPerformed(ActionEvent e) {
            PropertiesEditor.editProperties(panel.config,
                    SwingUtilities.getWindowAncestor(panel), true);
        }
    });
    
    public SidescanToolbar(SidescanPanel panel) {
        super();
        this.panel = panel;
        buildToolbar();
    }    
    private void buildToolbar() {
        bgroup.add(btnInfo);
        bgroup.add(btnZoom);
        bgroup.add(btnMeasure);
        bgroup.add(btnMark);
        add(btnInfo);
        add(btnZoom);
        add(btnMeasure);
        add(btnMark);
        
        addSeparator();
        add(btnConfig);
        
        btnInfo.addActionListener(this);
        btnZoom.addActionListener(this);
        btnMeasure.addActionListener(this);
        btnMark.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SidescanPanel.InteractionMode imode = SidescanPanel.InteractionMode.NONE;
        
        if(btnInfo.isSelected())
            imode = InteractionMode.INFO;
        if(btnZoom.isSelected())
            imode = InteractionMode.ZOOM;
        if(btnMark.isSelected())
            imode = InteractionMode.MARK;
        if(btnMeasure.isSelected())
            imode = InteractionMode.MEASURE;
        
        panel.setInteractionMode(imode);
    }
    
}
