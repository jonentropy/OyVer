package org.canthack.tris.oyver;

import java.io.Serializable;

public class Vote implements Serializable{
	private static final long serialVersionUID = -5022092856758509133L;

	public static final int YAY = 0;
	public static final int MEH = 1;
	public static final int NAY = 2;

	private String url = "";
	private int numberOfAttempts = 0; //The voter will give up after this hits Voter.MAX_ATTEMPTS

	public Vote(String endpoint, int id, int vt){
		StringBuilder urlBuilder = new StringBuilder().append(endpoint);
		if(!endpoint.endsWith("/")) urlBuilder.append('/');

		urlBuilder.append("INCR").append('/').append(id).append('_');

		switch(vt){
		case YAY: 
			urlBuilder.append("yay");
			break;
		case MEH: 
			urlBuilder.append("meh");
			break;
		case NAY: 
			urlBuilder.append("nay");
			break;
		}
		
		url = urlBuilder.toString();
	}

	public String getUrl(){
		return url;
	}
	
	public int incrementAttempts(){
		numberOfAttempts++;
		return numberOfAttempts;
	}
	
	public int getNumberOfAttempts(){
		return numberOfAttempts;
	}
}
