package be.smals.psnextrequest.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the psnext_visible_for_responsible database table.
 * 
 */
@Entity
@Table(name="psnext_req_visible_for_responsible")
public class ReqVisibleForResponsible implements Serializable {
	private static final long serialVersionUID = 1L;

//	@EmbeddedId
//	private ReqVisibleForResponsiblePK id;

	@Column(name="IS_VISIBLE")
	private boolean isVisible;
	
	@Id
	@ManyToOne(fetch=FetchType.LAZY) //bi-directional many-to-one association to PsnextUser
	@JoinColumn(name="USER_ID_USER", nullable=false, updatable=false)
	private User user;

	@Id
	@ManyToOne(fetch=FetchType.LAZY) //bi-directional many-to-one association to PsnextRequest
	@JoinColumn(name="REQ_ID_REQ", nullable=false, updatable=false)
	private Request request;

	public ReqVisibleForResponsible() {
	}

//	public ReqVisibleForResponsiblePK getId() {
//		return this.id;
//	}
//
//	public void setId(ReqVisibleForResponsiblePK id) {
//		this.id = id;
//	}

	public boolean getIsVisible() {
		return this.isVisible;
	}

	public void setIsVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Request getRequest() {
		return this.request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

}