/*
 * Converts duplications to insertions
 * Usage: java DuplicationsToInsertions input_vcf reference_genome output_vcf
 */
import java.util.*;
import java.io.*;
public class DuplicationsToInsertions {
	static String inputFile = "/home/mkirsche/crossstitch/ENC0003.spes.rck.vcf";
	static String genomeFile = "/home/mkirsche/references/genome.fa";
	static String outputFile = "/home/mkirsche/crossstitch/ENC0003.spes.rck.no_dups.vcf";
	public static void main(String[] args) throws Exception
	{
		if(args.length != 3)
		{
			System.out.println("Usage: java DuplicationsToInsertions input_vcf reference_genome output_vcf");
			return;
		}
		else
		{
			inputFile = args[0];
			genomeFile = args[1];
			outputFile = args[2];
		}
		
		convertFile(inputFile, genomeFile, outputFile);
	}
	
	static void convertFile(String inputFile, String genomeFile, String outputFile) throws Exception
	{
		Scanner input = new Scanner(new FileInputStream(new File(inputFile)));
		
		GenomeQuery gq = new GenomeQuery(genomeFile);
		
		PrintWriter out = new PrintWriter(new File(outputFile));
		
		VcfHeader header = new VcfHeader();
		ArrayList<VcfEntry> entries = new ArrayList<VcfEntry>();
		
		int countDup = 0;
		
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.startsWith("#"))
			{
				header.addLine(line);
			}
			else if(line.length() > 0)
			{
				VcfEntry ve = new VcfEntry(line);
				if(ve.getType().equals("DUP"))
				{
					countDup++;
					
					long start = ve.getPos(), end = Long.parseLong(ve.getInfo("END"));
					int length = ve.getLength();
					long nstart = start + length, nend = nstart+1;

					if(ve.getAlt().equals("<DUP>"))
					{
						String seq = gq.genomeSubstring("chr" + ve.getChromosome(), start, end-1);
						
						if(length < 100000)
						{
							ve.setRef(seq.charAt(seq.length()-1)+"");
							ve.setAlt(seq.charAt(seq.length()-1)+seq);
						}
						else
						{
							ve.setRef(".");
							ve.setAlt("<INS>");
						}
						ve.setInfo("END", nend+"");
						ve.setPos(nstart);
						ve.setInfo("OLDTYPE", "DUP");
					}
					else
					{
						//System.out.println(line);
						ve.setInfo("OLDTYPE", "DUP");
					}
					ve.setType("INS");
					
				}
				else
				{
					ve.setInfo("OLDTYPE", ve.getType());
				}
				entries.add(ve);
			}
		}
		
		System.out.println("Number of duplications converted to insertions: " + countDup + " out of " + entries.size() + " total variants");
		
		header.addInfoField("OLDTYPE", "1", "String", "");
		header.print(out);
		
		for(VcfEntry ve : entries)
		{
			out.println(ve);
		}
		
		input.close();
		out.close();
	}
}