package fr.uga.pddl4j.examples;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.parser.Message;
import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.InitialState;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.util.BitVector;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.io.*;
import java.util.*;

import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.problem.operator.Effect;
/**
 * The class is an example class. It shows how to use the library to create to ground planning problem.
 *
 * @author D. Pellier
 * @version 4.0 - 06.12.2021
 */
public class ProblemInstantiationExample3 {

    /**
     * The main method the class. The first argument must be the path to the PDDL domain description and the second
     * argument the path to the PDDL problem description.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {

        // Checks the number of arguments from the command line
        if (args.length != 2) {
            System.out.println("Invalid command line");
            return;
        }

        try {
            // Creates an instance of the PDDL parser
            final Parser parser = new Parser();
            // Parses the domain and the problem files.
            final DefaultParsedProblem parsedProblem = parser.parse(args[0], args[1]);
            // Gets the error manager of the parser
            final ErrorManager errorManager = parser.getErrorManager();
            // Checks if the error manager contains errors
            if (!errorManager.isEmpty()) {
                // Prints the errors
                for (Message m : errorManager.getMessages()) {
                    System.out.println(m.toString());
                }
            } else {
                // Prints that the domain and the problem were successfully parsed
                System.out.print("\nparsing domain file \"" + args[0] + "\" done successfully");
                System.out.print("\nparsing problem file \"" + args[1] + "\" done successfully\n\n");
                // Create a problem
                final Problem problem = new DefaultProblem(parsedProblem);
                // Instantiate the planning problem
                problem.instantiate();
                // Print the list of actions of the instantiated problem
                for (Action a : problem.getActions()) {
                    System.out.println(problem.toString(a));
					System.out.println(a);
                }
				System.out.println(problem.getActions());
				
				for (Fluent g : problem.getFluents()) {
                    System.out.println(problem.toString(g));
					System.out.println(g);
                }

	
				String filePath = "cnf";
				try {
					int nb_lines = 0;
					try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
						
						int m = 3; // nb of element in the plan 
						writer.println("c This is a CNF file generated by EncoderDimacs");
						writer.println("p cnf "+1*m+" 2");
						
						List<Action> act = problem.getActions();
						int nb_action = act.size();
						
						List<Fluent> fluent = problem.getFluents();
						int NbFluents = fluent.size();
						int numero_action = 0;
						int len = 0;
						int inter = 0;
						int inter_act = 0;
						
						String PartClausePosEffect = "";
						String PartClauseNegEffect = "";
						for (int i = 1; i < m; i++) {
							
							for (int j = 1; j < NbFluents+1; j++){
								numero_action = 0;
								PartClausePosEffect = "";
								PartClauseNegEffect = "";
								for (Action a : act){
									numero_action += 1;
									inter_act = -(m*NbFluents+nb_action*(i-1)+numero_action);
									nb_lines += 1;
									List<ConditionalEffect> ce = a.getConditionalEffects();

						
									for (ConditionalEffect c: ce){
										Effect eff = c.getEffect();
										BitVector pf = eff.getPositiveFluents();
										BitVector nf = eff.getNegativeFluents();
										
										if(pf.get(j)){
											PartClausePosEffect = PartClausePosEffect + inter_act+ " ";
										}	
										if(nf.get(j)){
											PartClauseNegEffect = PartClauseNegEffect + inter_act+ " ";
										}
										
										
									}
									
								}
							if (PartClausePosEffect != ""){
								writer.println((-NbFluents*(m-1)-j) + " "+(NbFluents*(m)+j)+" "+PartClausePosEffect+ " 0");
								nb_lines +=1;
							}
							if (PartClauseNegEffect != ""){
								writer.println((NbFluents*(m-1)+j) + " "+(-NbFluents*(m)-j)+" "+PartClauseNegEffect+ " 0");
								nb_lines +=1;
							}
							
							}


	
						}


						
						
						
					}
					
					
					List<String> lines = new ArrayList<>();
					try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
						String line;
						while ((line = reader.readLine()) != null) {
							lines.add(line);
						}
					}

					// Step 3: Modify the first line
					if (lines.size() > 1) {
						String header = lines.get(1);
						String[] parts = header.split(" ");
						if (parts.length > 3) {
							parts[3] = Integer.toString(nb_lines);  // Change the "3" to "100"
							lines.set(1, String.join(" ", parts));
						}
					}

					// Step 4: Write the modified content back to the file
					try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
						for (String line : lines) {
							writer.println(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			
				
				
				
            }
            // This exception could happen if the domain or the problem does not exist
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}