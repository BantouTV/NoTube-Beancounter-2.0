package it.rainet.portal.cms.client.integration.lightstreamer;

import java.io.Serializable;

public class LightstreamerDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TYPE_OPENBOXES = "OB";
	public static final String TYPE_BEANCOUNTER = "BC";

	public enum Command {
		START, STOP
	};

	private String owner;
	private String body;
	private String type;


	public LightstreamerDTO(String owner, String body, String type) {
		this.owner = owner;
		this.body = body;
		this.type = type;
	}


	public String getOwner() {
		return owner;
	}


	public String getBody() {
		return body;
	}


	public String getType() {
		return type;
	}


	@Override
	public String toString() {
		return "owner = " + owner +
				"type = " + type + 
				", body = " + (body.length() > 100 ? body.substring(0, 97) + "..." : body);
	}

}