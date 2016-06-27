package pt.lsts.neptus.plugins.mvplanning;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import pt.lsts.neptus.mp.maneuvers.FollowPath;
import pt.lsts.neptus.mp.maneuvers.Goto;
import pt.lsts.imc.PlanSpecification;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.comm.IMCUtils;
import pt.lsts.neptus.mp.Maneuver;
import pt.lsts.neptus.mp.ManeuverLocation;
import pt.lsts.neptus.mp.maneuvers.LocatedManeuver;
import pt.lsts.neptus.plugins.mvplanning.exceptions.SafePathNotFoundException;
import pt.lsts.neptus.plugins.mvplanning.interfaces.ConsoleAdapter;
import pt.lsts.neptus.plugins.mvplanning.jaxb.profiles.Profile;
import pt.lsts.neptus.plugins.mvplanning.planning.PlanTask;
import pt.lsts.neptus.plugins.mvplanning.planning.PlanTask.TASK_TYPE;
import pt.lsts.neptus.plugins.mvplanning.planning.algorithm.CoverageAreaFactory;
import pt.lsts.neptus.plugins.mvplanning.planning.mapdecomposition.GridArea;
import pt.lsts.neptus.plugins.mvplanning.utils.MvPlanningUtils;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.mission.GraphType;
import pt.lsts.neptus.types.mission.plan.PlanType;
import pt.lsts.neptus.util.NameNormalizer;

public class PlanGenerator {
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private static GridArea operationalArea;
    private Thread opAreaThread; // Thread used to create the operational area

    private ConsoleAdapter console;
    /* Map decomposition needed for some algorithms, e.g, A-star */

    public PlanGenerator(ConsoleAdapter console) {
        this.console = console;
    }

    public PlanGenerator() {

    }

    public void setOperationalArea(GridArea opArea) {
        RW_LOCK.writeLock().lock();
        operationalArea = opArea;
        RW_LOCK.writeLock().unlock();
    }

    public void updateOperationalArea() {
        RW_LOCK.writeLock().lock();
        operationalArea.updateCellsObstacles();
        RW_LOCK.writeLock().unlock();
    }

    public void generatePlan(Profile planProfile, Object obj) {
/*        if(obj.getClass().getSimpleName().equals("PlanType")) {
            PlanType pType = (PlanType) obj;
        }
        else {
        }*/
    }

    /**
     * Given an area generate one or more plans to cover it
     * */
    public List<PlanType> generateCoverageArea(Profile planProfile, GridArea areaToCover) {
        String id = "coverage_" + NameNormalizer.getRandomID();

        CoverageAreaFactory covArea = new CoverageAreaFactory(id, planProfile, areaToCover, console.getMission());
        List<PlanType> plans = covArea.asPlanType();

        if (plans.isEmpty())
            NeptusLog.pub().warn("No plans were generated");

        return covArea.asPlanType();
    }


    /**
     *  Generate a plan to visit the given location
     * */
    public PlanType generateVisitPoint(Profile planProfile, LocationType pointLoc) {
        String id = "visit_" + NameNormalizer.getRandomID();

        PlanType plan = new PlanType(console.getMission());
        plan.setId(id);

        Goto point = (Goto) MvPlanningUtils.buildManeuver(planProfile, pointLoc, PlanTask.TASK_TYPE.VISIT_POINT);
        point.setId("visit_point");

        plan.getGraph().addManeuver(point);

        return plan;
    }

    /**
     * Adds a safe path from the start location to the first
     * waypoint of the given plan, and from the last waypoint
     * of the plan to the end location.
     * @throws SafePathNotFoundException
     * */
    public static PlanSpecification closePlan(PlanTask ptask, LocationType start, LocationType end) throws SafePathNotFoundException {
        /* current plan */
        PlanType plan = ptask.asPlanType().clonePlan();
        GraphType planGraph = plan.getGraph();
        Maneuver firstMan = planGraph.getAllManeuvers()[0];
        ManeuverLocation planFirstLocation = ((LocatedManeuver) firstMan).getManeuverLocation();
        ManeuverLocation planLastLocation ;

        /* FollowPath is as just one maneuver, so first and last maneuver of the graph are the same */
        if(ptask.getTaskType() == TASK_TYPE.COVERAGE_AREA)
            planLastLocation = ((FollowPath) firstMan).getEndLocation();
        else
            planLastLocation = ((LocatedManeuver) planGraph.getLastManeuver()).getManeuverLocation();

        FollowPath initialFollowPath = buildSafePath(start, planFirstLocation);
        FollowPath endFollowPath = buildSafePath(planLastLocation, end);

        /* set new initial maneuver */
        String currInitialManId = plan.getGraph().getInitialManeuverId();
        plan.getGraph().addManeuver(initialFollowPath);
        plan.getGraph().setInitialManeuver(initialFollowPath.id);
        plan.getGraph().addTransition(initialFollowPath.id, currInitialManId, "ManeuverIsDone");

        plan.getGraph().addManeuverAtEnd(endFollowPath);

        return (PlanSpecification) IMCUtils.generatePlanSpecification(plan);
    }

    private static FollowPath buildSafePath(LocationType start, LocationType end) throws SafePathNotFoundException {
        RW_LOCK.readLock().lock();

        List<ManeuverLocation> safePath = operationalArea.getShortestPath(start, end);

        RW_LOCK.readLock().unlock();

        FollowPath safeFollowPath = new FollowPath();
        Vector<double[]> offsets = new Vector<>();
        for(ManeuverLocation loc : safePath) {
            double[] newPoint = new double[4];
            double[] pOffsets = loc.getOffsetFrom(start);

            newPoint[0] = pOffsets[0];
            newPoint[1] = pOffsets[1];
            newPoint[2] = pOffsets[2];
            newPoint[3] = 0;

            offsets.add(newPoint);
        }

        /* FIXME this is repeated code */
        ManeuverLocation manLoc = new ManeuverLocation(start);
        manLoc.setZ(end.getAllZ());
        /* TODO set according to profile's parameters */
        manLoc.setZUnits(ManeuverLocation.Z_UNITS.DEPTH);

        safeFollowPath.setOffsets(offsets);
        /* TODO set according to plan profile */
        safeFollowPath.setSpeed(1.3);
        safeFollowPath.setSpeedUnits("m/s");
        safeFollowPath.setManeuverLocation(manLoc);

        return safeFollowPath;
    }
}
