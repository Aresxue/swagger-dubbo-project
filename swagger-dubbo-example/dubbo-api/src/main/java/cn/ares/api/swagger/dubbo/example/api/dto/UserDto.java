package cn.ares.api.swagger.dubbo.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;


@Schema(description="user dto")
public class UserDto implements Serializable {

	private static final long serialVersionUID = -1169812613737118557L;
	private String id;
	@Schema(description = "user name")
	private String name;
	private String site;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

}
