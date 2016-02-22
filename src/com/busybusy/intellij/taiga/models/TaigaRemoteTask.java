package com.busybusy.intellij.taiga.models;

import com.google.gson.JsonObject;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.http.util.TextUtils;

import static com.busybusy.intellij.taiga.utilities.GsonUtilities.getAsStringOr;

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
			this.setStatus(getAsStringOr(current, "status", ""));
		}
		if (current.has("ref"))
		{
			this.setRef(getAsStringOr(current, "ref", ""));
		}
		if (current.has("created_date"))
		{
			this.setCreatedAt(getAsStringOr(current, "created_date", ""));
		}
		if (current.has("modified_date"))
		{
			this.setUpdatedAt(getAsStringOr(current, "modified_date", ""));
		}
		if (current.has("description"))
		{
			this.setDescription(getAsStringOr(current, "description", ""));
		}
		if (current.has("subject"))
		{
			this.setSubject(getAsStringOr(current, "subject", ""));
		}
		if (current.has("project"))
		{
			this.setProjectId(getAsStringOr(current, "project", ""));
		}
		if (current.has("id"))
		{
			this.setTaskId(getAsStringOr(current, "id", ""));
		}
		if (current.has("assigned_to"))
		{
			this.setAssignedTo(getAsStringOr(current, "assigned_to", ""));
		}
	}
}
