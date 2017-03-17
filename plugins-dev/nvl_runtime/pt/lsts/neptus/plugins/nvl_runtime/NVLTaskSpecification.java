/*
 * Copyright (c) 2004-2017 Universidade do Porto - Faculdade de Engenharia
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
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: keila
 * 09/03/2017
 */
package pt.lsts.neptus.plugins.nvl_runtime;

import java.util.ArrayList;
import java.util.List;

import nvl.TaskSpecification;
import nvl.VehicleRequirements;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PlanControl;
import pt.lsts.neptus.comm.manager.imc.ImcSystemsHolder;
import pt.lsts.neptus.types.mission.plan.PlanType;

/**
 * @author keila
 *
 */
public class NVLTaskSpecification implements TaskSpecification {
    private  IMCMessage imcmessage; //PlanControl?-> A List of them? //TODO
    private  final String [] vehicles_id; //final? can the number of vehicles for a task change?
    
    /**
     * 
     */
    public NVLTaskSpecification(NVLPayload payload) { //ImcStringDefs defs
        imcmessage = new IMCMessage();
        vehicles_id = define_vehicles(payload);
        
    }
    
    /**
     * Create a NVLTaskSpecification from Neptus PlanType
     * @param plan 
     * 
     */
    public NVLTaskSpecification(PlanType plan) {
        imcmessage = plan.asIMCPlan();
        ArrayList<String> vs = new ArrayList<>(plan.getVehicles().size());        
        plan.getVehicles().stream().forEach(v -> vs.add(v.getId()));
        vehicles_id = (String[]) vs.toArray();
    }
    
    /**
     * @param payload
     * @return 
     */
    private String[] define_vehicles(NVLPayload payload) { //Filter<Vehicle> / Vehicle Requirements
        // TODO Auto-generated method stub ImcSystemsHolder.lookupSystemVehicles();
        
        return null;
    }

    /* (non-Javadoc)
     * @see nvl.TaskSpecification#getRequirements()
     */
    @Override
    public List<VehicleRequirements> getRequirements() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nvl.TaskSpecification#toPlanControl()
     */
    @Override
    public PlanControl toPlanControl() {
        // TODO Auto-generated method stub
        return null;
    }

}
