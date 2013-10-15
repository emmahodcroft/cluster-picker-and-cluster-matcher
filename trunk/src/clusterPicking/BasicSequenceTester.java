package clusterPicking;

/**
 * class to test the different types of sequence matching
 * 
 *  Copyright (C) 2013  Samantha Lycett
 *  
 *  This file is part of ClusterPicker.
 *
 *  ClusterPicker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  ClusterPicker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ClusterPicker.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Samantha Lycett
 * @created 27 Sept 2013
 * @version 27 Sept 2013
 */
public class BasicSequenceTester {

	BasicSequence seq1;
	BasicSequence seq2;
	boolean verbose = false;
	
	
	public BasicSequenceTester() {
		
	}
	
	////////////////////////////////////////////////////////////////////
	
	public void test1() {
		System.out.println("Test 1 : Difference between sequences with only valid characters, A, C, T, G");
		seq1 = new BasicSequence("Seq1","AAAAAAAA");
		seq2 = new BasicSequence("Seq2","ACTGACTG");
		differenceTest();
	}
	
	
	public void test2() {
		System.out.println("Test 2 : Difference between sequences with some gap characters");
		seq1 = new BasicSequence("Seq1","AAAAAAAA");
		seq2 = new BasicSequence("Seq2","A-~NACTG");
		differenceTest();
	}
	
	public void test3() {
		System.out.println("Test 3: Difference between sequences with some ambiguity characters");
		seq1 = new BasicSequence("Seq1","AAAAAAAAAAAA");
		seq2 = new BasicSequence("Seq2","AMRWSYKVHDBN");
		differenceTest();
		
		seq1 = new BasicSequence("Seq1","CCCCCCCCCCCC");
		differenceTest();
		
		seq1 = new BasicSequence("Seq1","GGGGGGGGGGGG");
		differenceTest();
		
		seq1 = new BasicSequence("Seq1","TTTTTTTTTTTT");
		differenceTest();
		
		seq1 = new BasicSequence("Seq1","MRWSYKVHDBNA");
		differenceTest();
		
	}
	
	private void differenceTest() {
		System.out.println("-- Difference between sequences --");
		System.out.println(seq1.header()+"\t"+new String(seq1.charSeq())+"\tlength="+seq1.charSeq().length);
		System.out.println(seq2.header()+"\t"+new String(seq2.charSeq())+"\tlength="+seq2.charSeq().length);
		
		
		if (!verbose) {
			System.out.println("DifferenceType\tcounts\t1-2\t2-1\tfracts\t1-2\t2-1");
		}
		
		
		String[] diffTypes = {"abs","gap","ambiguity","valid"};
		
		for (String dt : diffTypes) {
		
			BasicSequence.setDifferenceType(dt);
			int diff_11			= seq1.difference(seq1);
			int diff_22			= seq2.difference(seq2);
			int diff_12 		= seq1.difference(seq2);
			int diff_21 		= seq2.difference(seq1);
		
			double fractDiff_11 = seq1.fractDifference(seq1);
			double fractDiff_22 = seq2.fractDifference(seq2);
			double fractDiff_12 = seq1.fractDifference(seq2);
			double fractDiff_21 = seq2.fractDifference(seq1);
			
			if (verbose) {
				System.out.println("");
				System.out.println("-- Results for Difference Type = "+dt+" --");
				System.out.println("\tCount differences:");
				System.out.println("\tSeq1 vs Seq1:\t"+diff_11);
				System.out.println("\tSeq1 vs Seq2:\t"+diff_12);
				System.out.println("\tSeq2 vs Seq1:\t"+diff_21);
				System.out.println("\tSeq2 vs Seq2:\t"+diff_22);
				System.out.println("");
				System.out.println("\tFraction differences:");
				System.out.println("\tSeq1 vs Seq1:\t"+fractDiff_11);
				System.out.println("\tSeq1 vs Seq2:\t"+fractDiff_12);
				System.out.println("\tSeq2 vs Seq1:\t"+fractDiff_21);
				System.out.println("\tSeq2 vs Seq2:\t"+fractDiff_22);
				System.out.println("");
			} else {
				String ddt = dt;
				while (ddt.length() < 10) {
					ddt = ddt+" ";
				}
				System.out.println(ddt+"\tcounts:\t"+diff_12+"\t"+diff_21+"\tfracts:\t"+fractDiff_12+"\t"+fractDiff_21);
			}
		}
		System.out.println();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public static void run(String[] args) {
		BasicSequenceTester tester = new BasicSequenceTester();
		tester.test1();
		tester.test2();
		tester.test3();
	}
	
	public static void main(String[] args) {
		run(args);
	}
	
}
