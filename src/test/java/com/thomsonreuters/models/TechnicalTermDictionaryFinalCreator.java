package com.thomsonreuters.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.codehaus.jettison.json.JSONObject;

public class TechnicalTermDictionaryFinalCreator {

	public TechnicalTermDictionaryFinalCreator() throws Exception {

		FileReader fr = new FileReader(
				new File(
						
						//"C:\\Users\\uc197123\\Desktop\\technicalTypeheadDict_sample.dict"));
						"C:\\Users\\uc197123\\Desktop\\technicalterm_20161215.dict"));

		BufferedReader br = new BufferedReader(fr);
		String line = "";
		StringBuilder sb = new StringBuilder();

		FileAppender appender = new FileAppender();

		int count = 0;
		
		StringBuilder buffer = new StringBuilder();
		while ((line = br.readLine()) != null) { 

			Termm term = new Termm();

			JSONObject object = new JSONObject(line);

			term.setTerm(object.getString("keyword"));
			term.setTermcount(Integer
					.parseInt(object.getString("count").trim()));
			term.setInif(Double.parseDouble(object.getString("inf").trim()));
			
			if(!term.isInclude()){
				continue;
			}

			buffer.append(term.toString() + FileAppender.nextLine);

			if (++count % 1000000 == 0) {
				
				System.out.println("Writing on file");

				appender.appendfile("d:/technicalterms_20170111.dict",
						buffer.toString());
				buffer.delete(0, buffer.length());

			} 
		}
		
		appender.appendfile("d:/technicalterms_20170111.dict",
				buffer.toString());
	}

	public class FileAppender {

		public static final String nextLine = "\r\n";

		public synchronized void appendfile(String fileName, String text) {

			try {

				makeFile(fileName);

				Files.write(Paths.get(fileName), text.getBytes("UTF8"),
						StandardOpenOption.APPEND);
			} catch (Exception e) {
				e.printStackTrace();
				// exception handling left as an exercise for the reader
			}

		}

		public void makeFile(String fileName) throws Exception {
			if (fileName != null) {
				File file = new File(fileName);
				if (file.exists()) {
					return;
				} else {
					BufferedWriter br = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(
									new File(fileName)), "UTF-8"));
					br.close();
				}
			}
		}
	}

	class Termm {
		String term;
		int termcount;
		double inif;

		public String getTerm() {
			return term;
		}

		public void setTerm(String term) {
			this.term = term;
		}

		public int getTermcount() {
			return termcount;
		}

		public void setTermcount(int termcount) {
			this.termcount = termcount;
		}

		public double getInif() {
			return inif;
		}

		public void setInif(double inif) {
			this.inif = inif;
		}

		@Override
		public String toString() {

			return "{\"keyword\":"
					+ org.codehaus.jettison.json.JSONObject.quote(term)
					+ ",\"count\":" + this.termcount + ",\"inf\":" + inif
					+ "}";

		}

		public boolean isInclude() {

			char[] data = term.toCharArray();
			StringBuilder sb = new StringBuilder();

			for (char c : data) {
				if (Character.isAlphabetic(c) || Character.isDigit(c)
						|| c == ' ') {
					continue;
				} else {
					return false;
				}
			}

			return true;
		}

	}

	public static void main(String[] args) throws Exception {
		TechnicalTermDictionaryFinalCreator data = new TechnicalTermDictionaryFinalCreator();
	}
}
