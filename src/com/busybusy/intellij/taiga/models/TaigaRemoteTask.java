package com.busybusy.intellij.taiga.models;

import com.google.gson.JsonObject;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.http.util.TextUtils;

/**
 * Created by Tjones on 5/26/15.
 */
@Tag("TaigaRemoteTask")
public class TaigaRemoteTask
{

	private String mTaskId;
	private String mRef;
	private String mProjectId;
	private String mSubject;
	private String mDescription;
	private String mUpdatedAt;
	private String mCreatedAt;
	private String mStatus;
	private String mAssignedTo;

	public TaigaRemoteTask()
	{
	}

	public TaigaRemoteTask(JsonObject suspect)
	{
		this.deserializeRemoteTask(suspect);
	}

	public String getTaskId()
	{
		return mTaskId;
	}

	public TaigaRemoteTask setTaskId(String taskId)
	{
		this.mTaskId = taskId;
		return this;
	}

	public String getRef()
	{
		return mRef;
	}

	public TaigaRemoteTask setRef(String ref)
	{
		this.mRef = ref;
		return this;
	}

	public String getProjectId()
	{
		return mProjectId;
	}

	public TaigaRemoteTask setProjectId(String projectId)
	{
		this.mProjectId = projectId;
		return this;
	}

	public String getSubject()
	{
		return mSubject;
	}

	public TaigaRemoteTask setSubject(String subject)
	{
		this.mSubject = subject;
		return this;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public TaigaRemoteTask setDescription(String description)
	{
		this.mDescription = description;
		return this;
	}

	public String getUpdatedAt()
	{
		return mUpdatedAt;
	}

	public TaigaRemoteTask setUpdatedAt(String updatedAt)
	{
		this.mUpdatedAt = updatedAt;
		return this;
	}

	public String getCreatedAt()
	{
		return mCreatedAt;
	}

	public TaigaRemoteTask setCreatedAt(String createdAt)
	{
		this.mCreatedAt = createdAt;
		return this;
	}

	public String getStatus()
	{
		return mStatus;
	}

	public TaigaRemoteTask setStatus(String status)
	{
		mStatus = status;
		return this;
	}

	public String getAssignedTo()
	{
		return mAssignedTo;
	}

	public TaigaRemoteTask setAssignedTo(String assignedTo)
	{
		mAssignedTo = assignedTo;
		return this;
	}

	public boolean isValid()
	{
		return !(TextUtils.isEmpty(mTaskId) ||
				         TextUtils.isEmpty(mRef) ||
				         TextUtils.isEmpty(mProjectId) ||
				         TextUtils.isEmpty(mUpdatedAt) ||
				         TextUtils.isEmpty(mCreatedAt) ||
				         TextUtils.isEmpty(mStatus));
	}

	private void deserializeRemoteTask(JsonObject current)
	{
		if (current.has("status"))
		{
			this.setStatus((current.get("status").isJsonPrimitive()) ? current.get("status")
			                                                                  .getAsJsonPrimitive()
			                                                                  .getAsString() : "");
		}
		if (current.has("ref"))
		{
			this.setRef((current.get("ref").isJsonPrimitive()) ? current.get("ref")
			                                                            .getAsJsonPrimitive()
			                                                            .getAsString() : "");
		}
		if (current.has("created_date"))
		{
			this.setCreatedAt((current.get("created_date").isJsonPrimitive()) ? current.get("created_date")
			                                                                           .getAsJsonPrimitive()
			                                                                           .getAsString() : "");
		}
		if (current.has("modified_date"))
		{
			this.setUpdatedAt((current.get("modified_date").isJsonPrimitive()) ? current.get("modified_date")
			                                                                            .getAsJsonPrimitive()
			                                                                            .getAsString() : "");
		}
		if (current.has("description"))
		{
			this.setDescription((current.get("description").isJsonPrimitive()) ? current.get("description")
			                                                                            .getAsJsonPrimitive()
			                                                                            .getAsString() : "");
		}
		if (current.has("subject"))
		{
			this.setSubject((current.get("subject").isJsonPrimitive()) ? current.get("subject")
			                                                                    .getAsJsonPrimitive()
			                                                                    .getAsString() : "");
		}
		if (current.has("project"))
		{
			this.setProjectId((current.get("project").isJsonPrimitive()) ? current.get("project")
			                                                                      .getAsJsonPrimitive()
			                                                                      .getAsString() : "");
		}
		if (current.has("id"))
		{
			this.setTaskId((current.get("id").isJsonPrimitive()) ? current.get("id")
			                                                              .getAsJsonPrimitive()
			                                                              .getAsString() : "");
		}
		if (current.has("assigned_to"))
		{
			this.setAssignedTo((current.get("assigned_to").isJsonPrimitive()) ? current.get("assigned_to")
			                                                                           .getAsJsonPrimitive()
			                                                                           .getAsString() : "");
		}
	}
}
