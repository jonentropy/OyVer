package org.canthack.tris.oyver.model.json;

import com.google.gson.annotations.SerializedName;

public class Talk {
	@SerializedName("id")
	public long id;
	
	@SerializedName("title")
	public String title;
	
	@SerializedName("email")
	public String emailAddress;
}
