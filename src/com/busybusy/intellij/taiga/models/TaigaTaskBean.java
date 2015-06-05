package com.busybusy.intellij.taiga.models;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * Created by Tjones on 5/26/15.
 */
@Tag("TaigaTaskBean")
public class TaigaTaskBean
{

	private String mTaskId;
	private String mRef;
	private String mProjectId;
	private String mSubject;
	private String mDescription;
	private String mUpdatedAt;
	private String mCreatedAt;
	private String mStatus;

	public TaigaTaskBean()
	{
	}

	public String getTaskId()
	{
		return mTaskId;
	}

	public TaigaTaskBean setTaskId(String taskId)
	{
		this.mTaskId = taskId;
		return this;
	}

	public String getRef()
	{
		return mRef;
	}

	public TaigaTaskBean setRef(String ref)
	{
		this.mRef = ref;
		return this;
	}

	public String getProjectId()
	{
		return mProjectId;
	}

	public TaigaTaskBean setProjectId(String projectId)
	{
		this.mProjectId = projectId;
		return this;
	}

	public String getSubject()
	{
		return mSubject;
	}

	public TaigaTaskBean setSubject(String subject)
	{
		this.mSubject = subject;
		return this;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public TaigaTaskBean setDescription(String description)
	{
		this.mDescription = description;
		return this;
	}

	public String getUpdatedAt()
	{
		return mUpdatedAt;
	}

	public TaigaTaskBean setUpdatedAt(String updatedAt)
	{
		this.mUpdatedAt = updatedAt;
		return this;
	}

	public String getCreatedAt()
	{
		return mCreatedAt;
	}

	public TaigaTaskBean setCreatedAt(String createdAt)
	{
		this.mCreatedAt = createdAt;
		return this;
	}

	public String getStatus()
	{
		return mStatus;
	}

	public TaigaTaskBean setStatus(String status)
	{
		mStatus = status;
		return this;
	}
}
