package pt.lsts.neptus.plugins.ho4;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.Vector;

import com.google.common.eventbus.Subscribe;

import pt.lsts.imc.IMCAddressResolver;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PathControlState;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.PlanDBInformation;
import pt.lsts.imc.Rpm;
import pt.lsts.imc.Temperature;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.console.ConsoleLayer;
import pt.lsts.neptus.console.events.ConsoleEventMainSystemChange;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.PluginDescription.CATEGORY;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.vehicle.VehicleType;
import pt.lsts.neptus.types.vehicle.VehiclesHolder;


@PluginDescription(author="Manuel R.", category=CATEGORY.UNSORTED, name="Myplugin", version = "0.8", description = "This is my first plugin")
public class MyFirstPlugin extends ConsoleLayer {

    private VehicleType vehicle;
    private IMCAddressResolver res = new IMCAddressResolver();
    private int mainvehicleId;
    private short rpmVal = -1;
    private double tempVal = Double.MAX_VALUE;
    private LocationType loc = null;
    private String plans = "no plans yet";

    public MyFirstPlugin() {
        NeptusLog.pub().info("This is my first plugin");
    }
    
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        super.paint(g, renderer);
        
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setColor(Color.BLACK);
        StringBuilder str0 = new StringBuilder();
        str0.append("Selected Vehicle: ");
        str0.append(getConsole().getMainSystem());
        g2.drawString(str0.toString(), 15, 15);
 
        StringBuilder str1 = new StringBuilder();
        str1.append("RPM: ");
        if (rpmVal != -1)
            str1.append(rpmVal);
        else
            str1.append("N/A");
            
        g2.drawString(str1.toString(), 15, 30);
        
        DecimalFormat df2 = new DecimalFormat("#.##");
        StringBuilder str2 = new StringBuilder();
        str2.append("Temp: ");
        if (tempVal != Double.MAX_VALUE)
            str2.append(df2.format(tempVal));
        else
            str2.append("N/A");
        
        g2.drawString(str2.toString(), 15, 45);
        
        //home-work
        if (loc != null)
        {
            g2.drawString("Target Coord.: "+ Double.toString(loc.getLatitudeDegs()) + ", " +Double.toString(loc.getLongitudeDegs()), 15, 60);
        }
        g2.drawString(plans, 15, 75);
        /*
        if (plans != "no plans")
        {
            g2.drawString(plans, 15, 75);
        }
        */
        g2.dispose();
    }
    
    @Subscribe
    public void mainVehicleChangeNotification(ConsoleEventMainSystemChange evt) {
        rpmVal = -1;
        tempVal = Double.MAX_VALUE;
        System.out.println("Selected vehicle is: " + evt.getCurrent());
        vehicle = VehiclesHolder.getVehicleById(evt.getCurrent());
        
        mainvehicleId = res.resolve(vehicle.getId());
        System.out.println(vehicle.toString() + " "+ mainvehicleId);
    }
    
    @Subscribe
    public void consume(Rpm rpm) {
        if (rpm.getSrc() == mainvehicleId) {
            rpmVal = rpm.getValue();
        }
    }
    
    
    //HOME-WORK
    @Subscribe
    public void consume(PathControlState pcs) {
        if (pcs.getSrc() == mainvehicleId) {
            loc = new LocationType(Math.toDegrees(pcs.getEndLat()), Math.toDegrees(pcs.getEndLon()));
        }
    }
    
    @Subscribe
    public void consume(PlanDB pdb) {
        System.out.println("====> pdb " + pdb.getSrc() + " main " + mainvehicleId);
        if (pdb.getSrc() == mainvehicleId) {
            int plan_count = pdb.getArg().get("plan_count", int.class);
            //System.out.println("====> plan " + pdb.getArg().get("plans_info", PlanDB.class)); //[0].getAsString()); //    .get("plans_info", PlanDBInformation.class)[0].getAsString());
            Vector<IMCMessage> plans_ = pdb.getArg().getMessageList("plans_info");
            plans = "";
            for (int i = 0; i < plan_count; i++) {
                plans = plans + plans_.get(i).get("plan_id", String.class) + ",";
            }
            
        }
    }

    @Subscribe
    public void consume(Temperature temp) {
        if (temp.getSrc() == mainvehicleId) {
            tempVal = temp.getValue();
        }
    }

    @Override
    public boolean userControlsOpacity() {
        return false;
    }

    @Override
    public void initLayer() {
        
    }

    @Override
    public void cleanLayer() {
        
    }

}
