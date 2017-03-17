import pt.lsts.imc.net.*
import pt.lsts.imc.*


class Globals {
	static proto = new IMCProtocol();
}

desired_lat = Math.toRadians(41)
desired_lon = Math.toRadians(-8)
desired_speed = 1.2
desired_depth = 3


pc = new PlanControl(
	opStr: 'START',
	planId: "GroovyPlan",
	arg: new Goto(
		lat: desired_lat,
		lon: desired_lon,
		z: desired_depth,
		ZUnitsStr: 'DEPTH',
		speed: desired_speed,
		speedUnitsStr: 'METERS_PS'
	)
)
//Globals.proto.setAutoConnect(".*")
while (true) {
	
	Thread.sleep(5000);
	if(vehicles_id.length > 0){
		vehicles_id.each {
			println it
			//Globals.proto.setAutoConnect(it)
			//Globals.getProto().waitFor(it,1000)
			//Globals.proto.sendMessage(it,pc)
			//Globals.proto.sendMessage(it, new Abort())

		}
		
		
	}
	else println "No avaliable vehicles."
	
	if(plans_id.length > 0)
	   plans_id.each {
	     println it
	}
    else println "No Plans."
	  
  /*  if(locs.length > 0)
        locs.each {
           println it.getId()
        }
    else
        println "No POI in the console."    */
}
