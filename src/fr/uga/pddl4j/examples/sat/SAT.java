package fr.uga.pddl4j.examples.sat;

import fr.uga.pddl4j.examples.sat.*;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.planners.SearchStrategy;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.sat4j.specs.*;
import java.io.*;
import java.util.*;

import org.sat4j.reader.DimacsReader;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.ParseFormatException;
/**
 * 
 *
 * @author D. Pellier
 * @version 4.0 - 30.11.2021
 */
@CommandLine.Command(name = "SAT",
    version = "SAT 1.0",
    description = "Solves a specified planning problem using SAT encoding.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class SAT extends AbstractPlanner {
    // Implement the isSupported method
    @Override
    public boolean isSupported(Problem problem) {
        // Implement the logic to check if the problem is supported
        return true; // or false based on the logic
    }
    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(SAT.class.getName());
	private  int LengthPlan; // the length of the plan
    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using SAT.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problemInit) {
        LOGGER.info("* Starting SAT search \n");
        // For the moment we do not decode the solution give by the SAT solver thus we keep plan null
        // but for a second version we will implement the decode part 
        Plan plan = null; 
        
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1 hour timeout
        DimacsReader reader = new DimacsReader(solver);
        PrintWriter out = new PrintWriter(System.out,true);
        
        //  Use the class EncoderDimacs that will transform the problem into a CNF file with the method encodes
        EncoderDimacs encoder = new EncoderDimacs(problemInit,this.LengthPlan);
        
        try {
        	
        	String filePathCNF = encoder.encode();
        	InputStream in = new FileInputStream(filePathCNF);
        	
            IProblem problem = reader.parseInstance(in);
            if (problem.isSatisfiable()) {
                System.out.println("Satisfiable !");
                reader.decode(problem.model(),out);
                
            } else {
                System.out.println("Unsatisfiable !");
            }
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");      
        } catch (FileNotFoundException e) {
			System.out.println("File not found, sorry!"); 
		} catch (ParseFormatException e) {
			System.out.println("Parse format error, sorry!"); 
		} catch ( IOException e) {
			System.out.println("Error!"); 
		}
        
        
        
        
        // For the moment the plan will be always null because we did not implemented the decode part of the 
        // solution given by the SAT solver.
        
        if (plan != null) {
            LOGGER.info("* SAT search succeeded\n");

        } else {
            LOGGER.info("* SAT search failed\n");
        }
        // Return the plan found or null if the search fails.
        return plan;
    }
	
	/**
     * Sets the length of the plan.
     *
     * @param length the length of the plan. The length must be greater than 0.
     * @throws IllegalArgumentException if the length is strictly less than 0.
     */
    @CommandLine.Option(names = {"-lp", "--Length"}, defaultValue = "1000",
        paramLabel = "<length>", description = "Set the length of the plan (preset 1000).")
    public void setHeuristicWeight(final int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length <= 0");
        }
        this.LengthPlan = length;
    }

    /**
     * The main method of the <code>SAT</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final SAT planner = new SAT();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
}