package com.busybusy.intellij.taiga;

import com.busybusy.intellij.taiga.models.TaigaProject;
import com.busybusy.intellij.taiga.models.TaigaTaskStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.Task;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tjones on 5/21/15.
 */
public class TaigaRepository extends BaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(TaigaRepository.class);
    private Pattern mPattern = Pattern.compile("(d+)");

    private String mAuthKey = null;
    private String mUserId = null;
    private List<TaigaProject> mProjects = null;
    private TaigaProject mSelectedProject = null;

    @NonNls
    public static final String SERVER_URL = "serverUrl";
    @NonNls
    public static final String USERNAME = "username";
    @NonNls
    public static final String PASSWORD = "password";

    private final TemplateVariable.FactoryVariable myServerTemplateVariable = new TemplateVariable.FactoryVariable(SERVER_URL) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getUrl();
        }
    };
    private final TemplateVariable.FactoryVariable myUserNameTemplateVariable = new TemplateVariable.FactoryVariable(USERNAME) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getUsername();
        }
    };
    private final TemplateVariable.FactoryVariable myPasswordTemplateVariable = new TemplateVariable.FactoryVariable(PASSWORD, true) {
        @NotNull
        @Override
        public String getValue() {
            return TaigaRepository.this.getPassword();
        }
    };

    private static final String kAuthEndpoint = "/auth";
    private static final String kUserMeEndpoint = "/users/me";
    private static final String kProjectListEndpoint = "/projects?member=";
    private static final String kTaskListEndpoint = "/tasks?project=";
    private static final String kTaskPatchEndpoint = "/tasks/";
    private static final String kTaskStatusEndpoint = "/task-statuses?project=";
    private static final String kAssigendToArg = "&assigned_to=";


    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository() {

    }

    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository(TaigaRepositoryType type) {
        super(type);
        setUseHttpAuthentication(false);
        setUrl("https://api.taiga.io/api/v1");
    }

    @SuppressWarnings("UnusedDeclaration")
    public TaigaRepository(TaigaRepository other) {
        super(other);
        mAuthKey = other.mAuthKey;
        mUserId = other.mUserId;
        mProjects = other.mProjects;
        mSelectedProject = other.mSelectedProject;
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String s) throws Exception {
        return null;
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed, @NotNull ProgressIndicator cancelled) throws Exception {
        if(mSelectedProject.getProjectId().equals("-1"))
        {
            return null;
        }
        List<TaigaTask> result = new ArrayList<TaigaTask>();
        JsonArray tasks = executeMethod(new GetMethod(getUrl() + kTaskListEndpoint + mSelectedProject.getProjectId()));
        for (int i = 0; i < tasks.size(); i++) {
            JsonObject current = tasks.get(i).getAsJsonObject();
            com.busybusy.intellij.taiga.models.TaigaTask raw = new com.busybusy.intellij.taiga.models.TaigaTask();

            raw.setStatus(current.get("status").getAsJsonPrimitive().getAsString())
                    .setRef(current.get("ref").getAsJsonPrimitive().getAsString())
                    .setCreatedAt(current.get("created_date").getAsJsonPrimitive().getAsString())
                    .setUpdatedAt(current.get("modified_date").getAsJsonPrimitive().getAsString())
                    .setDescription(current.get("description").getAsJsonPrimitive().getAsString())
                    .setSubject(current.get("subject").getAsJsonPrimitive().getAsString())
                    .setProjectId(current.get("project").getAsJsonPrimitive().getAsString())
                    .setTaskId(current.get("id").getAsJsonPrimitive().getAsString());

            TaigaTask mapped = new TaigaTask(this, raw);
            result.add(mapped);
        }
        Task[] primArray = new Task[result.size()];
        return result.toArray(primArray);
    }

    @NotNull
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public TaigaRepository clone() {
        return new TaigaRepository(this);
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                TaigaRepository.this.doTest();
            }

            @Override
            public void cancel() {
                //Jetbrains left this method blank in their generic task repo as well. Just let it time out?
            }
        };
    }

    @Nullable
    @Override
    public String extractId(@NotNull String taskName) {
        Matcher matcher = mPattern.matcher(taskName);
        return matcher.find() ? matcher.group(1) : null;
    }

    @NotNull
    @Override
    public Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception {
        Set<CustomTaskState> result = new HashSet<CustomTaskState>();
        if(mSelectedProject != null) {
            for (TaigaTaskStatus status : mSelectedProject.getStatusList())
            {
                result.add(new CustomTaskState(status.getTaigaId(), status.getName()));
            }
        }
        return result;
    }

    @Override
    public void setTaskState(@NotNull Task task, @NotNull CustomTaskState state) throws Exception {
        ((TaigaTask) task).mTask.setStatus(state.getId());
        NameValuePair[] data = {
                new NameValuePair("status", state.getId())
        };
        executeMethod(getPatchMethod(getUrl() + kTaskPatchEndpoint + ((TaigaTask) task).mTask.getTaskId(), data));
    }

    @Override
    protected int getFeatures() {
        return NATIVE_SEARCH | STATE_UPDATING;
    }

    private void doTest() throws Exception {
        mAuthKey = null;
        checkSetup();
        JsonArray response = executeMethod(getAuthMethod());
        if (response != null && response.get(0) != null) {
            JsonObject body = (JsonObject) response.get(0);
            if (body.has("_error_message")) {
                throw new Exception("Authentication failed with server error: " + body.get("_error_message").getAsJsonPrimitive().getAsString());
            } else {
                setAuthKey(body);
            }
        } else {
            throw new Exception("Unknown error: Auth method body came back null.");
        }
    }

    @Override
    public boolean isConfigured() {
        boolean result = true;
        if (!super.isConfigured()) {
            result = false;
        }
        if (result && StringUtil.isEmpty(myServerTemplateVariable.getValue())) {
            result = false;
        }
        if (result && StringUtil.isEmpty(myUserNameTemplateVariable.getValue())) {
            result = false;
        }
        if (result && StringUtil.isEmpty(myPasswordTemplateVariable.getValue())) {
            result = false;
        }
        return result;
    }

    public void checkSetup() throws Exception {
        String result = "";
        int errors = 0;
        if (StringUtil.isEmpty(myServerTemplateVariable.getValue())) {
            result += "Server";
            errors++;
        }
        if (StringUtil.isEmpty(myUserNameTemplateVariable.getValue())) {
            result += !StringUtils.isEmpty(result) ? " & " : "";
            result += "Username";
            errors++;
        }
        if (StringUtil.isEmpty(myPasswordTemplateVariable.getValue())) {
            result += !StringUtils.isEmpty(result) ? " & " : "";
            result += "Password";
            errors++;
        }
        if (!result.isEmpty()) {
            throw new Exception(result + ((errors > 1) ? " are required" : " is required"));
        }
    }

    private JsonArray executeMethod(@NotNull HttpMethod method) throws Exception {

        if (mAuthKey != null) {
            method.addRequestHeader("Content-type", "application/json");
            method.addRequestHeader("Authorization", "Bearer " + mAuthKey);
            method.addRequestHeader("x-disable-pagination", "True");
        } else {
            method.addRequestHeader("Content-type", "application/x-www-form-urlencoded");
        }
        getHttpClient().executeMethod(method);

        if (method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            JsonObject json = new JsonParser().parse(method.getResponseBodyAsString()).getAsJsonObject();
            if (json.has("_error_message") && json.get("_error_message").getAsJsonPrimitive().getAsString().equals("Invalid token")) {
                doTest();
                getHttpClient().executeMethod(method);
            }

        }

        JsonElement json = new JsonParser().parse(method.getResponseBodyAsString());

        JsonArray responseBody;

        if (json.isJsonArray()) {
            responseBody = json.getAsJsonArray();
        } else {
            responseBody = new JsonArray();
            responseBody.add(json);
        }

        if (method.getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("Request failed with HTTP error: " + method.getStatusText());
        }
        return responseBody;
    }

    private HttpMethod getAuthMethod() {
        PostMethod postMethod = new PostMethod(getUrl() + kAuthEndpoint);

        NameValuePair[] data = {
                new NameValuePair("type", "normal"),
                new NameValuePair("username", myUsername),
                new NameValuePair("password", myPassword)
        };
        postMethod.setRequestBody(data);
        return postMethod;
    }

    private HttpMethod getPatchMethod(String url, NameValuePair[] data)
    {
        PostMethod patchMethod = new PostMethod(url){
            @Override
            public String getName() {
                return "PATCH";
            }
        };

        patchMethod.setRequestBody(data);
        return patchMethod;
    }

    private void setAuthKey(JsonObject body) throws Exception {
        if (body.has("auth_token")) {
            mAuthKey = body.get("auth_token").getAsJsonPrimitive().getAsString();
        } else {
            throw new Exception("auth_token missing from server response");
        }
    }

    private void ensureUserId() throws Exception {
        if (mUserId == null || mUserId.isEmpty()) {
            JsonArray result = executeMethod(new GetMethod(getUrl() + kUserMeEndpoint));
            mUserId = result.get(0).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString();
        }
    }

    public List<TaigaProject> getProjectList() throws Exception {
        ensureUserId();
        if(mProjects == null || mProjects.isEmpty()) {
            JsonArray query = executeMethod(new GetMethod(getUrl() + kProjectListEndpoint + mUserId));
            List<TaigaProject> result = new ArrayList<TaigaProject>();
            for (int i = 0; i < query.size(); i++) {
                TaigaProject project = new TaigaProject();
                project.setProjectId(query.get(i).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString());
                project.setProjectTitle(query.get(i).getAsJsonObject().get("name").getAsJsonPrimitive().getAsString());
                project.setSlug(query.get(i).getAsJsonObject().get("slug").getAsJsonPrimitive().getAsString());
                project.setStatusList(getStatusList(project.getProjectId()));
                result.add(project);
            }

            mProjects = result;
        }
        return mProjects;
    }

    public List<TaigaTaskStatus> getStatusList(String projectId) throws Exception
    {
        JsonArray query = executeMethod(new GetMethod(getUrl() + kTaskStatusEndpoint + projectId));
        List<TaigaTaskStatus> result = new ArrayList<TaigaTaskStatus>();
        for (int i = 0; i < query.size(); i++)
        {
            TaigaTaskStatus status = new TaigaTaskStatus();
            status.setTaigaId(query.get(i).getAsJsonObject().get("id").getAsJsonPrimitive().getAsString())
                    .setName(query.get(i).getAsJsonObject().get("name").getAsJsonPrimitive().getAsString())
                    .setSlug(query.get(i).getAsJsonObject().get("slug").getAsJsonPrimitive().getAsString())
                    .setClosed(query.get(i).getAsJsonObject().get("is_closed").getAsJsonPrimitive().getAsBoolean());
            result.add(status);
        }

        return result;
    }

    public TaigaProject getSelectedProject() {
        return mSelectedProject;
    }

    public void setSelectedProject(TaigaProject mSelectedProject) {
        this.mSelectedProject = mSelectedProject;
    }

    public static final TaigaProject UNSPECIFIED_PROJECT = new TaigaProject() {
        @Override
        public String getProjectTitle() {
            return "-- Select A Project --";
        }

        @Override
        public String getProjectId() {
            return "-1";
        }

        @Override
        public String toString() {
            return getProjectTitle();
        }
    };


}
