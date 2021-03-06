package com.busybusy.intellij.taiga.constants;

public class ApiConstants
{
	public interface Url
	{
		String DEFAULT = "https://api.taiga.io/api/v1";
		String WEBSITE = "https://tree.taiga.io";
	}
	public interface EndPoint
	{
		String AUTH         = "/auth";
		String USER_ME      = "/users/me";
		String PROJECT_LIST = "/projects?member=";
		String TASK_LIST    = "/tasks?project=";
		String TASKS        = "/tasks/";
		String TASK_STATUS  = "/task-statuses?project=";
	}

	public interface Arguments
	{
		String ASSIGNED_TO = "&assigned_to=";
	}

}
