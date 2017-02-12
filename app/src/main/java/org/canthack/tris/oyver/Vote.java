package org.canthack.tris.oyver;

import java.io.Serializable;

class Vote implements Serializable{
	private static final long serialVersionUID = -5022092856758509133L;

	static final int YAY = 0;
	static final int MEH = 1;
	static final int NAY = 2;

	private String url = "";

	private String talkName;

	private int voteType;

	Vote(String endpoint, int id, String name, int vt){
		this.talkName = name;
		this.voteType = vt;
		
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

	String getUrl(){
		return url;
	}

    String getTalkName() {
        return talkName;
    }

	int getVoteType() {
		return voteType;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder().append(talkName).append(" ");
		switch(this.voteType){
		case YAY: 
			builder.append(":)");
			break;
		case MEH: 
			builder.append(":|");
			break;
		case NAY: 
			builder.append(":(");
			break;
		}
		
		builder.append("\n").append(url);
		return builder.toString();
	}
}
