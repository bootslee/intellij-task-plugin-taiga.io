package com.busybusy.intellij.taiga;

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
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Created by Tjones on 5/21/15.
 */
public class TaigaRepository extends BaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(TaigaRepository.class);

    private String mAuthKey = null;

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
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String s) throws Exception {
        return null;
    }

    @NotNull
    @Override
    public Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception {
        return super.getAvailableTaskStates(task);
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed, @NotNull ProgressIndicator cancelled) throws Exception {
        return super.getIssues(query, offset, limit, withClosed, cancelled);
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

    private void setAuthKey(JsonObject body) throws Exception {
        if (body.has("auth_token")) {
            mAuthKey = body.get("auth_token").getAsJsonPrimitive().getAsString();
        } else {
            throw new Exception("auth_token missing from server response");
        }
    }
}
