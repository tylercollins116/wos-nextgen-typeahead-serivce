package com.thomsonreuters.models;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DeleteTest {
	
	
	
	
	private static List<String> getValuesToIndex() {
		List<String> list = new ArrayList<String>();
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA IULIA UNIV\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA JULIA\",\"count\":13,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA LULIA\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIVERSITY ALBA IULIA\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE UNIV\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE UNIV ALBA JULIA\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1918\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1ST DECEMBER 1918 UNIV ALBA IULIA\",\"count\":12,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA IULIA\",\"count\":180,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA IULIA UNIV\",\"count\":3,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBER 1ST UNIV ALBA IULIA\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE 1918 UNIV\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE 1918 UNIV ALBA JULIA\",\"count\":3,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");

		return list;
	}
	
	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		List<String> datas = getValuesToIndex();
		 for (String line : datas) {
		   baos.write(line.getBytes());
		   baos.write("\r".getBytes());
		 }

		 byte[] bytes = baos.toByteArray();
		 

		 InputStream is = new ByteArrayInputStream(bytes);
		 
		 
		 BufferedReader br = new BufferedReader(new InputStreamReader(is));
		 
		 String line=null;
		 
		 while((line=br.readLine())!=null){
			 System.out.println(line);
		 }

	}

}
