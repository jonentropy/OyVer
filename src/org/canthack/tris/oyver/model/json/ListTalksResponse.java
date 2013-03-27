package org.canthack.tris.oyver.model.json;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class ListTalksResponse {
	@SerializedName("talks")
	public ArrayList<Talk> talks;
}
