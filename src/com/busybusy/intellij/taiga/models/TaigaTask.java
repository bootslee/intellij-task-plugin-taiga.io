package com.busybusy.intellij.taiga.models;

/**
 * Created by Tjones on 5/26/15.
 */
public class TaigaTask
{

	private String mTaskId;
	private String mRef;
	private String mProjectId;
	private String mSubject;
	private String mDescription;
	private String mUpdatedAt;
	private String mCreatedAt;
	private String mStatus;

	public TaigaTask()
	{
	}

	public String getTaskId()
	{
		return mTaskId;
	}

	public TaigaTask setTaskId(String taskId)
	{
		this.mTaskId = taskId;
		return this;
	}

	public String getRef()
	{
		return mRef;
	}

	public TaigaTask setRef(String ref)
	{
		this.mRef = ref;
		return this;
	}

	public String getProjectId()
	{
		return mProjectId;
	}

	public TaigaTask setProjectId(String projectId)
	{
		this.mProjectId = projectId;
		return this;
	}

	public String getSubject()
	{
		return mSubject;
	}

	public TaigaTask setSubject(String subject)
	{
		this.mSubject = subject;
		return this;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public TaigaTask setDescription(String description)
	{
		this.mDescription = description;
		return this;
	}

	public String getUpdatedAt()
	{
		return mUpdatedAt;
	}

	public TaigaTask setUpdatedAt(String updatedAt)
	{
		this.mUpdatedAt = updatedAt;
		return this;
	}

	public String getCreatedAt()
	{
		return mCreatedAt;
	}

	public TaigaTask setCreatedAt(String createdAt)
	{
		this.mCreatedAt = createdAt;
		return this;
	}

	public String getStatus()
	{
		return mStatus;
	}

	public TaigaTask setStatus(String status)
	{
		mStatus = status;
		return this;
	}
}
