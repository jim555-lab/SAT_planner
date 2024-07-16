
package fr.uga.pddl4j.examples.sat;

import fr.uga.pddl4j.problem.Problem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.InitialState;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.problem.operator.Effect;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.PrintWriter;
import java.io.FileWriter;

import java.io.*;
import java.util.*;

public class EncoderDimacs {
	
	private Problem problem;
	private int LengthPlan;
	
    // Constructor that accepts a Problem object
    public EncoderDimacs(Problem problem, int LengthPlan) {
        this.problem = problem;
		this.LengthPlan = LengthPlan;
		
    }
    
    public String encode(){
		
		// So our plan will have a number of step equal to this.LengthPlan
		// For each step we have NbFluents fluents and NbAction actions possible 
		// All the fluents indices: 1 2 ... NbFluents NbFluents+1 ... LengthPlan*NbFluents 
		// Then we have the actions indices: LengthPlan*NbFluents+1 LengthPlan*NbFluents+2 ... LengthPlan*NbFluents+NbAction LengthPlan*NbFluents+NbAction+1 ... LengthPlan*NbFluents+NbAction*LengthPlan
		
		List<Fluent> fluent = this.problem.getFluents();
		int NbFluents = fluent.size();
		System.out.println(NbFluents);
		int NbClauses = 0;
		
		InitialState init = problem.getInitialState();
		BitVector PosFluInit = init.getPositiveFluents();
		System.out.println(PosFluInit);
		int LengthPosFluInit = PosFluInit.length();
		
		Condition goal = problem.getGoal();
		BitVector PosFluGoal = goal.getPositiveFluents();
		int LengthPosFluGoal = PosFluGoal.length();
		System.out.println(PosFluGoal);
		
		List<Action> act = problem.getActions();
		int NbAction = act.size()+1; // +1 as we are adding the action that is doing nothing.
		
		String filePath = "cnf.cnf";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Perform the encoding of the problem into CNF format
			
			// The first line of cnf file is : p cnf <variables> <clauses>
			// for the moment we will put 2 for the number of clauses and at the end of the editing of the file we will change it the true value
			
			int NbVar = NbFluents*this.LengthPlan+NbAction*this.LengthPlan;
			writer.println("c");
			writer.println("p cnf "+NbVar+" 2");
			
			// First we have the initial state 
			
			for (int i = 1; i < NbFluents+1; i++) {
				if (i <= LengthPosFluInit-1){
					if (PosFluInit.get(i)){
						writer.println(i+" "+0);
						NbClauses += 1;
					}
					else{
						writer.println(-i+" "+0);
						NbClauses += 1;
					}
				}
				else{
					writer.println(-i+" "+0);
					NbClauses += 1;
				}
			}
			System.out.println("NbClauses end init: " + NbClauses);
			// Then we have the goal state 

			for (int i = 1; i < NbFluents+1; i++) {
				if (i <= LengthPosFluGoal-1){
					if (PosFluGoal.get(i)){
						writer.println((i+NbFluents*(this.LengthPlan-1))+" "+0);
						NbClauses += 1;
					}
					else{
						writer.println((-i-NbFluents*(this.LengthPlan-1))+" "+0);
						NbClauses += 1;
					}
				}
				else{
					writer.println((-i-NbFluents*(this.LengthPlan-1))+" "+0);
					NbClauses += 1;
				}
			}
			System.out.println("NbClauses end goal: " + NbClauses);
			
			// Then we have the actions with their effects and their precondition 
			int IndexLoopAct = 0;
			int len = 0;
			int ActionIndex = 0; // it is the number of the action as a proposition in the cnf file  
			int FluentIndex = 0; // it is the number of the fluent in the cnf file 
			for (int i = 1; i < LengthPlan; i++) {
				IndexLoopAct = 0;
				
				for (Action a : act){
					IndexLoopAct += 1;
					ActionIndex = -(this.LengthPlan*NbFluents+NbAction*(i-1)+IndexLoopAct);
					List<ConditionalEffect> ConditionalEffects = a.getConditionalEffects();

		
					for (ConditionalEffect c: ConditionalEffects){
						Effect eff = c.getEffect();
						BitVector PositiveFluentEffect = eff.getPositiveFluents();
						BitVector NegativeFluentEffect = eff.getNegativeFluents();
						
						len = PositiveFluentEffect.length();
						for (int k=1; k<len;k++ ){
							if(PositiveFluentEffect.get(k)){
								FluentIndex = -k-NbFluents*i;
								writer.println(ActionIndex+" "+FluentIndex+" 0");
								NbClauses +=1;
							}
						}
							
						len = NegativeFluentEffect.length();
						for (int k=1; k<len;k++ ){
							if(NegativeFluentEffect.get(k)){
								FluentIndex = k+NbFluents*i;
								writer.println(ActionIndex+" "+FluentIndex+" 0");
								NbClauses += 1;
							}
						}
					}
					
					Condition PreCondition = a.getPrecondition();
					BitVector PositiveFluentPC = PreCondition.getPositiveFluents();
					BitVector NegativeFluentPC = PreCondition.getNegativeFluents();
					
					len = PositiveFluentPC.length();
					for (int k=1; k<len;k++ ){
						if(PositiveFluentPC.get(k)){
							FluentIndex = -k-NbFluents*(i-1);
							writer.println(ActionIndex+" "+FluentIndex+" 0");
							NbClauses +=1 ;
						}
					}
					
					len = NegativeFluentPC.length();
					for (int k=1; k<len;k++ ){
						if(NegativeFluentPC.get(k)){
							FluentIndex = k+NbFluents*(i-1);
							writer.println(ActionIndex+" "+FluentIndex+" 0");
							NbClauses +=1 ;
						}
					}				
				}
			}
			
			
			
			// Then we have the state transition
			
			String PartClausePosEffect = ""; // will contain the different action indices that have has positive effect the fluent that is being processed 
			String PartClauseNegEffect = ""; // will contain the different action indices that have has negative effect the fluent that is being processed 
			for (int i = 1; i < this.LengthPlan; i++) {
				
				for (int j = 1; j < NbFluents+1; j++){
					IndexLoopAct = 0;
					PartClausePosEffect = "";
					PartClauseNegEffect = "";
					for (Action a : act){
						IndexLoopAct += 1;
						ActionIndex = this.LengthPlan*NbFluents+NbAction*(i-1)+IndexLoopAct;
						List<ConditionalEffect> ConditionalEffects = a.getConditionalEffects();

						for (ConditionalEffect c: ConditionalEffects){
							Effect eff = c.getEffect();
							BitVector PositiveFluentEffect = eff.getPositiveFluents();
							BitVector NegativeFluentEffect = eff.getNegativeFluents();
							
							if(PositiveFluentEffect.get(j)){
								PartClausePosEffect = PartClausePosEffect + ActionIndex+ " ";
							}	
							if(NegativeFluentEffect.get(j)){
								PartClauseNegEffect = PartClauseNegEffect + ActionIndex+ " ";
							}	
						}
					}
					if (PartClausePosEffect != ""){
						writer.println((-NbFluents*(this.LengthPlan-1)-j) + " "+(NbFluents*(this.LengthPlan)+j)+" "+PartClausePosEffect+ " 0");
						NbClauses +=1;
					}
					if (PartClauseNegEffect != ""){
						writer.println((NbFluents*(this.LengthPlan-1)+j) + " "+(-NbFluents*(this.LengthPlan)-j)+" "+PartClauseNegEffect+ " 0");
						NbClauses +=1;
					}
				}
			}


			// Finally we have the Action disjunctions
			// NB : The "null" actions appears in this section and it is the only section they appear in. 
			
			int ActionIndexI = 0; // it is the number of the action for the first loop as a proposition in the cnf file  
			int ActionIndexJ = 0; // it is the number of the action for the second loop as a proposition in the cnf file  
			
			for (int i = 1; i < this.LengthPlan; i++) {
				for (int acti = 1;acti < NbAction+1;acti++){
					for (int actj = 1; actj < NbAction+1;actj++){
						if (acti != actj){
							ActionIndexI = -(this.LengthPlan*NbFluents+NbAction*(i-1)+acti);
							ActionIndexJ = -(this.LengthPlan*NbFluents+NbAction*(i-1)+actj);
							writer.println(ActionIndexI+" "+ActionIndexJ+" 0");
							NbClauses += 1;
						}
					}
				}
			}
			
		}catch (IOException e) {
					e.printStackTrace();
				}
		
		// We edit the cnf file to replace the number of clauses 
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}catch (IOException e) {
					e.printStackTrace();
		}

		// We modify the first line
		if (lines.size() > 1) {
			String header = lines.get(1);
			String[] parts = header.split(" ");
			if (parts.length > 3) {
				parts[3] = Integer.toString(NbClauses);  
				lines.set(1, String.join(" ", parts));
			}
		}

		// We write the modified content back to the file
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			for (String line : lines) {
				writer.println(line);
			}
		}catch (IOException e) {
					e.printStackTrace();
		}
	return filePath;
	}
}
