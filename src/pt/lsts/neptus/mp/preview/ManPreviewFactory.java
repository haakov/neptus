/*
 * Copyright (c) 2004-2016 Universidade do Porto - Faculdade de Engenharia
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
 * Oct 11, 2011
 */
package pt.lsts.neptus.mp.preview;

import pt.lsts.neptus.mp.Maneuver;
import pt.lsts.neptus.mp.SystemPositionAndAttitude;
import pt.lsts.neptus.mp.maneuvers.FollowTrajectory;
import pt.lsts.neptus.mp.maneuvers.Launch;

/**
 * @author zp
 *
 */
public class ManPreviewFactory {

    public static IManeuverPreview<?> getPreview(Maneuver maneuver, String vehicleId, SystemPositionAndAttitude state, Object manState) {
        if (maneuver == null)
            return null;

        if (FollowTrajectory.class.isAssignableFrom(maneuver.getClass())) {
            FollowTrajectoryPreview prev = new FollowTrajectoryPreview();
            prev.init(vehicleId, (FollowTrajectory) maneuver, state, manState);
            return prev;
        }
        else if (Launch.class.isAssignableFrom(maneuver.getClass())) {
            GotoPreview prev = new GotoPreview();
            prev.init(vehicleId, (Launch) maneuver, state, manState);
            return prev;
        }

        String pkn = maneuver.getClass().getPackage().getName();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Class<?> prevClass = null;
        try {
            prevClass = cl.loadClass(pkn.replaceAll("\\.[A-Za-z0-9]+$", "") + ".preview."
                    + maneuver.getClass().getSimpleName() + "Preview");
        }
        catch (ClassNotFoundException e) {
            try {
                prevClass = cl.loadClass(pkn + "." + maneuver.getClass().getSimpleName() + "Preview");
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (prevClass != null) {
            try {
                @SuppressWarnings("unchecked")
                IManeuverPreview<Maneuver> prevG = (IManeuverPreview<Maneuver>) prevClass.newInstance();
                prevG.init(vehicleId, maneuver, state, manState);
                return prevG;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }    
}


